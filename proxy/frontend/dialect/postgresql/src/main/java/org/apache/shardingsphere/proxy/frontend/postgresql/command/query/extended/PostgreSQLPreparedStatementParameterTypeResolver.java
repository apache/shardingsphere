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
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parameter type resolver for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPreparedStatementParameterTypeResolver {
    
    private static final Collection<Integer> AMBIGUOUS_COLUMN_JDBC_TYPES = new HashSet<>(Arrays.asList(Types.OTHER, Types.TIME, Types.TIMESTAMP, Types.DOUBLE, Types.ARRAY));
    
    /**
     * Resolve unspecified parameter types before reading parameter values.
     * Insert statements are resolved by table metadata, other statements are resolved by JDBC metadata without parameters,
     * because parameter values of unspecified types can not be used to route.
     * Insert parameters of columns whose JDBC type is shared by several PostgreSQL types, such as uuid and jsonb as OTHER or timestamptz as TIMESTAMP,
     * stay unspecified, so their values are passed to the database as text.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) throws SQLException {
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        SQLStatement sqlStatement = preparedStatement.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            resolveInsertParameterTypes(connectionSession, preparedStatement, (InsertStatement) sqlStatement, false);
            return;
        }
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.loadForParameterMetadata(connectionSession, preparedStatement)) {
            resolveParameterTypes(preparedStatement, actualPreparedStatement);
        }
    }
    
    /**
     * Resolve unspecified parameter types by JDBC metadata.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param parameters parameters
     * @throws SQLException SQL exception
     */
    public static void resolveParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, parameters)) {
            resolveParameterTypes(preparedStatement, actualPreparedStatement);
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
        if (!hasUnspecifiedParameterTypes(preparedStatement)) {
            return;
        }
        ParameterMetaData parameterMetaData = actualPreparedStatement.getParameterMetaData();
        for (int i = 0; i < preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount(); i++) {
            int parameterIndex = preparedStatement.getActualParameterMarkerIndexes().get(i);
            if (PostgreSQLBinaryColumnType.UNSPECIFIED == preparedStatement.getParameterTypes().get(parameterIndex)) {
                preparedStatement.getParameterTypes().set(parameterIndex, getParameterType(parameterMetaData, i + 1));
            }
        }
    }
    
    /**
     * Resolve unspecified parameter types of insert statement by table metadata.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     */
    public static void resolveInsertParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) {
        resolveInsertParameterTypes(connectionSession, preparedStatement, (InsertStatement) preparedStatement.getSqlStatementContext().getSqlStatement(), true);
    }
    
    private static void resolveInsertParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final InsertStatement insertStatement,
                                                    final boolean resolveAmbiguousTypes) {
        String logicTableName = insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        ShardingSphereTable table = getTableFromMetaData(connectionSession.getUsedDatabaseName(), insertStatement, logicTableName);
        List<ShardingSphereIdentifier> columnNamesOfInsert = getColumnNamesOfInsertStatement(insertStatement, table);
        for (InsertValuesSegment each : insertStatement.getValues()) {
            resolveInsertValuesParameterTypes(preparedStatement, each, logicTableName, table, columnNamesOfInsert, resolveAmbiguousTypes);
        }
    }
    
    private static PostgreSQLBinaryColumnType getParameterType(final ParameterMetaData parameterMetaData, final int parameterMarkerIndex) throws SQLException {
        int jdbcType = parameterMetaData.getParameterType(parameterMarkerIndex);
        String parameterTypeName = parameterMetaData.getParameterTypeName(parameterMarkerIndex);
        return Types.TIMESTAMP == jdbcType && "timestamptz".equalsIgnoreCase(parameterTypeName)
                ? PostgreSQLBinaryColumnType.TIMESTAMPTZ
                : PostgreSQLBinaryColumnType.valueOfJDBCType(jdbcType, parameterTypeName);
    }
    
    private static ShardingSphereTable getTableFromMetaData(final String databaseName, final InsertStatement insertStatement, final String logicTableName) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase(databaseName);
        String schemaName = insertStatement.getTable().flatMap(SimpleTableSegment::getOwner).map(optional -> optional.getIdentifier().getValue()).orElseGet(database::getDefaultSchemaName);
        return database.getSchema(schemaName).getTable(logicTableName);
    }
    
    private static List<ShardingSphereIdentifier> getColumnNamesOfInsertStatement(final InsertStatement insertStatement, final ShardingSphereTable table) {
        return insertStatement.getColumns().isEmpty()
                ? table.getColumnNames()
                : insertStatement.getColumns().stream().map(each -> new ShardingSphereIdentifier(each.getIdentifier().getValue())).collect(Collectors.toList());
    }
    
    private static void resolveInsertValuesParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement, final InsertValuesSegment insertValuesSegment,
                                                          final String logicTableName, final ShardingSphereTable table, final List<ShardingSphereIdentifier> columnNamesOfInsert,
                                                          final boolean resolveAmbiguousTypes) {
        for (int i = 0; i < insertValuesSegment.getValues().size(); i++) {
            ExpressionSegment value = insertValuesSegment.getValues().get(i);
            if (!(value instanceof ParameterMarkerExpressionSegment)) {
                continue;
            }
            int parameterIndex = preparedStatement.getActualParameterMarkerIndexes().get(((ParameterMarkerExpressionSegment) value).getParameterIndex());
            if (PostgreSQLBinaryColumnType.UNSPECIFIED != preparedStatement.getParameterTypes().get(parameterIndex)) {
                continue;
            }
            String columnName = columnNamesOfInsert.get(i).toString();
            ShardingSpherePreconditions.checkState(table.containsColumn(columnName), () -> new ColumnNotFoundException(logicTableName, columnName));
            int columnType = table.getColumn(columnName).getDataType();
            if (resolveAmbiguousTypes || !AMBIGUOUS_COLUMN_JDBC_TYPES.contains(columnType)) {
                preparedStatement.getParameterTypes().set(parameterIndex, PostgreSQLBinaryColumnType.valueOfJDBCType(columnType));
            }
        }
    }
    
    private static boolean hasUnspecifiedParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement) {
        return 0 != preparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount()
                && preparedStatement.getParameterTypes().stream().anyMatch(each -> PostgreSQLBinaryColumnType.UNSPECIFIED == each);
    }
}
