/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.proxy.transport.mysql.packet.command.text.fieldlist;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.proxy.backend.common.SQLExecuteBackendHandler;
import io.shardingsphere.proxy.backend.common.SQLPacketsBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.CommandPacketRebuilder;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.close.DummyPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ComQueryPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * COM_FIELD_LIST command packet.
 *
 * @author zhangliang
 * @author wangkai
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-field-list.html">COM_FIELD_LIST</a>
 */
@Slf4j
public final class ComFieldListPacket extends CommandPacket implements CommandPacketRebuilder {
    
    private final int connectionId;
    
    private final String table;
    
    private final String fieldWildcard;
    
    private int currentSequenceId;
    
    private SQLExecuteBackendHandler sqlExecuteBackendHandler;
    
    private SQLPacketsBackendHandler sqlPacketsBackendHandler;
    
    public ComFieldListPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload mysqlPacketPayload) {
        super(sequenceId);
        this.connectionId = connectionId;
        table = mysqlPacketPayload.readStringNul();
        fieldWildcard = mysqlPacketPayload.readStringEOF();
    }
    
    public ComFieldListPacket(final int sequenceId, final int connectionId, final String table, final String fieldWildcard) {
        super(sequenceId);
        this.connectionId = connectionId;
        this.table = table;
        this.fieldWildcard = fieldWildcard;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(CommandPacketType.COM_FIELD_LIST.getValue());
        mysqlPacketPayload.writeStringNul(table);
        mysqlPacketPayload.writeStringEOF(fieldWildcard);
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("table name received for Sharding-Proxy: {}", table);
        log.debug("field wildcard received for Sharding-Proxy: {}", fieldWildcard);
        String sql = String.format("SHOW COLUMNS FROM %s FROM %s", table, ShardingConstant.LOGIC_SCHEMA_NAME);
        // TODO use common database type
        if (RuleRegistry.getInstance().isWithoutJdbc()) {
            sqlPacketsBackendHandler = new SQLPacketsBackendHandler(this, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
            sqlPacketsBackendHandler.execute();
            return new CommandResponsePackets(new DummyPacket());
        } else {
            sqlExecuteBackendHandler = new SQLExecuteBackendHandler(sql, DatabaseType.MySQL, RuleRegistry.getInstance().isShowSQL());
            sqlExecuteBackendHandler.execute();
            return new CommandResponsePackets(new DummyPacket());
        }
    }
    
    @Override
    public boolean hasMoreResultValue() {
        try {
            if (RuleRegistry.getInstance().isWithoutJdbc()) {
                return sqlPacketsBackendHandler.hasMoreResultValue();
            } else {
                return sqlExecuteBackendHandler.hasMoreResultValue();
            }
        } catch (final SQLException ex) {
            return false;
        }
    }
    
    @Override
    public DatabaseProtocolPacket getResultValue() {
        DatabaseProtocolPacket result;
        if (RuleRegistry.getInstance().isWithoutJdbc()) {
            result = sqlPacketsBackendHandler.getResultValue();
            if (!sqlPacketsBackendHandler.isHasMoreResultValueFlag()) {
                return new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
            }
        } else {
            result = sqlExecuteBackendHandler.getResultValue();
            if (!sqlExecuteBackendHandler.isHasMoreResultValueFlag()) {
                return new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
            }
        }
        if (result instanceof TextResultSetRowPacket) {
            TextResultSetRowPacket fieldListResponse = (TextResultSetRowPacket) result;
            String columnName = (String) fieldListResponse.getData().get(0);
            return new ColumnDefinition41Packet(++currentSequenceId, ShardingConstant.LOGIC_SCHEMA_NAME, table, table,
                columnName, columnName, 100, ColumnType.MYSQL_TYPE_VARCHAR, 0);
        } else {
            return new ErrPacket(1, 0, "", "", "");
        }
    }
    
    @Override
    public int connectionId() {
        return connectionId;
    }
    
    @Override
    public int sequenceId() {
        return getSequenceId();
    }
    
    @Override
    public String sql() {
        return String.format("SHOW COLUMNS FROM %s FROM %s", table, ShardingConstant.LOGIC_SCHEMA_NAME);
    }
    
    @Override
    public CommandPacket rebuild(final Object... params) {
        return new ComQueryPacket((int) params[0], (int) params[1], (String) params[2]);
    }
}
