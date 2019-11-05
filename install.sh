#!/usr/bin/env bash
set -ex

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

echo "
sql () {
  rm -rf ~/h2_shell
  mkdir ~/h2_shell
  cp ~/remember/db.mv.db ~/h2_shell/db.mv.db
  cp ~/.m2/repository/com/h2database/h2/*/h2*.jar ~/h2_shell/h2.jar
  pushd ~/h2_shell
  java -cp h2*.jar org.h2.tools.Shell -url jdbc:h2:/root/h2_shell/db -driver org.h2.Driver -user $1 -password $2
  popd
  rm -rf ~/h2_shell
}" >> ~/.profile

