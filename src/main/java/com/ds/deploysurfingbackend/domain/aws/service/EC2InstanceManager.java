package com.ds.deploysurfingbackend.domain.aws.service;

import com.ds.deploysurfingbackend.domain.aws.entity.EC2;
import com.ds.deploysurfingbackend.domain.aws.exception.AwsErrorCode;
import com.ds.deploysurfingbackend.domain.aws.type.EC2AMI;
import com.ds.deploysurfingbackend.global.annotation.RedissonLock;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EC2InstanceManager {

    private static final String SECURITY_GROUP_NAME = "deploySurfing_Security_Group";
    private static final String SECURITY_GROUP_DESC = "Created By DeploySurfing";

    public EC2 createFreeTierEC2(String name,
                                        StaticCredentialsProvider staticCredentialsProvider) {

        log.info(" [ AWS Utils ] 새로운 EC2를 생성합니다. 이름 ---> {}", name);

        Region region = Region.AP_NORTHEAST_2; // 서울 REGION
        Ec2Client ec2 = createEc2Client(staticCredentialsProvider, region);

        String keyFilePathStr = UUID.randomUUID() + "_" + name + ".pem";

        try {
            createKeyPair(name, ec2, keyFilePathStr);


            //----------------- [ 보안 그룹 생성 ] -----------------
            String vpcId = getVpcId(ec2);

            log.info(" [ AWS Utils ] 새로운 보안그룹을 생성합니다. VPC ID ---> {}", vpcId);
            String secGroupId = createBasicSecurityGroup(ec2, SECURITY_GROUP_NAME + UUID.randomUUID().toString().substring(0, 4), SECURITY_GROUP_DESC, vpcId);
            log.info(" [ AWS Utils ] 새로운 보안그룹을 생성에 성공했습니다. Group ID ---> {}", secGroupId);


            //----------------- [ EC2 실행 ] -----------------

            RunInstancesRequest runRequest = createEc2RunRequest(name);
            //인스턴스 실행까지 기다리기 위해 Waiter 사용
            log.info(" [ AWS Utils ] 인스턴스가 실행될 때 까지 대기합니다..");
            RunInstancesResponse response = runInstance(ec2, runRequest);


            //----------------- [ EC2 정보 ] -----------------
            String instanceIdVal = response.instances().get(0).instanceId();
            ec2.waiter().waitUntilInstanceRunning(r -> r.instanceIds(instanceIdVal));

            //태그 생성(이름)
            ec2.createTags(CreateTagsRequest.builder()
                    .resources(instanceIdVal)
                    .tags(Tag.builder().key("Name").value(name).build())
                    .build());
            log.info(" [ AWS Utils ] 인스턴스가 실행되었습니다. instance id ---> {}", instanceIdVal);


            //----------------- [ 탄력적 IP 할당 ] -----------------
            log.info(" [ AWS Utils ] 탄력적 IP를 할당합니다.");
            //탄력적 IP 할당
            String allocationId = allocateAddress(ec2);
            log.info(" [ AWS Utils ] 탄력적 IP를 할당에 성공했습니다. ---> allocation ID {}", allocationId);
            //탄력적 IP 연결
            String associationId = associateAddress(ec2, instanceIdVal, allocationId);
            log.info(" [ AWS Utils ] 탄력적 IP 연결에 성공했습니다. ---> association ID {}", associationId);


            log.info("[ AWS Utils ] : EC2 생성에 성공했습니다. ---> {}", instanceIdVal);

            String publicIp = getPublicIp(ec2, instanceIdVal);


            ec2.close();

            return EC2.builder()
                    .id(instanceIdVal)
                    .instanceType(InstanceType.T2_MICRO)
                    .securityGroupName(SECURITY_GROUP_NAME)
                    .vpcId(vpcId)
                    .publicIp(publicIp)
                    .keyFilePath(keyFilePathStr)
                    .associationId(associationId)
                    .securityGroupId(secGroupId)
                    .build();

        } catch (AwsServiceException | SdkClientException e) {
            ec2.close();
            //EC2 생성 실패
            if (e instanceof Ec2Exception) {
                log.error(((Ec2Exception) e).awsErrorDetails().errorMessage());
            }
            throw new CustomException(CommonErrorCode.SERVER_ERROR, e.getMessage());

        } catch (IOException e) {
            log.error("IO Exception : 파일 쓰기에 실패했습니다.");
            throw new RuntimeException(e);
        }
    }

    private RunInstancesResponse runInstance(Ec2Client ec2, RunInstancesRequest runRequest) {
        return ec2.runInstances(runRequest);
    }

    private Ec2Client createEc2Client(StaticCredentialsProvider staticCredentialsProvider, Region region) {
        return Ec2Client.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(region)
                .build();
    }

    private RunInstancesRequest createEc2RunRequest(String name) {
        return RunInstancesRequest.builder()
                .imageId(EC2AMI.AMAZON_LINUX_2023_AMI.getValue())
                .instanceType(InstanceType.T2_MICRO) //프리티어가 가능한 t2_micro
                .securityGroupIds(SECURITY_GROUP_NAME)
                .keyName(name)
                .additionalInfo("Created By DeploySurfing")
                .maxCount(1)
                .minCount(1)
                .build();
    }

    private String getVpcId(Ec2Client ec2) {
        //보안 그룹 추가할 VPC ID
        List<Vpc> vpcs = describeVpc(ec2);

        if (vpcs.isEmpty()) {
            throw new CustomException(AwsErrorCode.VPC_NOT_FOUND);
        }
        String vpcId = vpcs.get(0).vpcId();

        return vpcId;
    }

    private void createKeyPair(String name, Ec2Client ec2, String keyFilePathStr) throws IOException {
        //----------------- [ 키 페어 생성 ] -----------------
        log.info(" [ AWS Utils ] 새로운 키 페어를 생성합니다. 이름 ---> {}", name);
        //중복되는 키가 있는지 확인
        checkDuplicateKeyName(ec2, name);
        String keyFileContent = createKeyPair(ec2, name);

        //키 리소스 저장
        Path keyFilePath = Paths.get(keyFilePathStr);
        Files.write(keyFilePath, keyFileContent.getBytes());
        log.info(" [ AWS Utils ] 키 페어 생성에 성공했습니다. 내용 ---> {}", keyFileContent);
    }

    @RedissonLock(value = "#instanceId", waitTime = 10000L, leaseTime = 30000L)
    public void pauseEC2(StaticCredentialsProvider staticCredentialsProvider,
                                String instanceId) {

        Region region = Region.AP_NORTHEAST_2; // 서울 REGION
        Ec2Client ec2 = createEc2Client(staticCredentialsProvider, region);

        try {
            StopInstancesRequest stopInstancesRequest = StopInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            StopInstancesResponse response = ec2.stopInstances(stopInstancesRequest);

            ec2.close();

            if (response.hasStoppingInstances()) {
                log.info(" [ AWS Utils ]  인스턴스 중지에 성공했습니다.");
            } else log.error(" [ AWS Utils ]  인스턴스 중지에 실패했습니다.");

        } catch (AwsServiceException | SdkClientException e){
            ec2.close();
            log.error(" [ AWS Utils ]  인스턴스 중지에 실패했습니다. : {}", e.getMessage());
            throw new CustomException(CommonErrorCode.SERVER_ERROR, e.getMessage());
        }
    }


    @RedissonLock(value = "#instanceId", waitTime = 10000L, leaseTime = 30000L)
    public void terminateEC2(StaticCredentialsProvider staticCredentialsProvider,
                                    String instanceId,
                                    String allocationId,
                                    String secGroupId,
                                    String keyName
    ) {
        log.info(" [ AWS Utils ] 새로운 EC2를 종료합니다. id ---> {}", instanceId);

        Region region = Region.AP_NORTHEAST_2; // 서울 REGION
        Ec2Client ec2 = createEc2Client(staticCredentialsProvider, region);

        try {
            log.info(" [ AWS Utils ] 탄력적 IP 할당을 해제합니다. id ---> {}", instanceId);
            disassociateAddress(ec2, allocationId);
            log.info(" [ AWS Utils ] 탄력적 IP를 릴리즈합니다. id ---> {}", instanceId);
            releaseEC2Address(ec2, allocationId);


            //인스턴스 종료까지 기다리기 위해 Waiter 사용
            log.info(" [ AWS Utils ] 인스턴스가 종료될 때 까지 대기합니다..");
            TerminateInstancesRequest ti = TerminateInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();
            ec2.terminateInstances(ti);
            ec2.waiter().waitUntilInstanceTerminated(r -> r.instanceIds(instanceId));
            log.info(" [ AWS Utils ] 인스턴스 종료에 성공했습니다.");


            log.info(" [ AWS Utils ] 보안 그룹을 삭제합니다.");
            deleteEC2SecGroup(ec2, secGroupId);

            log.info(" [ AWS Utils ] 키 페어를 삭제합니다.");
            deleteKeys(ec2, keyName);

            log.info(" [ AWS Utils ] EC2 삭제를 완료했습니다.");
            ec2.close();

        } catch (AwsServiceException | SdkClientException e) {
            log.info(" [ AWS Utils ] EC2 삭제에 실패했습니다 : {}", e.getMessage());
            throw new CustomException(CommonErrorCode.SERVER_ERROR, e.getMessage());
        }
    }


    //  ---- private method ----

    private String createKeyPair(Ec2Client ec2, String keyName) {

        CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                .keyName(keyName)
                .build();

        CreateKeyPairResponse response = ec2.createKeyPair(request);

        return response.keyMaterial();
    }

    public void checkDuplicateKeyName(Ec2Client ec2, String keyName) {

        DescribeKeyPairsResponse response = ec2.describeKeyPairs();
        response.keyPairs().forEach(keyPair -> {
            log.info(
                    "키 이름 ---> {}" +
                            "키 지문 ---> {}",
                    keyPair.keyName(),
                    keyPair.keyFingerprint());

            //키 이름이 중복될 경우 오류 발생
            if (keyPair.keyName().equals(keyName)) {
                throw new CustomException(AwsErrorCode.DUPLICATE_KEY_NAME);
            }
        });
    }


    public String getPublicIp(Ec2Client ec2, String newInstanceId) {

        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .instanceIds(newInstanceId)
                .build();

        DescribeInstancesResponse response = ec2.describeInstances(request);
        String state = response.reservations().get(0).instances().get(0).state().name().name();
        if (state.compareTo("RUNNING") == 0) {
            log.info("[ AWS Utils ] EC2 Image ID ---> {}", response.reservations().get(0).instances().get(0).imageId());
            log.info("[ AWS Utils ] EC2 Instance Type ---> {}", response.reservations().get(0).instances().get(0).instanceType());
            log.info("[ AWS Utils ] EC2 State ---> {}", response.reservations().get(0).instances().get(0).state().name());
            log.info("[ AWS Utils ] EC2 Public Address ---> {}", response.reservations().get(0).instances().get(0).publicIpAddress());
            return response.reservations().get(0).instances().get(0).publicIpAddress();
        } else {
            throw new CustomException(AwsErrorCode.INSTACE_IS_NOT_RUNNING);
        }
    }

    public List<Vpc> describeVpc(Ec2Client ec2) {
        DescribeVpcsResponse response = ec2.describeVpcs();
        return response.vpcs();
    }


    public String createBasicSecurityGroup(Ec2Client ec2, String groupName, String groupDesc, String vpcId) {
        try {
            CreateSecurityGroupRequest createRequest = CreateSecurityGroupRequest.builder()
                    .groupName(groupName)
                    .description(groupDesc)
                    .vpcId(vpcId)
                    .build();

            CreateSecurityGroupResponse resp = ec2.createSecurityGroup(createRequest);
            IpRange ipAllRange = IpRange.builder()
                    .cidrIp("")
                    .cidrIp("0.0.0.0/0")
                    .build();

            List<IpPermission> ipPermissions = new ArrayList<>();
            ipPermissions.add(getIpPerm(80, ipAllRange));
            ipPermissions.add(getIpPerm(443, ipAllRange));
            ipPermissions.add(getIpPerm(22, ipAllRange));

            AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
                    .groupName(groupName)
                    .ipPermissions(ipPermissions)
                    .build();

            ec2.authorizeSecurityGroupIngress(authRequest);
            log.info("[ AWS Utils ] 보안 그룹 생성에 성공했습니다. ---> {}", groupName);
            return resp.groupId();
        } catch (Ec2Exception e) {
            throw new CustomException(AwsErrorCode.DUPLICATE_SECURITY_GROUP);
        }

    }

    public String allocateAddress(Ec2Client ec2) {

        AllocateAddressRequest allocateRequest = AllocateAddressRequest.builder()
                .domain(DomainType.VPC)
                .build();

        AllocateAddressResponse allocateResponse = ec2.allocateAddress(allocateRequest);
        return allocateResponse.allocationId();
    }

    public String associateAddress(Ec2Client ec2, String instanceId, String allocationId) {
        AssociateAddressRequest associateRequest = AssociateAddressRequest.builder()
                .instanceId(instanceId)
                .allocationId(allocationId)
                .build();

        AssociateAddressResponse associateResponse = ec2.associateAddress(associateRequest);
        return associateResponse.associationId();

    }

    public void disassociateAddress(Ec2Client ec2, String associationId) {
        DisassociateAddressRequest addressRequest = DisassociateAddressRequest.builder()
                .associationId(associationId)
                .build();

        ec2.disassociateAddress(addressRequest);
    }

    public void releaseEC2Address(Ec2Client ec2, String allocId) {
        ReleaseAddressRequest request = ReleaseAddressRequest.builder()
                .allocationId(allocId)
                .build();

        ec2.releaseAddress(request);
    }

    public void deleteEC2SecGroup(Ec2Client ec2, String groupId) {
        DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
                .groupId(groupId)
                .build();

        ec2.deleteSecurityGroup(request);
    }

    public void deleteKeys(Ec2Client ec2, String keyPair) {
        DeleteKeyPairRequest request = DeleteKeyPairRequest.builder()
                .keyName(keyPair)
                .build();

        ec2.deleteKeyPair(request);
    }



    private IpPermission getIpPerm(int integer, IpRange ipAllRange) {
        return IpPermission.builder()
                .ipProtocol("tcp")
                .toPort(integer)
                .fromPort(integer)
                .ipRanges(ipAllRange)
                .build();
    }

}
