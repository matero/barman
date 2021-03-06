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
import java.sql.SQLException;

/**
 * SELECT SQL queries which expects {@code ResultSet}s to hold EXACTLY one row, with a first column holding a NOT NULL {@code boolean}.
 */
public abstract class SelectBoolean
    extends Select
{

  /**
   * Constructs an instance of {@link SelectBoolean} with its sql statement and configuration.
   *
   * @param statement              sql statement to be specified by the instance.
   * @param statementConfiguration how to configure related JDBC prepared statements.
   * @throws NullPointerException     if {@code statement} is {@literal null}.
   * @throws IllegalArgumentException if {@code statement} is {@code empty} or {@code blank}.
   */
  protected SelectBoolean(
      final String statement,
      final Configuration statementConfiguration)
  {
    super(statement, statementConfiguration);
  }

  public boolean query(final Connection connection) throws SQLException
  {
    if (connection == null) {
      throw new NullPointerException("connection");
    }
    try (var select = prepareStatement(connection);
         var rs = wrap(select.executeQuery())) {
      if (!rs.next()) {
        throw withInformationAboutStatement(new IllegalStateException("no row fetched"));
      }
      if (rs.wasNull()) {
        throw withInformationAboutStatement(new IllegalStateException("NULL can't be interpreted as int"));
      }
      final var result = rs.getBoolean(FIRST_COLUMN);
      if (rs.next()) {
        throw withInformationAboutStatement(new IllegalStateException("more than one row fetched"));
      }
      return result;
    } catch (final SQLException e) {
      throw withInformationAboutStatement(e);
    }
  }
}
