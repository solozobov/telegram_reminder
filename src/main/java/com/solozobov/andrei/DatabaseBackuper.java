package com.solozobov.andrei;

import com.solozobov.andrei.utils.LoopThread;
import org.h2.tools.Backup;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * solozobov on 23.07.2019
 */
@Component
public class DatabaseBackuper {

  public DatabaseBackuper() {
    new LoopThread(
        this.getClass().getSimpleName(),
        () -> {
          try {
            // delete old backup
            // connection.prepeareStatement("BACKUP TO 'backup.zip'")
            Backup.execute("", "", "", false);
          } catch (SQLException e) {
            throw new RemindException(e, "Database backup failed");
          }
        },
        TimeUnit.MINUTES.toMillis(5)
    ).start();
  }
}
