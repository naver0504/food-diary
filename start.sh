echo "server start"

: <<'END'
nohup 으로 세션종료되어도 실행, & 으로 백그라운드 실행, /dev/null 로 로그생성 금
END

nohup java -jar api-0.0.1-SNAPSHOT.jar --spring.profiles.active=local 1>/dev/null 2>&1 &