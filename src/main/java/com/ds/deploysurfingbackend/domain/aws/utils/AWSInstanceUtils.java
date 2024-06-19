package com.ds.deploysurfingbackend.domain.aws.utils;

import com.ds.deploysurfingbackend.domain.aws.domain.Ec2;
import com.ds.deploysurfingbackend.domain.aws.type.EC2AMI;
import com.ds.deploysurfingbackend.global.exception.CustomException;
import com.ds.deploysurfingbackend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AWSInstanceUtils {

    private static final String SECURITY_GROUP_NAME = "deploySurfing_Security_Group";
    private static final String SECURITY_GROUP_DESC = "Created By DeploySurfing";


    public static Ec2 createFreeTierEC2(String name,
                                        StaticCredentialsProvider staticCredentialsProvider) {

        log.info(" [ AWS Utils ] 새로운 EC2를 생성합니다. 이름 ---> {}", name);

        Region region = Region.AP_NORTHEAST_2; // 서울 REGION
        Ec2Client ec2 = Ec2Client.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(region)
                .build();

        String keyFilePathStr = "" + name + ".pem";

        try {
            //----------------- [ 키 페어 생성 ] -----------------
            log.info(" [ AWS Utils ] 새로운 키 페어를 생성합니다. 이름 ---> {}", name);
            checkDuplicateKeyName(ec2, name);
            String keyFileContent = createKeyPair(ec2, name);

            //키 리소스 저장

            Path keyFilePath = Paths.get(keyFilePathStr);
            Files.write(keyFilePath, keyFileContent.getBytes());
            log.info(" [ AWS Utils ] 키 페어 생성에 성공했습니다. 내용 ---> {}", keyFileContent);


            //----------------- [ 보안 그룹 생성 ] -----------------
            //보안 그룹 추가할 VPC ID
            List<Vpc> vpcs = describeVpc(ec2);

            if (vpcs.isEmpty()) {
                throw new CustomException(ErrorCode.VPC_NOT_FOUND);
            }
            String vpcId = vpcs.get(0).vpcId();

            log.info(" [ AWS Utils ] 새로운 보안그룹을 생성합니다. VPC ID ---> {}", vpcId);

            String secGroupId = createBasicSecurityGroup(ec2, SECURITY_GROUP_NAME, SECURITY_GROUP_DESC, vpcId);


            //----------------- [ EC2 실행 ] -----------------

            RunInstancesRequest runRequest = RunInstancesRequest.builder()
                    .imageId(EC2AMI.AMAZON_LINUX_2023_AMI.getValue())
                    .instanceType(InstanceType.T2_MICRO) //프리티어가 가능한 t2_micro
                    .securityGroupIds(SECURITY_GROUP_NAME)
                    .keyName(name)
                    .additionalInfo("Created By DeploySurfing")
                    .maxCount(1)
                    .minCount(1)
                    .build();

            //인스턴스 실행까지 기다리기 위해 Waiter 사용
            log.info(" [ AWS Utils ] 인스턴스가 실행될 때 까지 대기합니다..");

            RunInstancesResponse response = ec2.runInstances(runRequest);


            //----------------- [ EC2 정보 ] -----------------
            String instanceIdVal = response.instances().get(0).instanceId();
            ec2.waiter().waitUntilInstanceRunning(r -> r.instanceIds(instanceIdVal));


            //----------------- [ 탄력적 IP 할당 ] -----------------
            log.info(" [ AWS Utils ] 탄력적 IP를 할당합니다.");
            //탄력적 IP 할당
            String allocationId = allocateAddress(ec2);
            log.info(" [ AWS Utils ] 탄력적 IP를 할당에 성공했습니다. ---> {}", allocationId);
            //탄력적 IP 연결
            String associationId = associateAddress(ec2, instanceIdVal, allocationId);
            log.info(" [ AWS Utils ] 탄력적 IP 연결에 성공했습니다. ---> {}", associationId);


            log.info("[ AWSInstanceUtils ] : EC2 생성에 성공했습니다. ---> {}", instanceIdVal);

            describeEC2Instances(ec2, instanceIdVal);


            ec2.close();

            return Ec2.builder()
                    .id(instanceIdVal)
                    .instanceType(InstanceType.T2_MICRO)
                    .securityGroupName(SECURITY_GROUP_NAME)
                    .vpcId(vpcId)
                    .keyFilePath(keyFilePathStr)
                    .associationId(associationId)
                    .secGroupId(secGroupId)
                    .build();

        } catch (Ec2Exception e) {
            ec2.close();
            //EC2 생성 실패
            log.error(e.awsErrorDetails().errorMessage());
            throw new RuntimeException();
        } catch (IOException e) {
            log.error("IO Exception : 파일 쓰기에 실패했습니다.");
            throw new RuntimeException(e);
        }
    }


    public static void terminateEC2(StaticCredentialsProvider staticCredentialsProvider,
                                    String instanceId,
                                    String allocationId,
                                    String secGroupId,
                                    String keyName
    ) {
        log.info(" [ AWS Utils ] 새로운 EC2를 종료합니다. id ---> {}", instanceId);

        Region region = Region.AP_NORTHEAST_2; // 서울 REGION
        Ec2Client ec2 = Ec2Client.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(region)
                .build();


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
        deleteKeys(ec2,keyName);

        log.info(" [ AWS Utils ] EC2 삭제를 완료했습니다.");

    }










    private static String createKeyPair(Ec2Client ec2, String keyName) {

        CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                .keyName(keyName)
                .build();

        CreateKeyPairResponse response = ec2.createKeyPair(request);

        return response.keyMaterial();
    }

    public static void checkDuplicateKeyName(Ec2Client ec2, String keyName) {

        DescribeKeyPairsResponse response = ec2.describeKeyPairs();
        response.keyPairs().forEach(keyPair -> {
            log.info(
                    "키 이름 ---> {}" +
                            "키 지문 ---> {}",
                    keyPair.keyName(),
                    keyPair.keyFingerprint());

            //키 이름이 중복될 경우 오류 발생
            if (keyPair.keyName().equals(keyName)) {
                throw new CustomException(ErrorCode.DUPLICATE_KEY_NAME);
            }
        });
    }


    public static void describeEC2Instances(Ec2Client ec2, String newInstanceId) {

        boolean isRunning = false;
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .instanceIds(newInstanceId)
                .build();

        while (!isRunning) {
            DescribeInstancesResponse response = ec2.describeInstances(request);
            String state = response.reservations().get(0).instances().get(0).state().name().name();
            if (state.compareTo("RUNNING") == 0) {
                log.info("[ AWS Utils ] EC2 Image ID ---> {}", response.reservations().get(0).instances().get(0).imageId());
                log.info("[ AWS Utils ] EC2 Instance Type ---> {}", response.reservations().get(0).instances().get(0).instanceType());
                log.info("[ AWS Utils ] EC2 State ---> {}", response.reservations().get(0).instances().get(0).state().name());
                log.info("[ AWS Utils ] EC2 Public Address ---> {}", response.reservations().get(0).instances().get(0).publicIpAddress());
                isRunning = true;
            }
        }
    }

    public static List<Vpc> describeVpc(Ec2Client ec2) {
        DescribeVpcsResponse response = ec2.describeVpcs();
        return response.vpcs();
    }


    public static String createBasicSecurityGroup(Ec2Client ec2, String groupName, String groupDesc, String vpcId) {
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
    }

    public static String allocateAddress(Ec2Client ec2) {

        AllocateAddressRequest allocateRequest = AllocateAddressRequest.builder()
                .domain(DomainType.VPC)
                .build();

        AllocateAddressResponse allocateResponse = ec2.allocateAddress(allocateRequest);
        return allocateResponse.allocationId();
    }

    public static String associateAddress(Ec2Client ec2, String instanceId, String allocationId) {
        AssociateAddressRequest associateRequest = AssociateAddressRequest.builder()
                .instanceId(instanceId)
                .allocationId(allocationId)
                .build();

        AssociateAddressResponse associateResponse = ec2.associateAddress(associateRequest);
        return associateResponse.associationId();

    }

    public static void disassociateAddress(Ec2Client ec2, String associationId) {
        DisassociateAddressRequest addressRequest = DisassociateAddressRequest.builder()
                .associationId(associationId)
                .build();

        ec2.disassociateAddress(addressRequest);
    }

    public static void releaseEC2Address(Ec2Client ec2, String allocId) {
        ReleaseAddressRequest request = ReleaseAddressRequest.builder()
                .allocationId(allocId)
                .build();

        ec2.releaseAddress(request);
    }

    public static void deleteEC2SecGroup(Ec2Client ec2, String groupId) {
        DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
                .groupId(groupId)
                .build();

        ec2.deleteSecurityGroup(request);
    }

    public static void deleteKeys(Ec2Client ec2, String keyPair) {
        DeleteKeyPairRequest request = DeleteKeyPairRequest.builder()
                .keyName(keyPair)
                .build();

        ec2.deleteKeyPair(request);
    }






    private static IpPermission getIpPerm(int integer, IpRange ipAllRange) {
        return IpPermission.builder()
                .ipProtocol("tcp")
                .toPort(integer)
                .fromPort(integer)
                .ipRanges(ipAllRange)
                .build();
    }

}
