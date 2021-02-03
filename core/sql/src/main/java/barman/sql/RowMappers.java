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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

@SuppressWarnings("checkstyle:constantName")
public final class RowMappers
{
  /**
   * Maps the the value of the first column in the current row of a {@link java.sql.ResultSet} as an {@link Array}.
   */
  public static final RowMapper<Array> asArray = rs -> rs.getArray(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as a stream of ASCII characters.
   * <p>
   * The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large LONGVARCHAR values. The JDBC
   * driver will do any necessary conversion from the database format into ASCII.
   * <P>Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method
   * implicitly closes the stream. Also, a stream may return 0 when the method InputStream.available is called whether there is data available or
   * not.
   */
  public static final RowMapper<InputStream> asAsciiStream = rs -> rs.getAsciiStream(Select.FIRST_COLUMN);
  public static final RowMapper<BigDecimal> asBigDecimal = rs -> rs.getBigDecimal(Select.FIRST_COLUMN);
  public static final RowMapper<InputStream> asBinaryStream = rs -> rs.getBinaryStream(Select.FIRST_COLUMN);
  public static final RowMapper<Blob> asBlob = rs -> rs.getBlob(Select.FIRST_COLUMN);
  public static final RowMapper<Boolean> asBoolean = rs -> rs.getBoolean(Select.FIRST_COLUMN);
  public static final RowMapper<Byte> asByte = rs -> rs.getByte(Select.FIRST_COLUMN);
  public static final RowMapper<byte[]> asBytes = rs -> rs.getBytes(Select.FIRST_COLUMN);
  public static final RowMapper<Reader> asCharacterStream = rs -> rs.getCharacterStream(Select.FIRST_COLUMN);
  public static final RowMapper<Clob> asClob = rs -> rs.getClob(Select.FIRST_COLUMN);
  public static final RowMapper<Date> asDate = rs -> rs.getDate(Select.FIRST_COLUMN);
  public static final RowMapper<Double> asDouble = rs -> rs.getDouble(Select.FIRST_COLUMN);
  public static final RowMapper<Float> asFloat = rs -> rs.getFloat(Select.FIRST_COLUMN);
  public static final RowMapper<Integer> asInteger = rs -> rs.getInt(Select.FIRST_COLUMN);
  public static final RowMapper<Long> asLong = rs -> rs.getLong(Select.FIRST_COLUMN);
  public static final RowMapper<Reader> asNCharacterStream = rs -> rs.getNCharacterStream(Select.FIRST_COLUMN);
  public static final RowMapper<NClob> asNClob = rs -> rs.getNClob(Select.FIRST_COLUMN);
  public static final RowMapper<String> asNString = rs -> rs.getNString(Select.FIRST_COLUMN);
  public static final RowMapper<Short> asShort = rs -> rs.getShort(Select.FIRST_COLUMN);
  public static final RowMapper<SQLXML> asSqlXml = rs -> rs.getSQLXML(Select.FIRST_COLUMN);
  public static final RowMapper<String> asString = rs -> rs.getString(Select.FIRST_COLUMN);
  public static final RowMapper<Time> asTime = rs -> rs.getTime(Select.FIRST_COLUMN);
  public static final RowMapper<Timestamp> asTimestamp = rs -> rs.getTimestamp(Select.FIRST_COLUMN);
  public static final RowMapper<URL> asUrl = rs -> rs.getURL(Select.FIRST_COLUMN);

  private RowMappers()
  {
    throw new UnsupportedOperationException();
  }
}
