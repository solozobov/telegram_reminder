#!/usr/bin/env bash
set -e

mkdir ~/remember
echo>~/remember/application.properties
apt-get update
apt-get install -y openjdk-8-jdk maven git
git config --global credential.helper store
git clone https://github.com/solozobov/telegram_reminder.git
cd telegram_reminder
mvn clean install -Dmaven.test.skip=true -Dremember.db.url=jdbc:h2:~/remember/db -Dremember.db.user=remember -Dremember.db.password=remember
nohup /usr/bin/java -jar target/remember-1.0-SNAPSHOT.jar com.solozobov.andrei.Application --spring.config.location=file:/root/remember/application.properties 2>&1 >> ~/remember/log &
echo $! >> ~/remember/pid
cp update.sh ~/remember/update.sh
chmod 700 ~/remember/update.sh
