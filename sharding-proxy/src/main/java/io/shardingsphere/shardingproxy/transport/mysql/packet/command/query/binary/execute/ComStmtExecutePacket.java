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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.backend.BackendHandler;
import io.shardingsphere.shardingproxy.backend.BackendHandlerFactory;
import io.shardingsphere.shardingproxy.backend.ResultPacket;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.constant.NewParametersBoundFlag;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryCommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatement;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementParameterType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.BinaryProtocolValue;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.BinaryProtocolValueFactory;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * COM_STMT_EXECUTE command packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-execute.html">COM_STMT_EXECUTE</a>
 *
 * @author zhangyonglun
 */
@Slf4j
public final class ComStmtExecutePacket implements QueryCommandPacket {
    
    private static final int ITERATION_COUNT = 1;
    
    private static final int NULL_BITMAP_OFFSET = 0;
    
    @Getter
    private final int sequenceId;
    
    private final int statementId;
    
    private final BinaryStatement binaryStatement;
    
    private final int flags;
    
    private final NullBitmap nullBitmap;
    
    private final NewParametersBoundFlag newParametersBoundFlag;
    
    private final List<Object> parameters;
    
    private final BackendHandler backendHandler;
    
    public ComStmtExecutePacket(
            final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        this.sequenceId = sequenceId;
        statementId = payload.readInt4();
        binaryStatement = BinaryStatementRegistry.getInstance().getBinaryStatement(statementId);
        flags = payload.readInt1();
        Preconditions.checkArgument(ITERATION_COUNT == payload.readInt4());
        int parametersCount = binaryStatement.getParametersCount();
        nullBitmap = new NullBitmap(parametersCount, NULL_BITMAP_OFFSET);
        for (int i = 0; i < nullBitmap.getNullBitmap().length; i++) {
            nullBitmap.getNullBitmap()[i] = payload.readInt1();
        }
        newParametersBoundFlag = NewParametersBoundFlag.valueOf(payload.readInt1());
        if (NewParametersBoundFlag.PARAMETER_TYPE_EXIST == newParametersBoundFlag) {
            binaryStatement.setParameterTypes(getParameterTypes(payload, parametersCount));
        }
        parameters = getParameters(payload, parametersCount);
        backendHandler = BackendHandlerFactory.getInstance().newBinaryProtocolInstance(sequenceId, binaryStatement.getSql(), parameters, backendConnection, DatabaseType.MySQL);
    }
    
    private List<BinaryStatementParameterType> getParameterTypes(final MySQLPacketPayload payload, final int parametersCount) {
        List<BinaryStatementParameterType> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            ColumnType columnType = ColumnType.valueOf(payload.readInt1());
            int unsignedFlag = payload.readInt1();
            result.add(new BinaryStatementParameterType(columnType, unsignedFlag));
        }
        return result;
    }
    
    private List<Object> getParameters(final MySQLPacketPayload payload, final int parametersCount) throws SQLException {
        List<Object> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            BinaryProtocolValue binaryProtocolValue = BinaryProtocolValueFactory.getBinaryProtocolValue(binaryStatement.getParameterTypes().get(parameterIndex).getColumnType());
            result.add(nullBitmap.isNullParameter(parameterIndex) ? null : binaryProtocolValue.read(payload));
        }
        return result;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt4(statementId);
        payload.writeInt1(flags);
        payload.writeInt4(ITERATION_COUNT);
        for (int each : nullBitmap.getNullBitmap()) {
            payload.writeInt1(each);
        }
        payload.writeInt1(newParametersBoundFlag.getValue());
        int count = 0;
        for (Object each : parameters) {
            BinaryStatementParameterType parameterType = binaryStatement.getParameterTypes().get(count);
            payload.writeInt1(parameterType.getColumnType().getValue());
            payload.writeInt1(parameterType.getUnsignedFlag());
            payload.writeStringLenenc(null == each ? "" : each.toString());
            count++;
        }
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_STMT_EXECUTE received for Sharding-Proxy: {}", statementId);
        if (GlobalRegistry.getInstance().isCircuitBreak()) {
            return Optional.of(new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_CIRCUIT_BREAK_MODE)));
        }
        return Optional.of(backendHandler.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return backendHandler.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = backendHandler.getResultValue();
        return new BinaryResultSetRowPacket(resultPacket.getSequenceId(), resultPacket.getColumnCount(), resultPacket.getData(), resultPacket.getColumnTypes());
    }
}
