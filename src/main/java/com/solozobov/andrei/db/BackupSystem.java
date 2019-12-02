package com.solozobov.andrei.db;

import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.utils.LoopThread;
import com.solozobov.andrei.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static com.solozobov.andrei.SettingKeys.BACKUP_CHAT_ID;
import static com.solozobov.andrei.utils.Transactions.executeSqlInTransaction;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * solozobov on 23.07.2019
 */
@Component
public class BackupSystem {
  // Yandex application https://oauth.yandex.ru/client/8e998165215546628c15c1acdffac912
  // Obtain token https://oauth.yandex.ru/authorize?response_type=token&client_id=8e998165215546628c15c1acdffac912

  private static final Logger LOG = LoggerFactory.getLogger(BackupSystem.class);

  private static final String BACKUP_FILE_NAME_PREFIX = "backup_";
  private static final DateTimeFormatter BACKUP_FILE_NAME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
  private static final String BACKUP_FILE_NAME_SUFFIX = ".zip";

  private final DataSource dataSource;
  private final TelegramBot bot;
  private final SettingSystem settingSystem;
  private final File backupFolder;
  private final LinkedList<File> backupsReplicationQueue;

  @Autowired
  public BackupSystem(
      DataSource dataSource,
      TelegramBot bot,
      SettingSystem settingSystem,
      @Value("${remember.working.folder}") String workingFolder,
      @Value("${remember.backup.interval.minutes}") long backupIntervalMinutes,
      @Value("${remember.backup.enabled}") boolean enabled
  ) {
    this.dataSource = dataSource;
    this.bot = bot;
    this.backupFolder = new File(workingFolder, "db_backups");
    this.settingSystem = settingSystem;
    if (backupFolder.exists() && !backupFolder.isDirectory()) {
      //noinspection ResultOfMethodCallIgnored
      backupFolder.delete();
    }

    if (!backupFolder.exists()) {
      //noinspection ResultOfMethodCallIgnored
      backupFolder.mkdir();
    }
    //noinspection ConstantConditions
    this.backupsReplicationQueue = stream(backupFolder
        .listFiles(this::isBackupFile))
        .map(f -> Pair.of(parseBackupFileName(f), f))
        .sorted(comparing(p -> p.first))
        .map(Pair::second)
        .collect(Collectors.toCollection(LinkedList::new));

    if (enabled) {
      new LoopThread("database_backup", this::makeBackup, MINUTES.toMillis(backupIntervalMinutes)).start();
    }
  }

  private void makeBackup() {
    final ZonedDateTime now = ZonedDateTime.now(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(3)));
    final String backupFile = backupFolder.getAbsolutePath() + "/" + BACKUP_FILE_NAME_PREFIX + now.format(BACKUP_FILE_NAME_FORMAT) + BACKUP_FILE_NAME_SUFFIX;
    executeSqlInTransaction(() -> "database dump", dataSource, "BACKUP TO '" + backupFile + "'");
    backupsReplicationQueue.add(new File(backupFile));

    final Long backupChatId = settingSystem.get(BACKUP_CHAT_ID);
    while(backupChatId != null && backupsReplicationQueue.size() > 0 && replicateBackup(backupChatId, backupsReplicationQueue.peekFirst())) {
      //noinspection ResultOfMethodCallIgnored
      backupsReplicationQueue.removeFirst().delete();
    }
  }

  private boolean replicateBackup(long backupChatId, File backup) {
    try {
      bot.sendFile(backupChatId, backup);
      return true;
    } catch (Exception e) {
      LOG.error("Failed to replicate backup '" + backup.getAbsolutePath() + "' to chat #" + backupChatId);
      return false;
    }
  }

  private boolean isBackupFile(File file) {
    if (!file.isFile()) {
      return false;
    }

    try {
      parseBackupFileName(file);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private ZonedDateTime parseBackupFileName(File file) {
    final String fileName = file.getName();
    return ZonedDateTime.parse(
        fileName.substring(BACKUP_FILE_NAME_PREFIX.length(), fileName.length() - BACKUP_FILE_NAME_SUFFIX.length()),
        BACKUP_FILE_NAME_FORMAT
    );
  }
}
