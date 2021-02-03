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
import java.sql.SQLException;

/** Sample of a SELECT querying for an object with ONE parameter. */
final class PlayerName
    extends SelectObject
{
  private PlayerId playerId;

  /**
   * Constructs an instance of {@link PlayerName}.
   */
  PlayerName()
  {
    super("SELECT name FROM Players WHERE id = ?", Configuration.none());
  }

  PlayerName playerId(final PlayerId value)
  {
    playerId = value;
    return this;
  }

  @Override protected void setParametersTo(final PreparedStatement ps) throws SQLException
  {
    if (playerId == null) {
      throw new IllegalStateException("'playerId' was not set");
    }
    playerId.set(ps, 1);
  }

  @Override protected boolean hasParameters()
  {
    return true;
  }

  @Override protected void appendParametersDescriptionTo(final StringBuilder msg)
  {
    msg.append("playerId: ").append(playerId);
  }
}
