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
import java.util.List;

class PlayersRepository
    extends Repository
{
  int countAll(final Connection connection) throws SQLException
  {
    return CountPlayers.get().query(connection);
  }

  String getPlayerName(
      final Connection connection,
      final PlayerId playerId) throws SQLException
  {
    return new PlayerName().playerId(playerId).query(connection, asString);
  }

  List<PlayerInfo> findOfensiveInners(
      final Connection connection,
      final SqlStatement.Configuration configuration,
      int playmaking,
      final int passing) throws SQLException
  {
    return new OffensiveInners(configuration)
               .playmaking(playmaking)
               .passing(passing)
               .query(connection, PlayerInfo::at);
  }
}
