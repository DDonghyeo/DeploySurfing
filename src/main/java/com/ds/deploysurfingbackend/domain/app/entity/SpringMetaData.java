package com.ds.deploysurfingbackend.domain.app.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Builder
@DiscriminatorValue("SPRING")
@Table(name = "spring_metadata")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpringMetaData extends AppMetadata {
    private String springBootVersion;
    private String javaVersion;
}
