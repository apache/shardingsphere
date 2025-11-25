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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.DistSQLStatementContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * PostgreSQL command parse executor.
 */
@RequiredArgsConstructor
public final class PostgreSQLComParseExecutor implements CommandExecutor {
    
    private final PostgreSQLComParsePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        DatabaseType databaseType = metaData.getDatabase(connectionSession.getUsedDatabaseName()).getProtocolType();
        SQLParserEngine sqlParserEngine = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType);
        String sql = packet.getSQL();
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, true);
        String escapedSql = escape(sqlStatement, sql);
        if (!escapedSql.equalsIgnoreCase(sql)) {
            sqlStatement = sqlParserEngine.parse(escapedSql, true);
            sql = escapedSql;
        }
        List<Integer> actualParameterMarkerIndexes = new ArrayList<>(sqlStatement.getParameterMarkers().size());
        if (sqlStatement.getParameterCount() > 0) {
            List<ParameterMarkerSegment> parameterMarkerSegments = new ArrayList<>(sqlStatement.getParameterMarkers());
            for (ParameterMarkerSegment each : parameterMarkerSegments) {
                actualParameterMarkerIndexes.add(each.getParameterIndex());
            }
            sql = convertSQLToJDBCStyle(parameterMarkerSegments, sql);
            sqlStatement = sqlParserEngine.parse(sql, true);
        }
        List<PostgreSQLColumnType> paddedColumnTypes = paddingColumnTypes(sqlStatement.getParameterCount(), packet.readParameterTypes());
        List<String> paddingTypeNames = paddingTypeNames(paddedColumnTypes);
        SQLStatementContext sqlStatementContext = sqlStatement instanceof DistSQLStatement
                ? new DistSQLStatementContext((DistSQLStatement) sqlStatement)
                : new SQLBindEngine(metaData, connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement);
        PostgreSQLServerPreparedStatement serverPreparedStatement = new PostgreSQLServerPreparedStatement(
                sql, sqlStatementContext, packet.getHintValueContext(), paddedColumnTypes,paddingTypeNames, actualParameterMarkerIndexes);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(packet.getStatementId(), serverPreparedStatement);
        return Collections.singleton(PostgreSQLParseCompletePacket.getInstance());
    }
    
    private String escape(final SQLStatement sqlStatement, final String sql) {
        return sqlStatement instanceof DMLStatement ? sql.replace("?", "??") : sql;
    }
    
    private String convertSQLToJDBCStyle(final List<ParameterMarkerSegment> parameterMarkerSegments, final String sql) {
        parameterMarkerSegments.sort(Comparator.comparingInt(SQLSegment::getStopIndex));
        StringBuilder result = new StringBuilder(sql);
        for (int i = parameterMarkerSegments.size() - 1; i >= 0; i--) {
            ParameterMarkerSegment each = parameterMarkerSegments.get(i);
            result.replace(each.getStartIndex(), each.getStopIndex() + 1, ParameterMarkerType.QUESTION.getMarker());
        }
        return result.toString();
    }
    
    private List<PostgreSQLColumnType> paddingColumnTypes(final int parameterCount, final List<PostgreSQLColumnType> specifiedColumnTypes) {
        if (parameterCount == specifiedColumnTypes.size()) {
            return specifiedColumnTypes;
        }
        List<PostgreSQLColumnType> result = new ArrayList<>(parameterCount);
        result.addAll(specifiedColumnTypes);
        int unspecifiedCount = parameterCount - specifiedColumnTypes.size();
        for (int i = 0; i < unspecifiedCount; i++) {
            result.add(PostgreSQLColumnType.UNSPECIFIED);
        }
        return result;
    }

    // 初始化TypeNames长度
    private List<String> paddingTypeNames(final List<PostgreSQLColumnType> paddedColumnTypes) {
        List<String> result = new ArrayList<>(paddedColumnTypes.size());
        for (PostgreSQLColumnType ignored : paddedColumnTypes) {
            result.add(null);
        }
        return result;
    }
}
