/*
The MIT License

Copyright (c) 2021 Juan J. GIL (matero _at_ gmail _dot_ com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package barman.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** The base class for QueryRunner &amp; AsyncQueryRunner. This class is thread safe. */
public abstract class Statement
{
  /** The represented statement, defined as JDBC query string (with '?' for parameters). */
  private final String sql;

  /**
   * Configuration to use when preparing statements.
   */
  private final Configuration configuration;

  /**
   * Constructs an instance of {@link Statement} with its sql and configuration.
   *
   * @param statement              sql statement to be specified by the instance.
   * @param statementConfiguration how to configure related JDBC prepared statements.
   * @throws NullPointerException     if {@code statement} is {@literal null}.
   * @throws IllegalArgumentException if {@code statement} is {@code empty} or {@code blank}.
   */
  protected Statement(
      final String statement,
      final Configuration statementConfiguration)
  {
    if (statement == null) {
      throw new NullPointerException("statement");
    }
    if (statement.isEmpty()) {
      throw new IllegalArgumentException("statement is empty");
    }
    if (statement.isBlank()) {
      throw new IllegalArgumentException("statement is blank");
    }
    sql = statement;
    configuration = statementConfiguration == null ? Configuration.none() : statementConfiguration;
  }

  private static String notNullMessageOf(final Exception e)
  {
    if (e.getMessage() == null) {
      return "";
    }
    return e.getMessage();
  }

  /**
   * Wrap the {@code ResultSet} in a decorator before processing it. This implementation returns the {@code ResultSet} it is given without any
   * decoration.
   *
   * <p>
   * Often, the implementation of this method can be done in an anonymous inner class like this:
   * </p>
   *
   * <pre>
   * QueryRunner run = new QueryRunner() {
   * protected ResultSet wrap(ResultSet rs) {
   * return StringTrimmedResultSet.wrap(rs);
   * }
   * };
   * </pre>
   *
   * @param rs The {@code ResultSet} to decorate; never {@code null}.
   * @return The {@code ResultSet} wrapped in some decorator.
   */
  protected static ResultSet wrap(final ResultSet rs)
  {
    return rs;
  }

  private void configureStatement(final java.sql.Statement stmt) throws SQLException
  {
    configuration.configureStatement(stmt);
  }

  /**
   * Initializes a {@code PreparedStatement} object for the given SQL.
   * <p>
   * Subclasses can override this method to provide special PreparedStatement configuration if needed.
   *
   * @param connection The {@code Connection} used to create the {@code PreparedStatement}
   * @return An initialized {@code PreparedStatement}.
   * @throws SQLException if a database access error occurs
   */
  protected PreparedStatement prepareStatement(final Connection connection) throws SQLException
  {
    final var ps = connection.prepareStatement(sql);
    try {
      configureStatement(ps);
    } catch (final SQLException e) {
      ps.close();
      throw e;
    }
    return ps;
  }

  /**
   * Throws a new exception with a more informative error message.
   *
   * @param e The original exception that will be chained to the new exception when it's rethrown.
   * @return a new {@link SQLException} with statement descriptive information.
   * @throws SQLException if a database access error occurs
   */
  protected SQLException withInformationAboutStatement(final Exception e) throws SQLException
  {
    if (e instanceof SQLException) {
      final var sqlEx = (SQLException) e;
      final var result = new SQLException(getInformativeErrorMessageFor(e), sqlEx.getSQLState(), sqlEx.getErrorCode());
      result.setNextException(sqlEx);
      return result;
    } else {
      return new SQLException(getInformativeErrorMessageFor(e));
    }
  }

  protected String getInformativeErrorMessageFor(final Exception cause)
  {
    final var msg = new StringBuilder();
    {
      final var causeMessage = cause.getMessage();
      if (causeMessage != null && !causeMessage.isEmpty() && !causeMessage.isBlank()) {
        msg.append(causeMessage).append(". Query: '");
      } else {
        msg.append("Query: '");
      }
    }
    msg.append(sql);
    msg.append("', Parameters: [");
    if (hasParameters()) {
      appendParametersDescriptionTo(msg);
    }
    return msg.append(']').toString();
  }

  protected abstract boolean hasParameters();

  protected abstract void appendParametersDescriptionTo(StringBuilder msg);

  private enum NoConfiguration
      implements Configuration
  {
    INSTANCE;

    @Override public void configureStatement(final java.sql.Statement stmt)
    {
      // nothing to configure
    }
  }

  public interface Configuration
  {
    static Configuration none()
    {
      return NoConfiguration.INSTANCE;
    }

    /**
     * @param value The direction for fetching rows from database tables.
     * @return A configuration {@link Builder} for with  fetch direction defined.
     */
    static Builder withFetchDirection(final int value)
    {
      return new Builder().fetchDirection(value);
    }

    /**
     * @param value The maximum number of rows that a {@code ResultSet} can produce.
     * @return A configuration {@link Builder} for with  max rows defined.
     */
    static Builder maxRows(final int value)
    {
      return new Builder().maxRows(value);
    }

    /**
     * @param value The number of seconds the driver will wait for execution.
     * @return A configuration {@link Builder} for with  query timeout defined.
     */
    static Builder queryTimeout(final int value)
    {
      return new Builder().queryTimeout(value);
    }

    /**
     * @param value The maximum number of bytes that can be returned for character and binary column values.
     * @return a configuration {@link Builder} with max field size defined.
     */
    static Builder maxFieldSize(final int value)
    {
      return new Builder().maxFieldSize(value);
    }

    void configureStatement(java.sql.Statement stmt) throws SQLException;

    /** Builder class for {@code StatementConfiguration} for more flexible construction. */
    final class Builder
    {
      private int fetchDirection = StatementConfiguration.UNDEFINED_PROPERTY;
      private int fetchSize = StatementConfiguration.UNDEFINED_PROPERTY;
      private int maxRows = StatementConfiguration.UNDEFINED_PROPERTY;
      private int queryTimeout = StatementConfiguration.UNDEFINED_PROPERTY;
      private int maxFieldSize = StatementConfiguration.UNDEFINED_PROPERTY;

      private Builder()
      {
        // nothing to do
      }

      /**
       * @param value The direction for fetching rows from database tables.
       * @return This builder for chaining.
       */
      public Builder fetchDirection(final int value)
      {
        fetchDirection = value;
        return this;
      }

      /**
       * @param value The number of rows that should be fetched from the database when more rows are needed.
       * @return This builder for chaining.
       */
      public Builder fetchSize(final int value)
      {
        fetchSize = value;
        return this;
      }

      /**
       * @param value The maximum number of rows that a {@code ResultSet} can produce.
       * @return This builder for chaining.
       */
      public Builder maxRows(final int value)
      {
        maxRows = value;
        return this;
      }

      /**
       * @param value The number of seconds the driver will wait for execution.
       * @return This builder for chaining.
       */
      public Builder queryTimeout(final int value)
      {
        queryTimeout = value;
        return this;
      }

      /**
       * @param value The maximum number of bytes that can be returned for character and binary column values.
       * @return This builder for chaining.
       */
      public Builder maxFieldSize(final int value)
      {
        maxFieldSize = value;
        return this;
      }

      /** @return A new and configured {@link Configuration}. */
      public Configuration build()
      {
        return new StatementConfiguration(fetchDirection, fetchSize, maxFieldSize, maxRows, queryTimeout);
      }
    }
  }

  /** Configuration options for a {@link java.sql.Statement} when preparing statements in {@code QueryRunner}. */
  private static final class StatementConfiguration
      implements Configuration
  {
    private static final int UNDEFINED_PROPERTY = Integer.MIN_VALUE;

    private final int fetchDirection;
    private final int fetchSize;
    private final int maxFieldSize;
    private final int maxRows;
    private final int queryTimeout;

    /**
     * Constructor for {@code StatementConfiguration}.  For more flexibility, use {@link Builder}.
     *
     * @param fetchDirection The direction for fetching rows from database tables.
     * @param fetchSize      The number of rows that should be fetched from the database when more rows are needed.
     * @param maxFieldSize   The maximum number of bytes that can be returned for character and binary column values.
     * @param maxRows        The maximum number of rows that a {@code ResultSet} can produce.
     * @param queryTimeout   The number of seconds the driver will wait for execution.
     */
    StatementConfiguration(
        final int fetchDirection,
        final int fetchSize,
        final int maxFieldSize,
        final int maxRows,
        final int queryTimeout)
    {
      this.fetchDirection = fetchDirection;
      this.fetchSize = fetchSize;
      this.maxFieldSize = maxFieldSize;
      this.maxRows = maxRows;
      this.queryTimeout = queryTimeout;
    }

    @Override public void configureStatement(final java.sql.Statement stmt) throws SQLException
    {
      if (fetchDirection != UNDEFINED_PROPERTY) {
        stmt.setFetchDirection(fetchDirection);
      }
      if (fetchSize != UNDEFINED_PROPERTY) {
        stmt.setFetchSize(fetchSize);
      }
      if (maxFieldSize != UNDEFINED_PROPERTY) {
        stmt.setMaxFieldSize(maxFieldSize);
      }
      if (maxRows != UNDEFINED_PROPERTY) {
        stmt.setMaxRows(maxRows);
      }
      if (queryTimeout != UNDEFINED_PROPERTY) {
        stmt.setQueryTimeout(queryTimeout);
      }
    }
  }
}
