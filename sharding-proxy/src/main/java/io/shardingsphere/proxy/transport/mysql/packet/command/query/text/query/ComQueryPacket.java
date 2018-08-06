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

package io.shardingsphere.proxy.transport.mysql.packet.command.query.text.query;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.proxy.backend.BackendHandler;
import io.shardingsphere.proxy.backend.BackendHandlerFactory;
import io.shardingsphere.proxy.backend.ResultPacket;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.transaction.TransactionEngine;
import io.shardingsphere.proxy.backend.jdbc.transaction.TransactionEngineFactory;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.QueryCommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.text.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * COM_QUERY command packet.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 *
 * @author zhangliang
 * @author linjiaqi
 * @author zhaojun
 */
@Slf4j
public final class ComQueryPacket implements QueryCommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final String sql;
    
    private final BackendHandler backendHandler;
    
    private final TransactionEngine transactionEngine;
    
    public ComQueryPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        sql = payload.readStringEOF();
        backendHandler = BackendHandlerFactory.newTextProtocolInstance(connectionId, sequenceId, sql, backendConnection, DatabaseType.MySQL);
        transactionEngine = TransactionEngineFactory.create(sql);
    }
    
    public ComQueryPacket(final int sequenceId, final String sql) {
        this.sequenceId = sequenceId;
        this.sql = sql;
        backendHandler = null;
        transactionEngine = null;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_QUERY.getValue());
        payload.writeStringEOF(sql);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() throws SQLException {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
        return Optional.of(transactionEngine.execute() ? new CommandResponsePackets(new OKPacket(1)) : backendHandler.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return backendHandler.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = backendHandler.getResultValue();
        return new TextResultSetRowPacket(resultPacket.getSequenceId(), resultPacket.getData());
    }
}
