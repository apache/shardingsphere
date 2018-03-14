/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.core.constant.ShardingConstant;
import io.shardingjdbc.server.DataSourceManager;
import io.shardingjdbc.server.constant.ColumnType;
import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.ok.EofPacket;
import io.shardingjdbc.server.packet.ok.ErrPacket;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * COM_FIELD_LIST command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-field-list.html">COM_FIELD_LIST</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComFieldListPacket extends AbstractCommandPacket {
    
    private String table;
    
    private String fieldWildcard;
    
    @Override
    public ComFieldListPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        table = mysqlPacketPayload.readStringNul();
        fieldWildcard = mysqlPacketPayload.readStringEOF();
        log.debug("table name received for Sharding-JDBC-server: {}", table);
        log.debug("field wildcard received for Sharding-JDBC-server: {}", fieldWildcard);
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        String sql = String.format("SHOW COLUMNS FROM %s FROM %s", table, ShardingConstant.LOGIC_SCHEMA_NAME);
        List<AbstractMySQLSentPacket> result = new LinkedList<>();
        int currentSequenceId = getSequenceId();
        try (
                Connection conn = DataSourceManager.getInstance().getDataSource().getConnection();
                Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String field = resultSet.getString(1);
                String type = resultSet.getString(2);
                int columnLength = Integer.parseInt(type.substring(type.indexOf('(', type.indexOf(')'))));
                ColumnType columnType = ColumnType.valueOfDescription(type.substring(0, type.indexOf('(')));
                result.add(new ColumnDefinition41Packet(++currentSequenceId, ShardingConstant.LOGIC_SCHEMA_NAME, table, table, field, field, columnLength, columnType, 0));
            }
            result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
            return result;
        } catch (final SQLException ex) {
            result.add(new ErrPacket(++currentSequenceId, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
            return result;
        } catch (final Exception ex) {
            if (ex.getCause() instanceof SQLException) {
                SQLException cause = (SQLException) ex.getCause();
                result.add(new ErrPacket(++currentSequenceId, cause.getErrorCode(), "", cause.getSQLState(), cause.getMessage()));
            } else {
                // TODO standard ShardingJdbcException
                result.add(new ErrPacket(++currentSequenceId, 99, "", "unknown", ex.getMessage()));
            }
            return result;
        }
    }
}
