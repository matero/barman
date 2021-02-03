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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class Name
    extends StringValue<Name>
{
  Name(final String value)
  {
    super(value);
  }

  @Override public String toString()
  {
    return "Name(" + value + ')';
  }

  @Override public int compareTo(final Name other)
  {
    return value.compareTo(other.value);
  }

  static Name name(final String value)
  {
    if (value == null) {
      throw new NullPointerException("value");
    }
    return new Name(value);
  }

  static Name at(
      final ResultSet rs,
      final int index) throws SQLException
  {
    final var value = rs.getString(index);
    if (rs.wasNull()) {
      throw new IllegalStateException("Name NULL at column#" + index);
    }
    return new Name(value);
  }

  @Override public void set(
      final PreparedStatement statement,
      final int columnIndex) throws SQLException
  {
    statement.setString(columnIndex, value);
  }
}
