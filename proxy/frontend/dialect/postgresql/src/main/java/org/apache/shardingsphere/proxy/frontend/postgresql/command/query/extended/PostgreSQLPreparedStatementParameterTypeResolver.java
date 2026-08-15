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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.database.protocol.postgresql.type.PostgreSQLColumnTypeOIDResolver;
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
        if (isParameterTypesResolved(preparedStatement)) {
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
        if (isParameterTypesResolved(preparedStatement)) {
            return;
        }
        ParameterMetaData parameterMetaData = actualPreparedStatement.getParameterMetaData();
        int paramCount = preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount();
        for (int i = 0; i < paramCount; i++) {
            if (!preparedStatement.getParameterTypeStates().get(i).isResolved()) {
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
                    preparedStatement.setParameterType(i, PostgreSQLPreparedStatementParameterType.valueOf(jdbcType, parameterTypeName,
                            new PostgreSQLColumnTypeOIDResolver().findTypeOID(actualPreparedStatement.getConnection(), parameterTypeName).orElse(null)));
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
            if (preparedStatement.getParameterTypeStates().get(paramIndex).isResolved()) {
                continue;
            }
            
            if (isIncompleteBoundInfo(boundInfo)) {
                continue;
            }
            String originalDatabaseName = boundInfo.getOriginalDatabase().getValue();
            if (!connectionSession.getUsedDatabaseName().equalsIgnoreCase(originalDatabaseName) || !database.containsSchema(boundInfo.getOriginalSchema().getValue())) {
                continue;
            }
            ShardingSphereSchema schema = database.getSchema(boundInfo.getOriginalSchema().getValue());
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
            
            PostgreSQLPreparedStatementParameterType parameterType = PostgreSQLPreparedStatementParameterType.valueOf(column.getDataType(), column.getTypeName(), null);
            if (!parameterType.isResolved()) {
                continue;
            }
            preparedStatement.setParameterType(paramIndex, parameterType);
            anyResolved = true;
        }
        return anyResolved && preparedStatement.isParameterTypesResolved();
    }
    
    /**
     * Check if column segment bound info contains incomplete target column metadata.
     *
     * @param boundInfo column segment bound info
     * @return true if incomplete
     */
    public static boolean isIncompleteBoundInfo(final ColumnSegmentBoundInfo boundInfo) {
        return null == boundInfo
                || null == boundInfo.getOriginalDatabase() || null == boundInfo.getOriginalDatabase().getValue() || boundInfo.getOriginalDatabase().getValue().isEmpty()
                || null == boundInfo.getOriginalSchema() || null == boundInfo.getOriginalSchema().getValue() || boundInfo.getOriginalSchema().getValue().isEmpty()
                || null == boundInfo.getOriginalTable() || null == boundInfo.getOriginalTable().getValue() || boundInfo.getOriginalTable().getValue().isEmpty()
                || null == boundInfo.getOriginalColumn() || null == boundInfo.getOriginalColumn().getValue() || boundInfo.getOriginalColumn().getValue().isEmpty();
    }
    
    private static void tryResolveFromJDBCMetadata(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) {
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, parameters)) {
            resolveParameterTypes(preparedStatement, actualPreparedStatement);
        } catch (final SQLException ex) {
            log.debug("Failed to resolve parameter types via JDBC metadata", ex);
        }
    }
    
    private static boolean isParameterTypesResolved(final PostgreSQLServerPreparedStatement preparedStatement) {
        return 0 == preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount()
                || preparedStatement.isParameterTypesResolved();
    }
    
    /**
     * Decode resolved text parameters for a prepared statement.
     *
     * @param preparedStatement prepared statement
     * @param parameters parameters
     */
    public static void decodeResolvedTextParameters(final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            if (value instanceof PostgreSQLTypeUnspecifiedSQLParameter && preparedStatement.getParameterTypeStates().get(i).isResolved()) {
                parameters.set(i, preparedStatement.getParameterTypeStates().get(i).decode(value.toString()));
            }
        }
    }
}
