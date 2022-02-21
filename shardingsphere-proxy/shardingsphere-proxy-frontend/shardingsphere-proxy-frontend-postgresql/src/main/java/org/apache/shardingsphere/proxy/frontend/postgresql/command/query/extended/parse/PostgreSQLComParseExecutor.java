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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * PostgreSQL command parse executor.
 */
@RequiredArgsConstructor
public final class PostgreSQLComParseExecutor implements CommandExecutor {
    
    private final PostgreSQLComParsePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        SQLStatement sqlStatement = parseSql(packet.getSql(), connectionSession.getSchemaName());
        List<PostgreSQLColumnType> paddedColumnTypes = paddingColumnTypes(sqlStatement.getParameterCount(), packet.readParameterTypes());
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionSession.getConnectionId(), packet.getStatementId(), packet.getSql(), sqlStatement, paddedColumnTypes);
        return Collections.singletonList(PostgreSQLParseCompletePacket.getInstance());
    }
    
    private SQLStatement parseSql(final String sql, final String schemaName) {
        if (sql.isEmpty()) {
            return new EmptyStatement();
        }
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType()),
                metaDataContexts.getGlobalRuleMetaData().findSingleRule(SQLParserRule.class).orElse(null));
        return sqlStatementParserEngine.parse(sql, true);
    }
    
    private List<PostgreSQLColumnType> paddingColumnTypes(final int parameterCount, final List<PostgreSQLColumnType> specifiedColumnTypes) {
        if (parameterCount == specifiedColumnTypes.size()) {
            return specifiedColumnTypes;
        }
        List<PostgreSQLColumnType> result = new ArrayList<>(parameterCount);
        result.addAll(specifiedColumnTypes);
        for (int i = 0; i < parameterCount; i++) {
            result.add(PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED);
        }
        return result;
    }
}
