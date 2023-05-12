# backend
백엔드 서버 코드 저장소입니다.

## 사용기술
jdk 17
Spring boot
JOOQ
MySQL 8.0
AWS EC2, S3
testcontainers

## 빌드 전에 할일
### jooq codeGen으로 JOOQ에서 쓰일 class들을 생성해야 한다.
.\gradlew -q generateJooqCode

## test시 주의점
### docker가 실행중이어야 합니다.
### windows, mac 등은 rancher desktop 사용하면 컨테이너 관리됩니다. docker관리툴입니다.