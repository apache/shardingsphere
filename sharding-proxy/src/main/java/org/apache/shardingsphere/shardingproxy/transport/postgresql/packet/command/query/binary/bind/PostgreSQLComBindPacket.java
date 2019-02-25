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
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.constant.PostgreSQLColumnType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;

import java.sql.SQLException;
import java.util.ArrayList;
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
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        for (int i = 0; i < payload.readInt2(); i++) {
            payload.readInt2();
        }
        binaryStatement = backendConnection.getConnectionScopeBinaryStatementRegistry().getBinaryStatement(statementId);
        if (null != binaryStatement && null != binaryStatement.getSql()) {
            databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(
                backendConnection.getLogicSchema(), binaryStatement.getSql(), getParameters(payload), backendConnection);
        }
        for (int i = 0; i < payload.readInt2(); i++) {
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
            if ("SHOW TRANSACTION ISOLATION LEVEL".equals(binaryStatement.getSql())) {
                PostgreSQLColumnDescription postgreSQLColumnDescription = new PostgreSQLColumnDescription("transaction_isolation", 0, 0, -1);
                List<PostgreSQLColumnDescription> postgreSQLColumnDescriptions = new ArrayList<>(16);
                postgreSQLColumnDescriptions.add(postgreSQLColumnDescription);
                PostgreSQLRowDescriptionPacket postgreSQLRowDescriptionPacket = new PostgreSQLRowDescriptionPacket(1, postgreSQLColumnDescriptions);
                List<Object> data = new ArrayList<>(1);
                data.add("read committed");
                PostgreSQLDataRowPacket postgreSQLDataRowPacket = new PostgreSQLDataRowPacket(data);
                result.getPackets().add(postgreSQLRowDescriptionPacket);
                result.getPackets().add(postgreSQLDataRowPacket);
            } else {
                result.getPackets().addAll(databaseCommunicationEngine.execute().getPackets());
            }
        }
        return Optional.of(result);
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public DatabasePacket getQueryData() throws SQLException {
        QueryData queryData = databaseCommunicationEngine.getQueryData();
        int columnCount = queryData.getColumnCount();
        List<Integer> jdbcColumnTypes = queryData.getColumnTypes();
        List<PostgreSQLColumnType> columnTypes = new ArrayList<>(128);
        for (int i = 0; i < columnCount; i++) {
            columnTypes.add(PostgreSQLColumnType.valueOfJDBCType(jdbcColumnTypes.get(i)));
        }
        return new PostgreSQLBinaryResultSetRowPacket(queryData.getColumnCount(), queryData.getData(), columnTypes);
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
