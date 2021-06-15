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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.parse;

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.ConnectionScopeBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * PostgreSQL command parse executor.
 */
public final class PostgreSQLComParseExecutor implements CommandExecutor {
    
    public PostgreSQLComParseExecutor(final PostgreSQLConnectionContext connectionContext, final PostgreSQLComParsePacket packet, final BackendConnection backendConnection) {
        String schemaName = backendConnection.getSchemaName();
        ConnectionScopeBinaryStatementRegistry binaryStatementRegistry = PostgreSQLBinaryStatementRegistry.getInstance().get(backendConnection.getConnectionId());
        SQLStatement sqlStatement = parseSql(packet.getSql(), schemaName);
        connectionContext.setSqlStatement(sqlStatement);
        binaryStatementRegistry.register(packet.getStatementId(), packet.getSql(), sqlStatement.getParameterCount(), packet.getBinaryStatementColumnTypes());
    }
    
    private SQLStatement parseSql(final String sql, final String schemaName) {
        if (sql.isEmpty()) {
            return new EmptyStatement();
        }
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType()));
        return sqlStatementParserEngine.parse(sql, true);
    }
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        return Collections.singletonList(new PostgreSQLParseCompletePacket());
    }
}
