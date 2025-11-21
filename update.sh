ps aux | grep '/root/wats.pw/target' | awk '{print $2}' | xargs kill
git pull
nohup ./mvnw spring-boot:run > output.log 2>&1 &
cd ~
