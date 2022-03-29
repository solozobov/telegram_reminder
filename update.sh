#!/usr/bin/env bash
set -e

work_dir=`pwd`
cd telegram_reminder
git pull
mvn clean install -Dmaven.test.skip=true -Dremember.db.url=jdbc:h2:$work_dir/db -Dremember.db.user=remember -Dremember.db.password=remember

cd ..
cp telegram_reminder/update.sh update.sh
chmod 700 update.sh
for pid in `cat pid`;
do
  kill -9 $pid || echo;
done
rm pid || echo
rm db.lock.db || echo
nohup /usr/bin/java -jar $work_dir/telegram_reminder/target/remember-1.0-SNAPSHOT.jar com.solozobov.andrei.Application --spring.config.location=file:$work_dir/application.properties &
echo $! >> pid
