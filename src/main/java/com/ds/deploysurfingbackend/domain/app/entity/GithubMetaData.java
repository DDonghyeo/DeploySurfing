package com.ds.deploysurfingbackend.domain.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "github_meta_data")
public class GithubMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "repoUrl", nullable = false)
    private String repoUrl;
    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "repoName", nullable = false)
    private String repoName;

    private String repoPublicKeyId;

    private String repoPublicKey;

    @OneToOne
    @JoinColumn(name = "app_id")
    private App app;

    public void setRepoPublicKeyId(String repoPublicKeyId) {
        this.repoPublicKeyId = repoPublicKeyId;
    }

    public void setRepoPublicKey(String repoPublicKey) {
        this.repoPublicKey = repoPublicKey;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
