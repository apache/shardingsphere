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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.shardingproxy.backend.BackendHandler;
import io.shardingsphere.shardingproxy.backend.BackendHandlerFactory;
import io.shardingsphere.shardingproxy.backend.ResultPacket;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryCommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.TextResultSetRowPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.spi.transaction.ShardingTransactionHandler;
import io.shardingsphere.spi.transaction.ShardingTransactionHandlerRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * COM_QUERY command packet.
 *
 * @author zhangliang
 * @author linjiaqi
 * @author zhaojun
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 */
@Slf4j
public final class ComQueryPacket implements QueryCommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final String sql;
    
    private final BackendHandler backendHandler;
    
    private final ShardingTransactionHandler<ShardingTransactionEvent> shardingTransactionHandler;
    
    private final TransactionType transactionType;
    
    public ComQueryPacket(final int sequenceId, final int connectionId, final MySQLPacketPayload payload, final BackendConnection backendConnection, final FrontendHandler frontendHandler) {
        this.sequenceId = sequenceId;
        sql = payload.readStringEOF();
        backendHandler = BackendHandlerFactory.createBackendHandler(connectionId, sequenceId, sql, backendConnection, DatabaseType.MySQL, frontendHandler);
        transactionType = GlobalRegistry.getInstance().getTransactionType();
        shardingTransactionHandler = ShardingTransactionHandlerRegistry.getInstance().getHandler(transactionType);
        if (null != transactionType && transactionType != TransactionType.LOCAL) {
            Preconditions.checkNotNull(shardingTransactionHandler, String.format("Cannot find transaction manager of [%s]", transactionType));
        }
    }
    
    public ComQueryPacket(final int sequenceId, final String sql) {
        this.sequenceId = sequenceId;
        this.sql = sql;
        transactionType = GlobalRegistry.getInstance().getTransactionType();
        backendHandler = null;
        shardingTransactionHandler = null;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_QUERY.getValue());
        payload.writeStringEOF(sql);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_QUERY received for Sharding-Proxy: {}", sql);
        if (GlobalRegistry.getInstance().isCircuitBreak()) {
            return Optional.of(new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_CIRCUIT_BREAK_MODE)));
        }
        Optional<TransactionOperationType> operationType = TransactionOperationType.getOperationType(sql);
        if (!operationType.isPresent()) {
            return Optional.of(backendHandler.execute());
        }
        if (TransactionType.XA == transactionType) {
            shardingTransactionHandler.doInTransaction(new XATransactionEvent(operationType.get()));
        }
        // TODO :zhaojun do not send TCL to backend, send when local transaction ready
        return Optional.of(new CommandResponsePackets(new OKPacket(1)));
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
