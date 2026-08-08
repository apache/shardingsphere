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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.postgresql.util.PGobject;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Parameter type resolver for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PostgreSQLPreparedStatementParameterTypeResolver {
    
    /**
     * Resolve unspecified parameter types by JDBC metadata or schema metadata fallback.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        if (!hasUnspecifiedParameterTypes(preparedStatement) && !hasUnresolvedParameters(parameters)) {
            return;
        }
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, parameters)) {
            resolveParameterTypes(connectionSession, preparedStatement, actualPreparedStatement, parameters);
        } catch (final SQLException ex) {
            log.debug("Failed to resolve parameter types via JDBC metadata, falling back to schema metadata", ex);
            resolveParameterTypesFromSchema(connectionSession, preparedStatement, parameters);
        }
    }
    
    /**
     * Resolve unspecified parameter types by prepared statement metadata.
     *
     * @param preparedStatement prepared statement
     * @param actualPreparedStatement actual prepared statement
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement, final PreparedStatement actualPreparedStatement) throws SQLException {
        resolveParameterTypes(null, preparedStatement, actualPreparedStatement, Collections.emptyList());
    }
    
    /**
     * Resolve parameter types using ParameterMetaData.
     *
     * @param preparedStatement prepared statement
     * @param actualPreparedStatement actual prepared statement
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement,
                                             final PreparedStatement actualPreparedStatement, final List<Object> parameters) throws SQLException {
        resolveParameterTypes(null, preparedStatement, actualPreparedStatement, parameters);
    }
    
    /**
     * Resolve parameter types using ParameterMetaData with a ConnectionSession fallback.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param actualPreparedStatement actual prepared statement
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession,
                                             final PostgreSQLServerPreparedStatement preparedStatement,
                                             final PreparedStatement actualPreparedStatement,
                                             final List<Object> parameters) throws SQLException {
        ParameterMetaData parameterMetaData = actualPreparedStatement.getParameterMetaData();
        int paramCount = preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount();
        
        for (int i = 0; i < paramCount; i++) {
            int paramIndex = i + 1;
            int jdbcType = Types.OTHER;
            String parameterTypeName = null;
            
            try {
                jdbcType = parameterMetaData.getParameterType(paramIndex);
                parameterTypeName = parameterMetaData.getParameterTypeName(paramIndex);
            } catch (final SQLException ex) {
                log.debug("Failed to resolve parameter type via JDBC metadata for index {}, falling back to schema metadata", paramIndex, ex);
            }
            
            if (Types.OTHER == jdbcType || null == parameterTypeName || parameterTypeName.trim().isEmpty() || "unknown".equalsIgnoreCase(parameterTypeName)) {
                String schemaTypeName = findParameterTypeName(connectionSession, preparedStatement.getSqlStatementContext(), i);
                if (!"unknown".equalsIgnoreCase(schemaTypeName)) {
                    parameterTypeName = schemaTypeName;
                }
            }
            
            if (PostgreSQLBinaryColumnType.UNSPECIFIED == preparedStatement.getParameterTypes().get(i) && null != parameterTypeName && !"unknown".equalsIgnoreCase(parameterTypeName)) {
                preparedStatement.getParameterTypes().set(i, PostgreSQLBinaryColumnType.valueOfJDBCType(jdbcType, parameterTypeName));
            }
            
            if (null == parameterTypeName || parameterTypeName.trim().isEmpty() || "unknown".equalsIgnoreCase(parameterTypeName)) {
                continue;
            }
            
            if (i >= parameters.size()) {
                continue;
            }
            
            Object parameter = parameters.get(i);
            
            if (parameter instanceof PGobject) {
                PGobject pgObject = (PGobject) parameter;
                if (null == pgObject.getType() || pgObject.getType().isEmpty()) {
                    pgObject.setType(parameterTypeName);
                }
                continue;
            }
            
            if (parameter instanceof String && Types.OTHER == jdbcType) {
                PGobject pgObject = new PGobject();
                pgObject.setType(parameterTypeName);
                pgObject.setValue((String) parameter);
                parameters.set(i, pgObject);
            }
        }
    }
    
    private static String findParameterTypeName(final ConnectionSession connectionSession, final SQLStatementContext sqlStatementContext, final int parameterIndex) {
        String columnName = extractColumnName(sqlStatementContext, parameterIndex);
        if (null == columnName) {
            return "unknown";
        }
        
        if (null == sqlStatementContext.getTablesContext()) {
            return "unknown";
        }
        
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (tableNames.isEmpty()) {
            return "unknown";
        }
        
        if (null == connectionSession) {
            return "unknown";
        }
        
        String usedDb = connectionSession.getUsedDatabaseName();
        String currentDb = connectionSession.getCurrentDatabaseName();
        String databaseName = null != usedDb ? usedDb : currentDb;
        
        if (null == databaseName) {
            return "unknown";
        }
        
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        if (null == database) {
            return "unknown";
        }
        
        String schemaName = sqlStatementContext.getTablesContext().getSchemaName()
                .orElseGet(() -> null != database.getDefaultSchemaName() ? database.getDefaultSchemaName() : "public");
        
        ShardingSphereSchema schema = database.getSchema(schemaName);
        if (null == schema) {
            return "unknown";
        }
        
        String tableName = tableNames.iterator().next();
        ShardingSphereTable table = schema.getTable(tableName);
        if (null != table) {
            ShardingSphereColumn column = table.getColumn(columnName);
            if (null != column && null != column.getTypeName()) {
                return column.getTypeName();
            }
        }
        
        return "unknown";
    }
    
    private static String extractColumnName(final SQLStatementContext sqlStatementContext, final int parameterIndex) {
        if (sqlStatementContext instanceof InsertStatementContext) {
            List<String> columnNames = ((InsertStatementContext) sqlStatementContext).getInsertColumnNames();
            return parameterIndex < columnNames.size() ? columnNames.get(parameterIndex) : null;
        }
        if (sqlStatementContext instanceof UpdateStatementContext) {
            return extractColumnNameFromUpdate(((UpdateStatementContext) sqlStatementContext).getSqlStatement(), parameterIndex);
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            return extractColumnNameFromSelect(((SelectStatementContext) sqlStatementContext).getSqlStatement(), parameterIndex);
        }
        if (sqlStatementContext instanceof DeleteStatementContext) {
            return extractColumnNameFromDelete(((DeleteStatementContext) sqlStatementContext).getSqlStatement(), parameterIndex);
        }
        return null;
    }
    
    private static String extractColumnNameFromSelect(final SelectStatement selectStatement, final int parameterIndex) {
        if (selectStatement.getWhere().isPresent()) {
            return findColumnInExpression(selectStatement.getWhere().get().getExpr(), parameterIndex);
        }
        return null;
    }
    
    private static String extractColumnNameFromDelete(final DeleteStatement deleteStatement, final int parameterIndex) {
        if (deleteStatement.getWhere().isPresent()) {
            return findColumnInExpression(deleteStatement.getWhere().get().getExpr(), parameterIndex);
        }
        return null;
    }
    
    private static String extractColumnNameFromUpdate(final UpdateStatement updateStatement, final int parameterIndex) {
        if (null != updateStatement.getSetAssignment()) {
            for (ColumnAssignmentSegment assignment : updateStatement.getSetAssignment().getAssignments()) {
                if (hasParameterIndex(assignment.getValue(), parameterIndex) && !assignment.getColumns().isEmpty()) {
                    return assignment.getColumns().get(0).getIdentifier().getValue();
                }
            }
        }
        if (updateStatement.getWhere().isPresent()) {
            return findColumnInExpression(updateStatement.getWhere().get().getExpr(), parameterIndex);
        }
        return null;
    }
    
    /**
     * Recursively checks if an expression segment contains a specific parameter marker index.
     *
     * @param expression expression segment
     * @param parameterIndex parameter index
     * @return true if expression contains parameter index, otherwise false
     */
    private static boolean hasParameterIndex(final ExpressionSegment expression, final int parameterIndex) {
        if (null == expression) {
            return false;
        }
        if (expression instanceof ParameterMarkerExpressionSegment) {
            return ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex() == parameterIndex;
        }
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpr = (BinaryOperationExpression) expression;
            return hasParameterIndex(binaryExpr.getLeft(), parameterIndex) || hasParameterIndex(binaryExpr.getRight(), parameterIndex);
        }
        return false;
    }
    
    /**
     * Recursively inspects WHERE expressions (AND/OR, binary operations) to find
     * which ColumnSegment is paired with the given parameterIndex.
     *
     * @param expression expression segment
     * @param parameterIndex parameter index
     * @return column name if found, otherwise null
     */
    private static String findColumnInExpression(final ExpressionSegment expression, final int parameterIndex) {
        if (null == expression) {
            return null;
        }
        
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpr = (BinaryOperationExpression) expression;
            
            if (binaryExpr.getLeft() instanceof ColumnSegment && hasParameterIndex(binaryExpr.getRight(), parameterIndex)) {
                return ((ColumnSegment) binaryExpr.getLeft()).getIdentifier().getValue();
            }
            if (binaryExpr.getRight() instanceof ColumnSegment && hasParameterIndex(binaryExpr.getLeft(), parameterIndex)) {
                return ((ColumnSegment) binaryExpr.getRight()).getIdentifier().getValue();
            }
            
            String leftResult = findColumnInExpression(binaryExpr.getLeft(), parameterIndex);
            if (null != leftResult) {
                return leftResult;
            }
            return findColumnInExpression(binaryExpr.getRight(), parameterIndex);
        }
        
        return null;
    }
    
    private static boolean hasUnspecifiedParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement) {
        return 0 != preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount()
                && preparedStatement.getParameterTypes().stream().anyMatch(each -> PostgreSQLBinaryColumnType.UNSPECIFIED == each);
    }
    
    private static boolean hasUnresolvedParameters(final List<Object> parameters) {
        for (Object each : parameters) {
            if (each instanceof String) {
                return true;
            }
            if (each instanceof PGobject && (null == ((PGobject) each).getType() || ((PGobject) each).getType().isEmpty())) {
                return true;
            }
        }
        return false;
    }
    
    private static void resolveParameterTypesFromSchema(final ConnectionSession connectionSession,
                                                        final PostgreSQLServerPreparedStatement preparedStatement,
                                                        final List<Object> parameters) throws SQLException {
        int paramCount = preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount();
        for (int i = 0; i < paramCount; i++) {
            if (PostgreSQLBinaryColumnType.UNSPECIFIED != preparedStatement.getParameterTypes().get(i)) {
                continue;
            }
            
            String schemaTypeName = findParameterTypeName(connectionSession, preparedStatement.getSqlStatementContext(), i);
            if ("unknown".equalsIgnoreCase(schemaTypeName)) {
                continue;
            }
            
            preparedStatement.getParameterTypes().set(i, PostgreSQLBinaryColumnType.valueOfJDBCType(Types.OTHER, schemaTypeName));
            
            if (i >= parameters.size()) {
                continue;
            }
            
            Object parameter = parameters.get(i);
            if (parameter instanceof PGobject) {
                PGobject pgObject = (PGobject) parameter;
                if (null == pgObject.getType() || pgObject.getType().isEmpty()) {
                    pgObject.setType(schemaTypeName);
                }
            } else if (parameter instanceof String) {
                PGobject pgObject = new PGobject();
                pgObject.setType(schemaTypeName);
                pgObject.setValue((String) parameter);
                parameters.set(i, pgObject);
            }
        }
    }
}
