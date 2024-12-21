package com.ds.deploysurfingbackend.domain.aws.entity;

import com.ds.deploysurfingbackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import software.amazon.awssdk.services.ec2.model.InstanceType;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "ec2")
public class EC2 {

    //EC2 메타데이터 저장
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String ec2Id;

    //인스턴스 타입
    @Enumerated(value = EnumType.STRING)
    private InstanceType instanceType;

    //IP : 할당받은 탄력적 IP
    private String publicIp;

    //연결된 VPC ID
    private String vpcId;

    //키 페어 이름 (AWS 내)
    private String keyName;

    //ssh key file 경로 (서버 내)
    private String keyFilePath;

    //탄력적 IP 할당 ID
    private String associationId;

    //보안그룹 이름
    private String securityGroupName;

    //보안그룹 ID
    private String securityGroupId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
