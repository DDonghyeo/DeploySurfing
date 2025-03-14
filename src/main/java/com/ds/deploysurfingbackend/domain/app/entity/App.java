package com.ds.deploysurfingbackend.domain.app.entity;

import com.ds.deploysurfingbackend.domain.app.entity.type.AppStatus;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppType;
import com.ds.deploysurfingbackend.domain.app.dto.AppDto;
import com.ds.deploysurfingbackend.domain.aws.entity.EC2;
import com.ds.deploysurfingbackend.domain.user.entity.User;
import com.ds.deploysurfingbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "app")
public class App extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "app_id", nullable = false)
    private String id;

    //앱 이름
    @Column(name = "name", nullable = false)
    private String name;

    //앱 타입
    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AppType type;

    //앱 설명
    @Column(name = "description")
    private String description;

    //앱 상태
    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //초기화 여부
    private boolean isInit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ec2_id", unique = true)
    private EC2 ec2;

    @OneToOne(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GithubMetaData gitHubMetaData;

    @OneToOne(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AppMetadata metaData;

    public void setUser(User user) {
        this.user = user;
    }

    public void update(AppDto.UpdateAppDto updateAppDto) {
        name = updateAppDto.name();
        description = updateAppDto.description();
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public void setStatus(AppStatus status) {
        this.status = status;
    }

    public void setMetaData(AppMetadata metaData) {
        this.metaData = metaData;
        metaData.setApp(this);
    }
}
