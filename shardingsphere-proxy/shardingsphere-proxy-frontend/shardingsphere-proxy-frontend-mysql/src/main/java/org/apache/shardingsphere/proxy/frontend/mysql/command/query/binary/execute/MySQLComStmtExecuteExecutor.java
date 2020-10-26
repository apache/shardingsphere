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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLBinaryResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.CircuitBreakException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder.ResponsePacketBuilder;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * COM_STMT_EXECUTE command executor for MySQL.
 */
public final class MySQLComStmtExecuteExecutor implements QueryCommandExecutor {
    
    private final DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Getter
    private volatile ResponseType responseType;
    
    private int currentSequenceId;
    
    public MySQLComStmtExecuteExecutor(final MySQLComStmtExecutePacket packet, final BackendConnection backendConnection) {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(ProxyContext.getInstance().getSchemaContexts().getDatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parseToSQLStatement(packet.getSql(), true);
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(sqlStatement, packet.getSql(), packet.getParameters(), backendConnection);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        if (ProxyContext.getInstance().getSchemaContexts().isCircuitBreak()) {
            throw new CircuitBreakException();
        }
        BackendResponse backendResponse = databaseCommunicationEngine.execute();
        return backendResponse instanceof QueryResponse ? processQuery((QueryResponse) backendResponse) : processUpdate((UpdateResponse) backendResponse);
    }
    
    private Collection<DatabasePacket<?>> processQuery(final QueryResponse queryResponse) {
        responseType = ResponseType.QUERY;
        Collection<DatabasePacket<?>> result = ResponsePacketBuilder.buildQueryResponsePackets(queryResponse);
        currentSequenceId = result.size();
        return result;
    }
    
    private Collection<DatabasePacket<?>> processUpdate(final UpdateResponse updateResponse) {
        responseType = ResponseType.UPDATE;
        return ResponsePacketBuilder.buildUpdateResponsePackets(updateResponse);
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public MySQLPacket getQueryData() throws SQLException {
        QueryData queryData = databaseCommunicationEngine.getQueryData();
        return new MySQLBinaryResultSetRowPacket(++currentSequenceId, queryData.getData(), getMySQLColumnTypes(queryData));
    }
    
    private List<MySQLColumnType> getMySQLColumnTypes(final QueryData queryData) {
        List<MySQLColumnType> result = new ArrayList<>(queryData.getColumnTypes().size());
        for (int i = 0; i < queryData.getColumnTypes().size(); i++) {
            result.add(MySQLColumnType.valueOfJDBCType(queryData.getColumnTypes().get(i)));
        }
        return result;
    }
}
