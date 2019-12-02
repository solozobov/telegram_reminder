#!/usr/bin/env bash
set -e

cd ~/telegram_reminder
git pull
mvn clean install -Dmaven.test.skip=true -Dremember.db.url=jdbc:h2:~/remember/db -Dremember.db.user=remember -Dremember.db.password=remember

cd ~/remember
cp ~/telegram_reminder/update.sh update.sh
chmod 700 update.sh
for pid in `cat pid`;
do
  kill -9 $pid || echo;
done
rm pid || echo
rm db.lock.db || echo
nohup /usr/bin/java -jar ~/telegram_reminder/target/remember-1.0-SNAPSHOT.jar com.solozobov.andrei.Application --spring.config.location=file:/root/remember/application.properties &
echo $! >> pid
