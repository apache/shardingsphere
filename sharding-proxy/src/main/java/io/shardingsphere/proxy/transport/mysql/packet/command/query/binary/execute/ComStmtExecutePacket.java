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

package io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.proxy.backend.ResultPacket;
import io.shardingsphere.proxy.backend.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngineFactory;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.NewParametersBoundFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.QueryCommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.BinaryStatement;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.BinaryStatementParameter;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.BinaryStatementParameterType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * COM_STMT_EXECUTE command packet.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-stmt-execute.html">COM_STMT_EXECUTE</a>
 *
 * @author zhangyonglun
 */
@Getter
@Slf4j
public final class ComStmtExecutePacket implements QueryCommandPacket {
    
    private static final RuleRegistry RULE_REGISTRY = RuleRegistry.getInstance();
    
    private static final int ITERATION_COUNT = 1;
    
    private static final int NULL_BITMAP_OFFSET = 0;
    
    private static final ColumnType NULL_PARAMETER_DEFAULT_COLUMN_TYPE = ColumnType.MYSQL_TYPE_STRING;
    
    private static final int NULL_PARAMETER_DEFAULT_UNSIGNED_FLAG = 0;
    
    private final int sequenceId;
    
    private final int statementId;
    
    private final int flags;
    
    private final NullBitmap nullBitmap;
    
    private final NewParametersBoundFlag newParametersBoundFlag;
    
    private final List<BinaryStatementParameter> binaryStatementParameters = new ArrayList<>(32);
    
    private final BinaryStatement binaryStatement;
    
    private final JDBCBackendHandler jdbcBackendHandler;
    
    public ComStmtExecutePacket(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        statementId = payload.readInt4();
        binaryStatement = BinaryStatementRegistry.getInstance().getBinaryStatement(statementId);
        flags = payload.readInt1();
        Preconditions.checkArgument(ITERATION_COUNT == payload.readInt4());
        int parametersCount = binaryStatement.getParametersCount();
        if (parametersCount > 0) {
            nullBitmap = new NullBitmap(parametersCount, NULL_BITMAP_OFFSET);
            for (int i = 0; i < nullBitmap.getNullBitmap().length; i++) {
                nullBitmap.getNullBitmap()[i] = payload.readInt1();
            }
            newParametersBoundFlag = NewParametersBoundFlag.valueOf(payload.readInt1());
            setParameterList(payload, parametersCount);
        } else {
            nullBitmap = null;
            newParametersBoundFlag = null;
        }
        jdbcBackendHandler = new JDBCBackendHandler(binaryStatement.getSql(), JDBCExecuteEngineFactory.createBinaryProtocolInstance(binaryStatementParameters, backendConnection));
    }
    
    private void setParameterList(final MySQLPacketPayload payload, final int parametersCount) {
        if (NewParametersBoundFlag.PARAMETER_TYPE_EXIST == newParametersBoundFlag) {
            setParameterHeader(payload, parametersCount);
        } else if (NewParametersBoundFlag.PARAMETER_TYPE_NOT_EXIST == newParametersBoundFlag) {
            setParameterHeaderFromCache(parametersCount);
        }
        setParameterValue(payload, parametersCount);
    }
    
    private void setParameterHeader(final MySQLPacketPayload payload, final int numParameters) {
        List<BinaryStatementParameterType> parameterHeaders = new ArrayList<>(32);
        for (int i = 0; i < numParameters; i++) {
            if (nullBitmap.isParameterNull(i)) {
                binaryStatementParameters.add(new BinaryStatementParameter(NULL_PARAMETER_DEFAULT_COLUMN_TYPE, NULL_PARAMETER_DEFAULT_UNSIGNED_FLAG, null));
                continue;
            }
            ColumnType columnType = ColumnType.valueOf(payload.readInt1());
            int unsignedFlag = payload.readInt1();
            binaryStatementParameters.add(new BinaryStatementParameter(columnType, unsignedFlag));
            parameterHeaders.add(new BinaryStatementParameterType(columnType, unsignedFlag));
        }
        binaryStatement.setParameterTypes(parameterHeaders);
    }
    
    private void setParameterHeaderFromCache(final int numParameters) {
        Iterator<BinaryStatementParameterType> parameterHeaders = binaryStatement.getParameterTypes().iterator();
        for (int i = 0; i < numParameters; i++) {
            if (nullBitmap.isParameterNull(i)) {
                binaryStatementParameters.add(new BinaryStatementParameter(NULL_PARAMETER_DEFAULT_COLUMN_TYPE, NULL_PARAMETER_DEFAULT_UNSIGNED_FLAG, null));
                continue;
            }
            BinaryStatementParameterType preparedStatementParameterHeader = parameterHeaders.next();
            binaryStatementParameters.add(new BinaryStatementParameter(preparedStatementParameterHeader.getColumnType(), preparedStatementParameterHeader.getUnsignedFlag()));
        }
    }
    
    private void setParameterValue(final MySQLPacketPayload payload, final int numParameters) {
        for (int i = 0; i < numParameters; i++) {
            if (nullBitmap.isParameterNull(i)) {
                continue;
            }
            BinaryStatementParameter binaryStatementParameter = binaryStatementParameters.get(i);
            ColumnType columnType = binaryStatementParameter.getType().getColumnType();
            binaryStatementParameter.setValue(BinaryProtocolValueUtility.getInstance().readBinaryProtocolValue(columnType, payload));
        }
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt4(statementId);
        payload.writeInt1(flags);
        payload.writeInt4(ITERATION_COUNT);
        if (null != nullBitmap) {
            for (int each : nullBitmap.getNullBitmap()) {
                payload.writeInt1(each);
            }
        }
        if (null != newParametersBoundFlag) {
            payload.writeInt1(newParametersBoundFlag.getValue());
        }
        for (BinaryStatementParameter each : binaryStatementParameters) {
            payload.writeInt1(each.getType().getColumnType().getValue());
            payload.writeInt1(each.getType().getUnsignedFlag());
            payload.writeStringLenenc((String) each.getValue());
        }
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_STMT_EXECUTE received for Sharding-Proxy: {}", statementId);
        return Optional.of(jdbcBackendHandler.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return jdbcBackendHandler.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = jdbcBackendHandler.getResultValue();
        return new BinaryResultSetRowPacket(resultPacket.getSequenceId(), resultPacket.getColumnCount(), resultPacket.getData(), resultPacket.getColumnTypes());
    }
}
