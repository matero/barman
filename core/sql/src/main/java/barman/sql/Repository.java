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

/**
 * Repository against {@code JDBC} interfaces. Defined to provided a cleaner code with a DSL, not really necessary as it doesnt manage any resource.
 */
@SuppressWarnings("checkstyle:constantName")
public abstract class Repository
{
  /**
   * Retrieves the the value of the first column in the current row of a {@link java.sql.ResultSet} as an {@link Array}.
   */
  protected static final RowMapper<Array> asArray = rs -> rs.getArray(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as a stream of {@code ASCII} characters.
   * <p>
   * The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large {@code LONGVARCHAR} values. The
   * {@code JDBC} driver will do any necessary conversion from the database format into {@code ASCII}.
   * <p>
   * Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method
   * implicitly closes the stream. Also, a stream may return 0 when the method {@link InputStream#available()} is called whether there is data
   * available or not.
   */
  protected static final RowMapper<InputStream> asAsciiStream = rs -> rs.getAsciiStream(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as a {@link BigDecimal} with full precision.
   */
  protected static final RowMapper<BigDecimal> asBigDecimal = rs -> rs.getBigDecimal(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as a stream of uninterpreted bytes.
   * <p>
   * The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large {@code LONGVARBINARY} values.
   * <p>
   * Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method
   * implicitly closes the stream. Also, a stream may return 0 when the method {@link InputStream#available()} is called whether there is data
   * available or not.
   */
  protected static final RowMapper<InputStream> asBinaryStream = rs -> rs.getBinaryStream(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Blob}.
   */
  protected static final RowMapper<Blob> asBlob = rs -> rs.getBlob(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Boolean}.
   * <p>
   * If the column has a datatype of {@code CHAR} or {@code VARCHAR} and contains a {@literal "0"} or has a datatype of {@code BIT}, {@code TINYINT},
   * {@code SMALLINT}, {@code INTEGER} or {@code BIGINT} and contains a {@literal 0}, a value of {@literal false} is returned.
   * <p>
   * If the column has a datatype of {@code CHAR} or {@code VARCHAR} and contains a {@literal "1"} or has a datatype of {@code BIT}, {@code TINYINT},
   * {@code SMALLINT}, {@code INTEGER} or {@code BIGINT} and contains a {@literal 1}, a value of {@literal true} is returned.
   */
  protected static final RowMapper<Boolean> asBoolean = rs -> rs.getBoolean(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Byte}.
   */
  protected static final RowMapper<Byte> asByte = rs -> rs.getByte(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as a byte array.
   * <p>
   * The bytes represent the raw values returned by the driver..
   */
  protected static final RowMapper<byte[]> asBytes = rs -> rs.getBytes(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Reader}.
   */
  protected static final RowMapper<Reader> asCharacterStream = rs -> rs.getCharacterStream(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Clob}.
   */
  protected static final RowMapper<Clob> asClob = rs -> rs.getClob(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Date}.
   */
  protected static final RowMapper<Date> asDate = rs -> rs.getDate(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Double}.
   */
  protected static final RowMapper<Double> asDouble = rs -> rs.getDouble(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Float}.
   */
  protected static final RowMapper<Float> asFloat = rs -> rs.getFloat(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Integer}.
   */
  protected static final RowMapper<Integer> asInteger = rs -> rs.getInt(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Long}.
   */
  protected static final RowMapper<Long> asLong = rs -> rs.getLong(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Reader}, it is intended for use when
   * accessing {@code NCHAR}, {@code NVARCHAR} and {@code LONGNVARCHAR} columns.
   */
  protected static final RowMapper<Reader> asNCharacterStream = rs -> rs.getNCharacterStream(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link NClob}.
   */
  protected static final RowMapper<NClob> asNClob = rs -> rs.getNClob(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link String}, it is intended for use when
   * accessing {@code NCHAR}, {@code NVARCHAR} and {@code LONGNVARCHAR} columns..
   */
  protected static final RowMapper<String> asNString = rs -> rs.getNString(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Short}.
   */
  protected static final RowMapper<Short> asShort = rs -> rs.getShort(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link SQLXML}.
   */
  protected static final RowMapper<SQLXML> asSqlXml = rs -> rs.getSQLXML(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link String}.
   */
  protected static final RowMapper<String> asString = rs -> rs.getString(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Time}.
   */
  protected static final RowMapper<Time> asTime = rs -> rs.getTime(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link Timestamp}.
   */
  protected static final RowMapper<Timestamp> asTimestamp = rs -> rs.getTimestamp(Select.FIRST_COLUMN);
  /**
   * Retrieves the value of the first column in the current row of a {@link java.sql.ResultSet} as {@link URL}.
   */
  protected static final RowMapper<URL> asUrl = rs -> rs.getURL(Select.FIRST_COLUMN);

  /** avoids unnamed {@link Repository} class instantiation. */
  protected Repository()
  {
    // nothing to do
  }
}
