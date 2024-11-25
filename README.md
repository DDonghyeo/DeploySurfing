# Deploy Surifng
자동 CI/CD 파이프라인 구축 애플리케이션

<br><br>

# 작동 방식
- Repository URL 입력을 통해 앱 등록

- aws-sdk를 이용하여 EC2생성, 키페어 생성, IP 연결
- Github REST API를 이용하여 deploy 브랜치 생성, Action Secret 구성, CI/CD 스크립트 및 Dockerfile 자동 커밋

<br><br>
# Tech
Spring Boot 3.2.3
Java 17
MySQL, Spring Security, WebFlux, Libsodium, aws-sdk 2, Swagger, Redis
