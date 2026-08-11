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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

/**
 * Parameter type resolver for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PostgreSQLPreparedStatementParameterTypeResolver {
    
    /**
     * Resolve unspecified parameter types.
     * Resolution order: semantic (bound AST) first, then JDBC metadata as fallback.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) {
        resolveParameterTypes(connectionSession, preparedStatement, Collections.emptyList());
    }
    
    /**
     * Resolve unspecified parameter types.
     * Resolution order: semantic (bound AST) first, then JDBC metadata as fallback.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param parameters parameters
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) {
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        if (tryResolveFromBoundAST(connectionSession, preparedStatement)) {
            return;
        }
        tryResolveFromJDBCMetadata(connectionSession, preparedStatement, parameters);
    }
    
    /**
     * Resolve unspecified parameter types by prepared statement metadata.
     *
     * @param preparedStatement prepared statement
     * @param actualPreparedStatement actual prepared statement
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement, final PreparedStatement actualPreparedStatement) throws SQLException {
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        ParameterMetaData parameterMetaData = actualPreparedStatement.getParameterMetaData();
        int paramCount = preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount();
        for (int i = 0; i < paramCount; i++) {
            if (PostgreSQLBinaryColumnType.UNSPECIFIED == preparedStatement.getParameterTypes().get(i)) {
                int paramIndex = i + 1;
                int jdbcType = Types.OTHER;
                String parameterTypeName = null;
                try {
                    jdbcType = parameterMetaData.getParameterType(paramIndex);
                    parameterTypeName = parameterMetaData.getParameterTypeName(paramIndex);
                } catch (final SQLException ex) {
                    log.debug("Failed to resolve parameter type via JDBC metadata for index {}", paramIndex, ex);
                }
                if (null != parameterTypeName && !parameterTypeName.trim().isEmpty() && !"unknown".equalsIgnoreCase(parameterTypeName)) {
                    preparedStatement.getParameterTypes().set(i, PostgreSQLBinaryColumnType.valueOfJDBCType(jdbcType, parameterTypeName));
                }
            }
        }
    }
    
    /**
     * Try to resolve unspecified parameter types from bound AST.
     * The Binder annotates each {@link ParameterMarkerExpressionSegment} with a {@link ColumnSegmentBoundInfo}
     * when it is semantically paired with a column (INSERT values, UPDATE SET, WHERE predicates).
     * This method reads that annotation from the flat {@code parameterMarkers} list — no AST traversal.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @return true if all remaining UNSPECIFIED types are resolved
     */
    public static boolean tryResolveFromBoundAST(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) {
        SQLStatementContext sqlStatementContext = preparedStatement.getSqlStatementContext();
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData()
                .getDatabase(connectionSession.getUsedDatabaseName());
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        String schemaName = null == tablesContext ? database.getDefaultSchemaName() : tablesContext.getSchemaName().orElse(database.getDefaultSchemaName());
        if (null == schemaName || !database.containsSchema(schemaName)) {
            return false;
        }
        ShardingSphereSchema schema = database.getSchema(schemaName);
        boolean anyResolved = false;
        for (ParameterMarkerSegment marker : sqlStatementContext.getSqlStatement().getParameterMarkers()) {
            if (!(marker instanceof ParameterMarkerExpressionSegment)) {
                continue;
            }
            ParameterMarkerExpressionSegment param = (ParameterMarkerExpressionSegment) marker;
            ColumnSegmentBoundInfo boundInfo = param.getBoundInfo();
            if (null == boundInfo) {
                continue;
            }
            int paramIndex = param.getParameterMarkerIndex();
            if (paramIndex >= preparedStatement.getParameterTypes().size()) {
                continue;
            }
            if (PostgreSQLBinaryColumnType.UNSPECIFIED != preparedStatement.getParameterTypes().get(paramIndex)) {
                continue;
            }
            String tableName = boundInfo.getOriginalTable().getValue();
            String columnName = boundInfo.getOriginalColumn().getValue();
            if (null == tableName || tableName.isEmpty() || null == columnName || columnName.isEmpty()) {
                continue;
            }
            ShardingSphereTable table = schema.getTable(tableName);
            if (null == table) {
                continue;
            }
            ShardingSphereColumn column = table.getColumn(columnName);
            if (null == column) {
                continue;
            }
            preparedStatement.getParameterTypes().set(paramIndex, PostgreSQLBinaryColumnType.valueOfJDBCType(column.getDataType(), column.getTypeName()));
            anyResolved = true;
        }
        return anyResolved && !hasUnspecifiedParameterTypes(preparedStatement);
    }
    
    private static void tryResolveFromJDBCMetadata(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) {
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, parameters)) {
            resolveParameterTypes(preparedStatement, actualPreparedStatement);
        } catch (final SQLException ex) {
            log.debug("Failed to resolve parameter types via JDBC metadata", ex);
        }
    }
    
    private static boolean hasUnspecifiedParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement) {
        return 0 != preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount()
                && preparedStatement.getParameterTypes().stream().anyMatch(each -> PostgreSQLBinaryColumnType.UNSPECIFIED == each);
    }
}
