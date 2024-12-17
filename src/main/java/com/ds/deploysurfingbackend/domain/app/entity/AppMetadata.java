package com.ds.deploysurfingbackend.domain.app.entity;

import com.ds.deploysurfingbackend.domain.app.entity.type.FrameworkType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_metadata")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn //부모 클래스 선언 (JPA)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AppMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private App app;

    @Column(name = "framework_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FrameworkType frameworkType;

    // 설정 파일 (yml)
    @Column(columnDefinition = "json")
    private String configFile;

    public void setConfigFile(String configFiles) {
        this.configFile = configFiles;
    }

}

