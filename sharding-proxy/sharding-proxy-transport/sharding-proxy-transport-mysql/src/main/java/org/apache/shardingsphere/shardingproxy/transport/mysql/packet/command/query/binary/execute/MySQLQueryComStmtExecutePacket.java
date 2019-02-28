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
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.error.CommonErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatement;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementParameterType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValue;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL COM_STMT_EXECUTE command packet.
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
    
    private int dataHeaderEofSequenceId;
    
    private int currentQueryDataSequenceId;
    
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
                backendConnection.getLogicSchema(), mySQLBinaryStatement.getSql(), parameters, backendConnection);
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
            MySQLBinaryProtocolValue mySQLBinaryProtocolValue = MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(
                mySQLBinaryStatement.getParameterTypes().get(parameterIndex).getMySQLColumnType());
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
        if (GlobalContext.getInstance().isCircuitBreak()) {
            return Optional.of(new CommandResponsePackets(new MySQLErrPacket(1, CommonErrorCode.CIRCUIT_BREAK_MODE)));
        }
        BackendResponse backendResponse = databaseCommunicationEngine.execute();
        if (backendResponse instanceof ErrorResponse) {
            return Optional.of(new CommandResponsePackets(createErrorPacket(((ErrorResponse) backendResponse).getCause())));
        }
        if (backendResponse instanceof UpdateResponse) {
            return Optional.of(new CommandResponsePackets(createUpdatePacket((UpdateResponse) backendResponse)));
        }
        List<QueryHeader> queryHeaders = ((QueryResponse) backendResponse).getQueryHeaders();
        dataHeaderEofSequenceId = queryHeaders.size() + 2;
        return Optional.<CommandResponsePackets>of(new QueryResponsePackets(queryHeaders));
    }
    
    private MySQLErrPacket createErrorPacket(final Exception cause) {
        return MySQLErrPacketFactory.newInstance(1, cause);
    }
    
    private MySQLOKPacket createUpdatePacket(final UpdateResponse updateResponse) {
        return new MySQLOKPacket(1, updateResponse.getUpdateCount(), updateResponse.getLastInsertId());
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public MySQLPacket getQueryData() throws SQLException {
        QueryData queryData = databaseCommunicationEngine.getQueryData();
        return new MySQLBinaryResultSetRowPacket(++currentQueryDataSequenceId + dataHeaderEofSequenceId, queryData.getData(), getMySQLColumnTypes(queryData));
    }
    
    private List<MySQLColumnType> getMySQLColumnTypes(final QueryData queryData) {
        List<MySQLColumnType> result = new ArrayList<>(queryData.getColumnTypes().size());
        for (int i = 0; i < queryData.getColumnTypes().size(); i++) {
            result.add(MySQLColumnType.valueOfJDBCType(queryData.getColumnTypes().get(i)));
        }
        return result;
    }
}
