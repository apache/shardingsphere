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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.backend.BackendHandler;
import io.shardingsphere.shardingproxy.backend.BackendHandlerFactory;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import lombok.Getter;
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
public final class ComFieldListPacket implements CommandPacket {
    
    private static final String SQL = "SHOW COLUMNS FROM %s FROM %s";
    
    @Getter
    private final int sequenceId;
    
    private final String schemaName;
    
    private final String table;
    
    private final String fieldWildcard;
    
    private final BackendHandler backendHandler;
    
    public ComFieldListPacket(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        this.schemaName = backendConnection.getSchemaName();
        table = payload.readStringNul();
        fieldWildcard = payload.readStringEOF();
        backendHandler = BackendHandlerFactory.getInstance().newTextProtocolInstance(sequenceId, String.format(SQL, table, schemaName), backendConnection, DatabaseType.MySQL);
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_FIELD_LIST.getValue());
        payload.writeStringNul(table);
        payload.writeStringEOF(fieldWildcard);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() throws SQLException {
        log.debug("Table name received for Sharding-Proxy: {}", table);
        log.debug("Field wildcard received for Sharding-Proxy: {}", fieldWildcard);
        CommandResponsePackets responsePackets = backendHandler.execute();
        return Optional.of(responsePackets.getHeadPacket() instanceof ErrPacket ? responsePackets : getColumnDefinition41Packets());
    }
    
    private CommandResponsePackets getColumnDefinition41Packets() throws SQLException {
        CommandResponsePackets result = new CommandResponsePackets();
        int currentSequenceId = 0;
        while (backendHandler.next()) {
            String columnName = backendHandler.getResultValue().getData().get(0).toString();
            result.getPackets().add(new ColumnDefinition41Packet(++currentSequenceId, schemaName, table, table, columnName, columnName, 100, ColumnType.MYSQL_TYPE_VARCHAR, 0));
        }
        result.getPackets().add(new EofPacket(++currentSequenceId));
        return result;
    }
}
