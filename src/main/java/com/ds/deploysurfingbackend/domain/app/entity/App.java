package com.ds.deploysurfingbackend.domain.app.entity;

import com.ds.deploysurfingbackend.domain.app.entity.type.AppStatus;
import com.ds.deploysurfingbackend.domain.app.entity.type.AppType;
import com.ds.deploysurfingbackend.domain.app.dto.AppDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "app")
public class App extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "app_id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AppType type;

    @Column(name = "description")
    private String description;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppStatus status;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ------------ GitHub 관련 내용 -------------
    @Column(name = "repoUrl", nullable = false)
    private String repoUrl;
    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "repoName", nullable = false)
    private String repoName;

    private String repoPublicKeyId;

    private String repoPublicKey;

    //------------------------------------------

    private boolean isInit;

    public void setRepoPublicKeyId(String repoPublicKeyId) {
        this.repoPublicKeyId = repoPublicKeyId;
    }

    public void setRepoPublicKey(String repoPublicKey) {
        this.repoPublicKey = repoPublicKey;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void update(AppDto.updateAppDto updateAppDto) {
        name = updateAppDto.getName();
        description = updateAppDto.getDescription();
    }

    public void setInit(boolean init) {
        isInit = init;
    }
}
