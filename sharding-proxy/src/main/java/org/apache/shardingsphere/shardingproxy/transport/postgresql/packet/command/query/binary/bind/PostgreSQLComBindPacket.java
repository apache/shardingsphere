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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.FailureResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.SuccessResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeaderResponse;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.constant.PostgreSQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL command bind packet.
 *
 * @author zhangyonglun
 */
@Slf4j
public final class PostgreSQLComBindPacket implements PostgreSQLQueryCommandPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.BIND.getValue();
    
    private final String statementId;
    
    private final PostgreSQLBinaryStatement binaryStatement;
    
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Getter
    private final boolean binaryRowData;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        int parameterFormatsLength = payload.readInt2();
        for (int i = 0; i < parameterFormatsLength; i++) {
            payload.readInt2();
        }
        binaryStatement = backendConnection.getConnectionScopeBinaryStatementRegistry().getBinaryStatement(statementId);
        if (null != binaryStatement && null != binaryStatement.getSql()) {
            databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(
                backendConnection.getLogicSchema(), binaryStatement.getSql(), getParameters(payload), backendConnection);
        }
        int resultFormatsLength = payload.readInt2();
        binaryRowData = resultFormatsLength > 0;
        for (int i = 0; i < resultFormatsLength; i++) {
            payload.readInt2();
        }
    }
    
    private List<Object> getParameters(final PostgreSQLPacketPayload payload) throws SQLException {
        int parametersCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            payload.readInt4();
            PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(binaryStatement.getParameterTypes().get(parameterIndex).getColumnType());
            result.add(binaryProtocolValue.read(payload));
        }
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("PostgreSQLComBindPacket received for Sharding-Proxy: {}", statementId);
        if (GlobalRegistry.getInstance().isCircuitBreak()) {
            return Optional.of(new CommandResponsePackets(new PostgreSQLErrorResponsePacket()));
        }
        CommandResponsePackets result = new CommandResponsePackets(new PostgreSQLBindCompletePacket());
        if (null != databaseCommunicationEngine) {
            BackendResponse backendResponse = databaseCommunicationEngine.execute();
            if (backendResponse instanceof SuccessResponse) {
                return Optional.of(new CommandResponsePackets(createDatabaseSuccessPacket((SuccessResponse) backendResponse)));
            }
            if (backendResponse instanceof FailureResponse) {
                return Optional.of(new CommandResponsePackets(createDatabaseFailurePacket((FailureResponse) backendResponse)));
            }
            Collection<DataHeaderPacket> dataHeaderPackets = createDataHeaderPackets(((QueryHeaderResponse) backendResponse).getQueryHeaders());
            return Optional.<CommandResponsePackets>of(new QueryResponsePackets(dataHeaderPackets, dataHeaderPackets.size() + 2));
        }
        return Optional.of(result);
    }
    
    private DatabaseSuccessPacket createDatabaseSuccessPacket(final SuccessResponse successResponse) {
        return new DatabaseSuccessPacket(1, successResponse.getAffectedRows(), successResponse.getLastInsertId());
    }
    
    private DatabaseFailurePacket createDatabaseFailurePacket(final FailureResponse failureResponse) {
        return new DatabaseFailurePacket(1, failureResponse.getErrorCode(), failureResponse.getSqlState(), failureResponse.getErrorMessage());
    }
    
    private Collection<DataHeaderPacket> createDataHeaderPackets(final List<QueryHeader> queryHeaders) {
        Collection<DataHeaderPacket> result = new LinkedList<>();
        int sequenceId = 1;
        for (QueryHeader each : queryHeaders) {
            result.add(createDataHeaderPacket(++sequenceId, each));
        }
        return result;
    }
    
    private DataHeaderPacket createDataHeaderPacket(final int sequenceId, final QueryHeader queryHeader) {
        return new DataHeaderPacket(sequenceId, queryHeader.getSchema(), queryHeader.getTable(), queryHeader.getTable(),
            queryHeader.getColumnLabel(), queryHeader.getColumnName(), queryHeader.getColumnLength(), queryHeader.getColumnType(), queryHeader.getDecimals());
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != databaseCommunicationEngine && databaseCommunicationEngine.next();
    }
    
    @Override
    public DatabasePacket getQueryData() throws SQLException {
        QueryData queryData = databaseCommunicationEngine.getQueryData();
        return binaryRowData
                ? new PostgreSQLBinaryResultSetRowPacket(queryData.getColumnCount(), queryData.getData(), getPostgreSQLColumnTypes(queryData)) : new PostgreSQLDataRowPacket(queryData.getData());
    }
    
    private List<PostgreSQLColumnType> getPostgreSQLColumnTypes(final QueryData queryData) {
        List<PostgreSQLColumnType> result = new ArrayList<>(queryData.getColumnTypes().size());
        for (int i = 0; i < queryData.getColumnCount(); i++) {
            result.add(PostgreSQLColumnType.valueOfJDBCType(queryData.getColumnTypes().get(i)));
        }
        return result;
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
