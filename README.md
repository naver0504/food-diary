# backend
백엔드 서버 코드 저장소입니다.

## 사용기술
jdk 17   
Spring boot   
JPA   
MySQL 8.0   
AWS EC2, S3   
testcontainers

## 실행하려면 project 폴더에서 다음을 입력후
`gradlew bootJar`
## build/libs 폴더에 가서 생성된 jar를 다음과 같이 profile 에 맞게 실행하면 된다.
`java -jar api-0.0.1-SNAPSHOT.jar --spring.profiles.active=production`
### production환경에서는 start.sh, stop.sh를 이용하여 시작, 종료를 간단히 해주시면 됩니다.
`sh start.sh`

## test시 주의점
### docker가 실행중이어야 합니다.
### windows, mac 등은 rancher desktop, minikube 등으로 testcontainers에 docker를 연결하도록 해야 합니다.
### 부분 유료화된 docker desktop도 가능하나 권장하지는 않습니다.