/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatement;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementParameterType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValue;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;

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
public final class MySQLQueryComStmtExecutePacket implements MySQLQueryCommandPacket {
    
    private static final int ITERATION_COUNT = 1;
    
    private static final int NULL_BITMAP_OFFSET = 0;
    
    @Getter
    private final int sequenceId;
    
    private final int statementId;
    
    private final MySQLBinaryStatement mySQLBinaryStatement;
    
    private final int flags;
    
    private final MySQLNullBitmap mySQLNullBitmap;
    
    private final MySQLNewParametersBoundFlag mySQLNewParametersBoundFlag;
    
    private final List<Object> parameters;
    
    private final DatabaseCommunicationEngine databaseCommunicationEngine;
    
    public MySQLQueryComStmtExecutePacket(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        this.sequenceId = sequenceId;
        statementId = payload.readInt4();
        mySQLBinaryStatement = MySQLBinaryStatementRegistry.getInstance().getBinaryStatement(statementId);
        flags = payload.readInt1();
        Preconditions.checkArgument(ITERATION_COUNT == payload.readInt4());
        int parametersCount = mySQLBinaryStatement.getParametersCount();
        mySQLNullBitmap = new MySQLNullBitmap(parametersCount, NULL_BITMAP_OFFSET);
        for (int i = 0; i < mySQLNullBitmap.getNullBitmap().length; i++) {
            mySQLNullBitmap.getNullBitmap()[i] = payload.readInt1();
        }
        mySQLNewParametersBoundFlag = MySQLNewParametersBoundFlag.valueOf(payload.readInt1());
        if (MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST == mySQLNewParametersBoundFlag) {
            mySQLBinaryStatement.setParameterTypes(getParameterTypes(payload, parametersCount));
        }
        parameters = getParameters(payload, parametersCount);
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(
                backendConnection.getLogicSchema(), sequenceId, mySQLBinaryStatement.getSql(), parameters, backendConnection, GlobalRegistry.getInstance().getDatabaseType());
    }
    
    private List<MySQLBinaryStatementParameterType> getParameterTypes(final MySQLPacketPayload payload, final int parametersCount) {
        List<MySQLBinaryStatementParameterType> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            MySQLColumnType mySQLColumnType = MySQLColumnType.valueOf(payload.readInt1());
            int unsignedFlag = payload.readInt1();
            result.add(new MySQLBinaryStatementParameterType(mySQLColumnType, unsignedFlag));
        }
        return result;
    }
    
    private List<Object> getParameters(final MySQLPacketPayload payload, final int parametersCount) throws SQLException {
        List<Object> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            MySQLBinaryProtocolValue mySQLBinaryProtocolValue = MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(mySQLBinaryStatement.getParameterTypes().get(parameterIndex).getMySQLColumnType());
            result.add(mySQLNullBitmap.isNullParameter(parameterIndex) ? null : mySQLBinaryProtocolValue.read(payload));
        }
        return result;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt4(statementId);
        payload.writeInt1(flags);
        payload.writeInt4(ITERATION_COUNT);
        for (int each : mySQLNullBitmap.getNullBitmap()) {
            payload.writeInt1(each);
        }
        payload.writeInt1(mySQLNewParametersBoundFlag.getValue());
        int count = 0;
        for (Object each : parameters) {
            MySQLBinaryStatementParameterType parameterType = mySQLBinaryStatement.getParameterTypes().get(count);
            payload.writeInt1(parameterType.getMySQLColumnType().getValue());
            payload.writeInt1(parameterType.getUnsignedFlag());
            payload.writeStringLenenc(null == each ? "" : each.toString());
            count++;
        }
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("COM_STMT_EXECUTE received for Sharding-Proxy: {}", statementId);
        if (GlobalRegistry.getInstance().isCircuitBreak()) {
            return Optional.of(new CommandResponsePackets(new MySQLErrPacket(1, MySQLServerErrorCode.ER_CIRCUIT_BREAK_MODE)));
        }
        return Optional.of(databaseCommunicationEngine.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = databaseCommunicationEngine.getResultValue();
        int columnCount = resultPacket.getColumnCount();
        List<Integer> jdbcColumnTypes = resultPacket.getColumnTypes();
        List<MySQLColumnType> mySQLColumnTypes = new ArrayList<>(128);
        for (int i = 0; i < columnCount; i++) {
            mySQLColumnTypes.add(MySQLColumnType.valueOfJDBCType(jdbcColumnTypes.get(i)));
        }
        return new MySQLBinaryResultSetRowPacket(resultPacket.getSequenceId(), columnCount, resultPacket.getData(), mySQLColumnTypes);
    }
}
