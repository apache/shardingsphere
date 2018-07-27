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
import io.shardingsphere.proxy.backend.BackendHandler;
import io.shardingsphere.proxy.backend.BackendHandlerFactory;
import io.shardingsphere.proxy.backend.jdbc.BackendConnection;
import io.shardingsphere.proxy.transport.common.packet.CommandPacketRebuilder;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.QueryCommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.binary.close.DummyPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ComQueryPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * COM_FIELD_LIST command packet.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-field-list.html">COM_FIELD_LIST</a>
 * 
 * @author zhangliang
 * @author wangkai
 */
@Slf4j
public final class ComFieldListPacket implements QueryCommandPacket, CommandPacketRebuilder {
    
    @Getter
    private final int sequenceId;
    
    private final int connectionId;
    
    private final String table;
    
    private final String fieldWildcard;
    
    private final BackendHandler backendHandler;
    
    private int currentSequenceId;
    
    public ComFieldListPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        this.connectionId = connectionId;
        table = payload.readStringNul();
        fieldWildcard = payload.readStringEOF();
        backendHandler = BackendHandlerFactory.newTextProtocolInstance(sql(), backendConnection, DatabaseType.MySQL, this);
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_FIELD_LIST.getValue());
        payload.writeStringNul(table);
        payload.writeStringEOF(fieldWildcard);
    }
    
    @Override
    public CommandResponsePackets execute() {
        log.debug("Table name received for Sharding-Proxy: {}", table);
        log.debug("Field wildcard received for Sharding-Proxy: {}", fieldWildcard);
        DatabasePacket headPacket = backendHandler.execute().getHeadPacket();
        return headPacket instanceof ErrPacket ? new CommandResponsePackets(headPacket) : new CommandResponsePackets(new DummyPacket());
    }
    
    @Override
    public boolean next() throws SQLException {
        return backendHandler.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        TextResultSetRowPacket fieldListResponse = (TextResultSetRowPacket) backendHandler.getResultValue();
        String columnName = (String) fieldListResponse.getData().get(0);
        return new ColumnDefinition41Packet(++currentSequenceId, ShardingConstant.LOGIC_SCHEMA_NAME, table, table, columnName, columnName, 100, ColumnType.MYSQL_TYPE_VARCHAR, 0);
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
