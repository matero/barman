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

/** Sample of a SELECT querying for objects with MORE THAN ONE parameter. */
final class OffensiveInners
    extends SelectList
{
  private int playmaking;
  private boolean playmakingSet;
  private int passing;
  private boolean passingSet;

  /**
   * Constructs an instance of {@link OffensiveInners}.
   */
  OffensiveInners(final Configuration configuration)
  {
    super("SELECT playerId, name FROM Players WHERE playmaking >= ? and passing >= ?", configuration);
  }

  OffensiveInners playmaking(final int value)
  {
    playmaking = value;
    playmakingSet = true;
    return this;
  }

  OffensiveInners passing(final int value)
  {
    passing = value;
    passingSet = true;
    return this;
  }

  @Override protected void setParametersTo(final PreparedStatement ps) throws SQLException
  {
    if (!playmakingSet) {
      throw new IllegalStateException("'playmaking' was not set");
    }
    if (!passingSet) {
      throw new IllegalStateException("'passing' was not set");
    }
    ps.setLong(1, playmaking);
    ps.setLong(2, passing);
  }

  @Override protected boolean hasParameters()
  {
    return true;
  }

  @Override protected void appendParametersDescriptionTo(final StringBuilder msg)
  {
    msg.append("playmaking: ").append(playmaking).append(", passing: ").append(passing);
  }
}
