package com.solozobov.andrei;

import com.solozobov.andrei.utils.LoopThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static com.solozobov.andrei.utils.Transactions.executeSqlInTransaction;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

/**
 * solozobov on 23.07.2019
 */
@Component
public class DatabaseBackuper {

  @Autowired
  public DatabaseBackuper(
      @Value("${remember.working.folder}") String workingFolder,
      DataSource dataSource
  ) {
    new LoopThread(
        this.getClass().getSimpleName(),
        () -> {
          final ZonedDateTime now = ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(3)));
          final String backupFile = workingFolder + "/backup_" + now.format(ISO_OFFSET_DATE_TIME) + ".zip";
          executeSqlInTransaction(() -> "database dump", dataSource, "BACKUP TO '" + backupFile + "'");
        },
        TimeUnit.MINUTES.toMillis(5)
    ).start();
  }
}
