package com.ds.deploysurfingbackend.domain.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_metadata")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private App app;

    // 설정 파일 (yml)
    @Column
    private String configFile;

    @Column
    private String version;

    @Column
    private String port;

    public void setConfigFile(String configFiles) {
        this.configFile = configFiles;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public void setPort(String port) {
        this.port = port;
    }
}

