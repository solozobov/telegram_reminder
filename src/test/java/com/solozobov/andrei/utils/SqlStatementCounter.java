package com.solozobov.andrei.utils;

import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

/**
 * solozobov on 27.11.2019
 */
@SuppressWarnings("unused")
public final class SqlStatementCounter {

  private static final Logger LOG = LoggerFactory.getLogger(SqlStatementCounter.class);

  private SqlStatementCounter() {
    throw new UnsupportedOperationException();
  }

  public static void reset() {
    QueryCountHolder.clear();
    LOG.info("reset");
  }

  public static QueryCount getCount() {
    return QueryCountHolder.getGrandTotal();
  }

  public static void assertCount(int select, int update, int insert, int delete) {
    final QueryCount queryCount = QueryCountHolder.getGrandTotal();
    assertEquals(select, queryCount.getSelect(), "Unexpected number of generated SELECT SQL queries");
    assertEquals(update, queryCount.getUpdate(), "Unexpected number of generated UPDATE SQL queries");
    assertEquals(insert, queryCount.getInsert(), "Unexpected number of generated INSERT SQL queries");
    assertEquals(delete, queryCount.getDelete(), "Unexpected number of generated DELETE SQL queries");
  }

  public static void assertCallCount(int queriesCount) {
    final QueryCount queryCount = QueryCountHolder.getGrandTotal();
    assertEquals(queriesCount, queryCount.getCall(), "Unexpected number of generated SQL queries");
  }

  private static void assertEquals(int expected, int actual, @NotNull String errorMessage) {
    if (expected != actual) {
      fail(errorMessage + " expected " + expected + " but got " + actual);
    }
  }

  public static void logStats() {
    final QueryCount queryCount = QueryCountHolder.getGrandTotal();
    LOG.debug("Inserts = {} / Updates = {} / Deletes = {} / Selects = {} / Others = {}\n\tTotal = {} / "
            + "Failures = {} / Calls = {} / ET = {}", queryCount.getInsert(), queryCount.getUpdate(),
        queryCount.getDelete(), queryCount.getSelect(), queryCount.getOther(), queryCount.getTotalNumOfQuery(),
        queryCount.getFailure(), queryCount.getCall(), queryCount.getElapsedTime());
  }

}
