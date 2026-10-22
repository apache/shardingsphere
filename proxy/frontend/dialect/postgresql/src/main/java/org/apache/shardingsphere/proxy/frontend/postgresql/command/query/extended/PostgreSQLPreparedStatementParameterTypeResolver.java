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
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parameter type resolver for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPreparedStatementParameterTypeResolver {
    
    private static final Collection<Integer> INTEGER_COLUMN_JDBC_TYPES = new HashSet<>(Arrays.asList(Types.INTEGER, Types.BIGINT));
    
    private static final Collection<String> COMPARE_OPERATORS = new HashSet<>(Arrays.asList("=", "<", ">", "<=", ">="));
    
    private static final Pattern INTEGER_TEXT_PATTERN = Pattern.compile("[ \\t\\n\\r\\f\\x0B]*([+-]?[0-9]+)[ \\t\\n\\r\\f\\x0B]*");
    
    private static final Pattern BIGINT_TEXT_PATTERN = Pattern.compile("[ \\t\\n\\r\\f\\x0B]*\\+?(0|[1-9][0-9]*)[ \\t\\n\\r\\f\\x0B]*");
    
    /**
     * Resolve values of unspecified type parameters by the integer columns they are compared with or assigned to.
     * A parameter is resolved only when every occurrence of it is compared with or assigned to a column, so that a parameter also used elsewhere,
     * such as an overloaded function argument, keeps the type the database infers from all of its occurrences.
     * Only columns of physical tables are used, and an IN list is used only when all of its values are parameters or literals,
     * because a derived column or another typed value in the list can make the database infer a different type.
     * Only plain decimal integer text is resolved, because Java decodes it to the same value as PostgreSQL integer input.
     * JDBC BIGINT columns can also be oid columns, whose input reads a leading zero as octal and wraps negative numbers,
     * so their text is resolved only without a minus sign or leading zeros, where oid and int8 input give the same value.
     * Other values stay unspecified and are passed to the database as text.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @param params parameters in PostgreSQL parameter index order
     * @return resolved parameters in PostgreSQL parameter index order
     */
    @HighFrequencyInvocation
    public static List<Object> resolveUnspecifiedParameterValues(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement, final List<Object> params) {
        if (!containsUnspecifiedParameterValue(params)) {
            return params;
        }
        List<Object> result = new ArrayList<>(params);
        for (Entry<Integer, Collection<Integer>> entry : getParameterColumnTypes(connectionSession, preparedStatement).entrySet()) {
            int parameterIndex = entry.getKey();
            if (parameterIndex < result.size() && result.get(parameterIndex) instanceof PostgreSQLTypeUnspecifiedSQLParameter) {
                result.set(parameterIndex, resolveUnspecifiedParameterValue(result.get(parameterIndex), entry.getValue()));
            }
        }
        return result;
    }
    
    private static boolean containsUnspecifiedParameterValue(final Collection<Object> params) {
        for (Object each : params) {
            if (each instanceof PostgreSQLTypeUnspecifiedSQLParameter) {
                return true;
            }
        }
        return false;
    }
    
    private static Map<Integer, Collection<Integer>> getParameterColumnTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) {
        Map<Integer, Collection<Integer>> parameterMarkerColumnTypes = getParameterMarkerColumnTypes(connectionSession, preparedStatement);
        Map<Integer, Collection<Integer>> result = new HashMap<>(preparedStatement.getParameterTypes().size(), 1F);
        Collection<Integer> parameterIndexesWithoutColumnType = new HashSet<>();
        int parameterMarkerIndex = 0;
        for (int each : preparedStatement.getActualParameterMarkerIndexes()) {
            Collection<Integer> columnTypes = parameterMarkerColumnTypes.get(parameterMarkerIndex++);
            if (null == columnTypes) {
                parameterIndexesWithoutColumnType.add(each);
            } else {
                result.computeIfAbsent(each, unused -> new HashSet<>()).addAll(columnTypes);
            }
        }
        result.keySet().removeAll(parameterIndexesWithoutColumnType);
        return result;
    }
    
    private static Map<Integer, Collection<Integer>> getParameterMarkerColumnTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase(connectionSession.getUsedDatabaseName());
        Map<Integer, Collection<Integer>> result = new HashMap<>(preparedStatement.getActualParameterMarkerIndexes().size(), 1F);
        SQLStatement sqlStatement = preparedStatement.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement) {
            putInsertValuesColumnTypes(database, (InsertStatement) sqlStatement, result);
        }
        if (sqlStatement instanceof UpdateStatement) {
            putSetAssignmentColumnTypes(database, (UpdateStatement) sqlStatement, result);
        }
        for (WhereSegment each : SQLStatementContextExtractor.getAllWhereSegments(preparedStatement.getSqlStatementContext())) {
            putWhereColumnTypes(database, each, result);
        }
        return result;
    }
    
    private static void putInsertValuesColumnTypes(final ShardingSphereDatabase database, final InsertStatement insertStatement, final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        String schemaName = getSchemaNameOfInsertStatement(database, insertStatement);
        String logicTableName = getTableNameOfInsertStatement(insertStatement);
        if (!database.containsSchema(schemaName) || !database.getSchema(schemaName).containsTable(logicTableName)) {
            return;
        }
        ShardingSphereTable table = database.getSchema(schemaName).getTable(logicTableName);
        Collection<ShardingSphereIdentifier> columnNamesOfInsert = getColumnNamesOfInsertStatement(insertStatement, table);
        for (InsertValuesSegment each : insertStatement.getValues()) {
            putInsertValueColumnTypes(each, table, columnNamesOfInsert, parameterMarkerColumnTypes);
        }
    }
    
    private static String getSchemaNameOfInsertStatement(final ShardingSphereDatabase database, final InsertStatement insertStatement) {
        return insertStatement.getTable().flatMap(SimpleTableSegment::getOwner).map(optional -> optional.getIdentifier().getValue()).orElseGet(database::getDefaultSchemaName);
    }
    
    private static String getTableNameOfInsertStatement(final InsertStatement insertStatement) {
        return insertStatement.getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
    }
    
    private static List<ShardingSphereIdentifier> getColumnNamesOfInsertStatement(final InsertStatement insertStatement, final ShardingSphereTable table) {
        if (insertStatement.getColumns().isEmpty()) {
            return table.getColumnNames();
        }
        List<ShardingSphereIdentifier> result = new ArrayList<>(insertStatement.getColumns().size());
        for (ColumnSegment each : insertStatement.getColumns()) {
            result.add(new ShardingSphereIdentifier(each.getIdentifier().getValue()));
        }
        return result;
    }
    
    private static void putInsertValueColumnTypes(final InsertValuesSegment insertValuesSegment, final ShardingSphereTable table, final Collection<ShardingSphereIdentifier> columnNamesOfInsert,
                                                  final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        Iterator<ShardingSphereIdentifier> columnNames = columnNamesOfInsert.iterator();
        for (ExpressionSegment each : insertValuesSegment.getValues()) {
            if (!columnNames.hasNext()) {
                return;
            }
            String columnName = columnNames.next().toString();
            if (each instanceof ParameterMarkerExpressionSegment && table.containsColumn(columnName)) {
                putParameterColumnType((ParameterMarkerExpressionSegment) each, table.getColumn(columnName).getDataType(), parameterMarkerColumnTypes);
            }
        }
    }
    
    private static void putParameterColumnType(final ParameterMarkerExpressionSegment parameterMarker, final int columnType, final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        parameterMarkerColumnTypes.computeIfAbsent(parameterMarker.getParameterMarkerIndex(), unused -> new HashSet<>()).add(columnType);
    }
    
    private static void putSetAssignmentColumnTypes(final ShardingSphereDatabase database, final UpdateStatement updateStatement, final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        for (ColumnAssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            if (1 == each.getColumns().size()) {
                putColumnType(database, each.getColumns().get(0), each.getValue(), parameterMarkerColumnTypes);
            }
        }
    }
    
    private static void putColumnType(final ShardingSphereDatabase database, final ExpressionSegment column, final ExpressionSegment value,
                                      final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        if (column instanceof ColumnSegment && value instanceof ParameterMarkerExpressionSegment) {
            findColumnType(database, ((ColumnSegment) column).getColumnBoundInfo())
                    .ifPresent(columnType -> putParameterColumnType((ParameterMarkerExpressionSegment) value, columnType, parameterMarkerColumnTypes));
        }
    }
    
    private static Optional<Integer> findColumnType(final ShardingSphereDatabase database, final ColumnSegmentBoundInfo columnBoundInfo) {
        if (TableSourceType.PHYSICAL_TABLE != columnBoundInfo.getTableSourceType()) {
            return Optional.empty();
        }
        if (!database.containsSchema(columnBoundInfo.getOriginalSchema()) || !database.getSchema(columnBoundInfo.getOriginalSchema()).containsTable(columnBoundInfo.getOriginalTable())) {
            return Optional.empty();
        }
        ShardingSphereTable table = database.getSchema(columnBoundInfo.getOriginalSchema()).getTable(columnBoundInfo.getOriginalTable());
        return table.containsColumn(columnBoundInfo.getOriginalColumn()) ? Optional.of(table.getColumn(columnBoundInfo.getOriginalColumn()).getDataType()) : Optional.empty();
    }
    
    private static void putWhereColumnTypes(final ShardingSphereDatabase database, final WhereSegment whereSegment, final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        for (ExpressionSegment each : ExpressionExtractor.extractAllExpressions(whereSegment.getExpr())) {
            putPredicateColumnTypes(database, each, parameterMarkerColumnTypes);
        }
    }
    
    private static void putPredicateColumnTypes(final ShardingSphereDatabase database, final ExpressionSegment predicate, final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        if (predicate instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryOperationExpression = (BinaryOperationExpression) predicate;
            if (COMPARE_OPERATORS.contains(binaryOperationExpression.getOperator())) {
                putColumnType(database, binaryOperationExpression.getLeft(), binaryOperationExpression.getRight(), parameterMarkerColumnTypes);
                putColumnType(database, binaryOperationExpression.getRight(), binaryOperationExpression.getLeft(), parameterMarkerColumnTypes);
            }
            return;
        }
        if (predicate instanceof InExpression) {
            putInColumnTypes(database, (InExpression) predicate, parameterMarkerColumnTypes);
            return;
        }
        if (predicate instanceof BetweenExpression) {
            BetweenExpression betweenExpression = (BetweenExpression) predicate;
            putColumnType(database, betweenExpression.getLeft(), betweenExpression.getBetweenExpr(), parameterMarkerColumnTypes);
            putColumnType(database, betweenExpression.getLeft(), betweenExpression.getAndExpr(), parameterMarkerColumnTypes);
        }
    }
    
    private static void putInColumnTypes(final ShardingSphereDatabase database, final InExpression inExpression, final Map<Integer, Collection<Integer>> parameterMarkerColumnTypes) {
        Collection<ExpressionSegment> values = inExpression.getExpressionList();
        if (!isParameterMarkersOrLiterals(values)) {
            return;
        }
        for (ExpressionSegment each : values) {
            putColumnType(database, inExpression.getLeft(), each, parameterMarkerColumnTypes);
        }
    }
    
    private static boolean isParameterMarkersOrLiterals(final Collection<ExpressionSegment> values) {
        for (ExpressionSegment each : values) {
            if (!(each instanceof ParameterMarkerExpressionSegment) && !(each instanceof LiteralExpressionSegment)) {
                return false;
            }
        }
        return true;
    }
    
    private static Object resolveUnspecifiedParameterValue(final Object parameter, final Collection<Integer> columnTypes) {
        if (1 != columnTypes.size() || !INTEGER_COLUMN_JDBC_TYPES.contains(columnTypes.iterator().next())) {
            return parameter;
        }
        int columnType = columnTypes.iterator().next();
        Matcher matcher = (Types.BIGINT == columnType ? BIGINT_TEXT_PATTERN : INTEGER_TEXT_PATTERN).matcher(parameter.toString());
        if (!matcher.matches()) {
            return parameter;
        }
        try {
            return PostgreSQLBinaryColumnType.valueOfJDBCType(columnType).getTextValueParser().parse(matcher.group(1));
        } catch (final NumberFormatException ignored) {
            return parameter;
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
                preparedStatement.getParameterTypes().set(parameterIndex,
                        PostgreSQLBinaryColumnType.valueOfJDBCType(parameterMetaData.getParameterType(i + 1), parameterMetaData.getParameterTypeName(i + 1)));
            }
        }
    }
    
    private static boolean hasUnspecifiedParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement) {
        for (int each : preparedStatement.getActualParameterMarkerIndexes()) {
            if (PostgreSQLBinaryColumnType.UNSPECIFIED == preparedStatement.getParameterTypes().get(each)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Resolve unspecified parameter types of insert statement by table metadata.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     */
    public static void resolveInsertParameterTypes(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) {
        InsertStatement insertStatement = (InsertStatement) preparedStatement.getSqlStatementContext().getSqlStatement();
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase(connectionSession.getUsedDatabaseName());
        String logicTableName = getTableNameOfInsertStatement(insertStatement);
        ShardingSphereTable table = database.getSchema(getSchemaNameOfInsertStatement(database, insertStatement)).getTable(logicTableName);
        List<ShardingSphereIdentifier> columnNamesOfInsert = getColumnNamesOfInsertStatement(insertStatement, table);
        for (InsertValuesSegment each : insertStatement.getValues()) {
            resolveInsertValuesParameterTypes(preparedStatement, each, logicTableName, table, columnNamesOfInsert);
        }
    }
    
    private static void resolveInsertValuesParameterTypes(final PostgreSQLServerPreparedStatement preparedStatement, final InsertValuesSegment insertValuesSegment,
                                                          final String logicTableName, final ShardingSphereTable table, final List<ShardingSphereIdentifier> columnNamesOfInsert) {
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
            preparedStatement.getParameterTypes().set(parameterIndex, PostgreSQLBinaryColumnType.valueOfJDBCType(table.getColumn(columnName).getDataType()));
        }
    }
}
