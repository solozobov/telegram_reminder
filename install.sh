#!/usr/bin/env bash
set -ex

apt-get update
apt-get install -y openjdk-8-jdk maven git

work_dir=`pwd`
mkdir remember
cd remember
touch application.properties
git config --global credential.helper store
git clone https://github.com/solozobov/telegram_reminder.git
cd telegram_reminder
mvn clean install -Dmaven.test.skip=true -Dremember.db.url=jdbc:h2:~/remember/db -Dremember.db.user=remember -Dremember.db.password=remember

cd ..
cp telegram_reminder/update.sh update.sh
chmod 700 update.sh
nohup /usr/bin/java -jar ~/telegram_reminder/target/remember-1.0-SNAPSHOT.jar com.solozobov.andrei.Application --spring.config.location=file:/root/remember/application.properties &
echo $! >> pid

echo "§
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

echo "
sqlprod () {
  rm -rf ~/h2_shell
  mkdir ~/h2_shell
  cp ~/remember/db.mv.db ~/h2_shell/db.mv.db
  cp ~/.m2/repository/com/h2database/h2/*/h2*.jar ~/h2_shell/h2.jar
  pushd ~/h2_shell
  kill -9 `jps | grep remember | cut -d ' ' -f 1`
  java -cp h2*.jar org.h2.tools.Shell -url jdbc:h2:/root/h2_shell/db -driver org.h2.Driver -user $1 -password $2
  ~/remember/./update.sh
  popd
  rm -rf ~/h2_shell
}" >> ~/.profile

