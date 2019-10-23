#!/usr/bin/env bash
set -e

cd ~/telegram_reminder
git pull
cp update.sh ~/remember/update.sh
chmod 700 ~/remember/update.sh
mvn clean install -Dmaven.test.skip=true -Dremember.db.url=jdbc:h2:~/remember/db -Dremember.db.user=remember -Dremember.db.password=remember
for pid in `cat pid`;
do
  kill -9 $pid;
done
rm ~/remember/pid
bash /usr/bin/java -jar target/remember-1.0-SNAPSHOT.jar com.solozobov.andrei.Application --spring.config.location=classpath:~/remember/application.properties >> ~/remember/log &
echo $! >> ~/remember/pid
