package com.ds.deploysurfingbackend.domain;

import com.ds.deploysurfingbackend.domain.type.AppStatus;
import com.ds.deploysurfingbackend.domain.type.AppType;
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
}
