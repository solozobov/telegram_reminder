package com.solozobov.andrei.utils;

import com.solozobov.andrei.RememberException;
import com.solozobov.andrei.bot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

/**
 * solozobov on 24.10.2019
 */
public class Transactions {
  private static final Logger LOG = LoggerFactory.getLogger(TelegramBot.class);

  public interface ConnectionUser {
    void use(Connection connection) throws Exception;
  }

  public interface ProducingConnectionUser<R> {
    R use(Connection connection) throws Exception;
  }

  public static void executeSqlInTransaction(Supplier<String> description, DataSource dataSource, String sql) {
    inTransaction(description, dataSource, connection -> {
      try {
        executeSql(connection, sql);
      } catch (Exception e) {
        throw new RememberException(e, "failed SQL '" + sql + "'");
      }
    });
  }

  public static void executeSql(Connection connection, String sql) throws SQLException {
    try (final Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  public static void inTransaction(Supplier<String> description, DataSource dataSource, ConnectionUser user) {
    inTransaction(description, dataSource, connection -> {user.use(connection); return null;});
  }

  public static <R> R inTransaction(Supplier<String> description, DataSource dataSource, ProducingConnectionUser<R> user) {
    Connection connection = null;
    Boolean originalAutoCommit = null;
    Integer originalTransactionIsolation = null;
    try {
      connection = dataSource.getConnection();
      originalAutoCommit = connection.getAutoCommit();
      originalTransactionIsolation = connection.getTransactionIsolation();

      connection.setAutoCommit(false);
      connection.setTransactionIsolation(TRANSACTION_READ_COMMITTED);

      final R result = user.use(connection);

      connection.commit();
      return result;
    } catch (Exception e) {
      if (connection != null) {
        try {
          connection.rollback();
        } catch (SQLException e1) {
          LOG.warn("Failed rolling back transaction after '" + description + "'", e1);
        }
      }

      throw new RuntimeException(e);
    } finally {
      if (connection != null) {
        if (originalAutoCommit != null) {
          try {
            connection.setAutoCommit(originalAutoCommit);
          } catch (SQLException e) {
            LOG.warn("Failed setting back afterCommit mode after '" + description + "'", e);
          }
          if (originalTransactionIsolation != null) {
            try {
              connection.setTransactionIsolation(originalTransactionIsolation);
            } catch (SQLException e) {
              LOG.warn("Failed setting back transactionIsolation mode after '" + description + "'", e);
            }
          }
        }

        try {
          connection.close();
        } catch (SQLException e) {
          LOG.error("Failed closing database connection after '" + description + "'", e);
        }
      }
    }
  }
}
