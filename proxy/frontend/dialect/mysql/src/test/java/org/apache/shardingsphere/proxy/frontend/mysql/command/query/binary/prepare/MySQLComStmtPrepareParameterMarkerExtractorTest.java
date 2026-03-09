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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLComStmtPrepareParameterMarkerExtractorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        ShardingSphereTable userTable = new ShardingSphereTable("user", Arrays.asList(
                new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, false, false, false),
                new ShardingSphereColumn("age", Types.SMALLINT, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable accountTable = new ShardingSphereTable("account", Arrays.asList(
                new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.BIGINT, false, false, false, false, false, false)), Collections.emptyList(), Collections.emptyList());
        schema = new ShardingSphereSchema("foo_db", databaseType, Arrays.asList(userTable, accountTable), Collections.emptyList());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("resolveColumnsForParameterMarkersArguments")
    void assertResolveColumnsForParameterMarkers(final String name, final String sql, final List<String> expectedColumnNames) {
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(createSqlStatementContext(sql), schema);
        List<ShardingSphereColumn> expectedColumns = createExpectedColumns(schema, expectedColumnNames);
        assertThat(actualColumns, is(expectedColumns));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithNoParameters() {
        assertTrue(MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(new CommonSQLStatementContext(new SQLStatement(databaseType)), schema).isEmpty());
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithNoSpecialStatementContext() {
        SQLStatement sqlStatement = new SQLStatement(databaseType);
        sqlStatement.addParameterMarkers(Collections.singleton(createParameterMarkerExpressionSegment(0)));
        assertThat(MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(new CommonSQLStatementContext(sqlStatement), schema), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithInsertWithoutTable() {
        InsertStatementContext sqlStatementContext = createInsertStatementContext(null, Collections.singletonList(createColumnSegment("id")),
                Collections.singletonList(createParameterMarkerExpressionSegment(0)), Collections.emptyList());
        assertThat(MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithMissingTable() {
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createSimpleTableSegment("unknown_user"), Collections.singletonList(createColumnSegment("id")),
                Collections.singletonList(createParameterMarkerExpressionSegment(0)), Collections.emptyList());
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Collections.singletonList(null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithOnDuplicateKeyUpdate() {
        ColumnAssignmentSegment columnAssignmentSegment = createColumnAssignmentSegment(createColumnSegment("name"), createParameterMarkerExpressionSegment(1));
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createSimpleTableSegment("user"), Collections.singletonList(createColumnSegment("id")),
                Collections.singletonList(createParameterMarkerExpressionSegment(0)), Collections.singletonList(columnAssignmentSegment));
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        List<ShardingSphereColumn> expectedColumns = createExpectedColumns(schema, Arrays.asList("id", "name"));
        assertThat(actualColumns, is(expectedColumns));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithExtraInsertValue() {
        List<ExpressionSegment> values = Arrays.asList(createParameterMarkerExpressionSegment(0), createParameterMarkerExpressionSegment(1), createParameterMarkerExpressionSegment(2));
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createSimpleTableSegment("user"),
                Arrays.asList(createColumnSegment("id"), createColumnSegment("name")), values, Collections.emptyList());
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        List<ShardingSphereColumn> expectedColumns = createExpectedColumns(schema, Arrays.asList("id", "name", null));
        assertThat(actualColumns, is(expectedColumns));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithMissingInsertColumn() {
        InsertStatementContext sqlStatementContext = createInsertStatementContext(createSimpleTableSegment("user"), Collections.singletonList(createColumnSegment("missing_column")),
                Collections.singletonList(createParameterMarkerExpressionSegment(0)), Collections.emptyList());
        assertThrows(ColumnNotFoundException.class, () -> MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithOwnerFallback() {
        ColumnSegment columnSegment = createColumnSegment("name");
        columnSegment.setOwner(createOwnerSegment("user"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, null, TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"),
                Collections.singletonList(createColumnAssignmentSegment(columnSegment, createParameterMarkerExpressionSegment(0))), null);
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        List<ShardingSphereColumn> expectedColumns = createExpectedColumns(schema, Collections.singletonList("name"));
        assertThat(actualColumns, is(expectedColumns));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithSingleTableFallback() {
        ColumnSegment columnSegment = createColumnSegment("age");
        columnSegment.setColumnBoundInfo(null);
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"),
                Collections.singletonList(createColumnAssignmentSegment(columnSegment, createParameterMarkerExpressionSegment(0))), null);
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        List<ShardingSphereColumn> expectedColumns = createExpectedColumns(schema, Collections.singletonList("age"));
        assertThat(actualColumns, is(expectedColumns));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithMultipleTablesWithoutFallback() {
        ColumnSegment columnSegment = createColumnSegment("name");
        columnSegment.setColumnBoundInfo(null);
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createJoinTableSegment("user", "account"), Collections.emptyList(),
                new BinaryOperationExpression(0, 0, columnSegment, createParameterMarkerExpressionSegment(0), "=", "name = ?"));
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Collections.singletonList(null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithResolvedMissingColumn() {
        ColumnSegment columnSegment = createColumnSegment("missing_column");
        columnSegment.setColumnBoundInfo(null);
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"),
                Collections.singletonList(createColumnAssignmentSegment(columnSegment, createParameterMarkerExpressionSegment(0))), null);
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Collections.singletonList(null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithResolvedMissingTable() {
        ColumnSegment columnSegment = createColumnSegment("name");
        columnSegment.setOwner(createOwnerSegment("unknown_user"));
        columnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(null, null, null, TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"),
                Collections.singletonList(createColumnAssignmentSegment(columnSegment, createParameterMarkerExpressionSegment(0))), null);
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Collections.singletonList(null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithInExpressionWithoutColumn() {
        ListExpression listExpression = createListExpression(createParameterMarkerExpressionSegment(0), createParameterMarkerExpressionSegment(1));
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"), Collections.emptyList(),
                new InExpression(0, 0, new LiteralExpressionSegment(0, 0, 1), listExpression, false));
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Arrays.asList(null, null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithBetweenExpressionWithoutColumn() {
        BetweenExpression betweenExpression = new BetweenExpression(0, 0, new LiteralExpressionSegment(0, 0, 1),
                createParameterMarkerExpressionSegment(0), createParameterMarkerExpressionSegment(1), false);
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"), Collections.emptyList(), betweenExpression);
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Arrays.asList(null, null)));
    }
    
    @Test
    void assertResolveColumnsForParameterMarkersWithOutOfRangeParameterIndex() {
        ColumnSegment columnSegment = createColumnSegment("name");
        UpdateStatementContext sqlStatementContext = createUpdateStatementContext(createSimpleTableSegment("user"),
                Collections.singletonList(createColumnAssignmentSegment(columnSegment, createParameterMarkerExpressionSegment(1))), null);
        List<ShardingSphereColumn> actualColumns = MySQLComStmtPrepareParameterMarkerExtractor.resolveColumnsForParameterMarkers(sqlStatementContext, schema);
        assertThat(actualColumns, is(Collections.singletonList(null)));
    }
    
    private static Stream<Arguments> resolveColumnsForParameterMarkersArguments() {
        return Stream.of(
                Arguments.of("insert values", "INSERT INTO user (id, name, age) VALUES (1, ?, ?), (?, 'bar', ?)", Arrays.asList("name", "age", "id", "age")),
                Arguments.of("select equality predicates", "SELECT age FROM user WHERE id = ? AND name = ?", Arrays.asList("id", "name")),
                Arguments.of("select in expression", "SELECT age FROM user WHERE name IN (?, ?)", Arrays.asList("name", "name")),
                Arguments.of("select between expression", "SELECT name FROM user WHERE age BETWEEN ? AND ?", Arrays.asList("age", "age")),
                Arguments.of("update assignments and where", "UPDATE user SET name = ?, age = ? WHERE id = ?", Arrays.asList("name", "age", "id")),
                Arguments.of("delete where", "DELETE FROM user WHERE name = ?", Collections.singletonList("name")));
    }
    
    private SQLStatementContext createSqlStatementContext(final String sql) {
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(0, 0L), new CacheOption(0, 0L)).parse(sql, false);
        return new SQLBindEngine(createMetaData(), "foo_db", new HintValueContext()).bind(sqlStatement);
    }
    
    private InsertStatementContext createInsertStatementContext(final SimpleTableSegment table, final List<ColumnSegment> insertColumns,
                                                                final List<ExpressionSegment> values, final List<ColumnAssignmentSegment> onDuplicateAssignments) {
        InsertStatement insertStatement = InsertStatement.builder()
                .databaseType(databaseType)
                .table(table)
                .insertColumns(insertColumns.isEmpty() ? null : new InsertColumnsSegment(0, 0, insertColumns))
                .values(Collections.singletonList(new InsertValuesSegment(0, 0, values)))
                .onDuplicateKeyColumns(onDuplicateAssignments.isEmpty() ? null : new OnDuplicateKeyColumnsSegment(0, 0, onDuplicateAssignments))
                .build();
        insertStatement.addParameterMarkers(createParameterMarkerSegments(values));
        if (!onDuplicateAssignments.isEmpty()) {
            List<ExpressionSegment> onDuplicateExpressions = new LinkedList<>();
            for (ColumnAssignmentSegment each : onDuplicateAssignments) {
                onDuplicateExpressions.add(each.getValue());
            }
            insertStatement.addParameterMarkers(createParameterMarkerSegments(onDuplicateExpressions));
        }
        return new InsertStatementContext(insertStatement, createMetaData(), "foo_db");
    }
    
    private UpdateStatementContext createUpdateStatementContext(final TableSegment table, final Collection<ColumnAssignmentSegment> assignments, final ExpressionSegment whereExpression) {
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType)
                .table(table)
                .setAssignment(new SetAssignmentSegment(0, 0, assignments))
                .where(null == whereExpression ? null : new WhereSegment(0, 0, whereExpression))
                .build();
        List<ExpressionSegment> expressions = new LinkedList<>();
        for (ColumnAssignmentSegment each : assignments) {
            expressions.add(each.getValue());
        }
        if (null != whereExpression) {
            expressions.add(whereExpression);
        }
        updateStatement.addParameterMarkers(createParameterMarkerSegments(expressions));
        return new UpdateStatementContext(updateStatement);
    }
    
    private List<ShardingSphereColumn> createExpectedColumns(final ShardingSphereSchema schema, final List<String> columnNames) {
        List<ShardingSphereColumn> result = new LinkedList<>();
        for (String each : columnNames) {
            result.add(null == each ? null : schema.getTable("user").getColumn(each));
        }
        return result;
    }
    
    private ShardingSphereMetaData createMetaData() {
        return new ShardingSphereMetaData(Collections.singleton(createDatabase()), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereDatabase createDatabase() {
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
    }
    
    private SimpleTableSegment createSimpleTableSegment(final String tableName) {
        return new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
    }
    
    private JoinTableSegment createJoinTableSegment(final String leftTableName, final String rightTableName) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(createSimpleTableSegment(leftTableName));
        result.setRight(createSimpleTableSegment(rightTableName));
        return result;
    }
    
    private ColumnSegment createColumnSegment(final String columnName) {
        return new ColumnSegment(0, 0, new IdentifierValue(columnName));
    }
    
    private ColumnAssignmentSegment createColumnAssignmentSegment(final ColumnSegment columnSegment, final ExpressionSegment value) {
        return new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment), value);
    }
    
    private ParameterMarkerExpressionSegment createParameterMarkerExpressionSegment(final int parameterIndex) {
        return new ParameterMarkerExpressionSegment(0, 0, parameterIndex);
    }
    
    private Collection<ParameterMarkerSegment> createParameterMarkerSegments(final Collection<ExpressionSegment> expressions) {
        Collection<ParameterMarkerSegment> result = new LinkedList<>();
        for (ExpressionSegment each : expressions) {
            result.addAll(createParameterMarkerSegments(each));
        }
        return result;
    }
    
    private Collection<ParameterMarkerSegment> createParameterMarkerSegments(final ExpressionSegment expression) {
        Collection<ParameterMarkerSegment> result = new LinkedList<>();
        if (expression instanceof ParameterMarkerExpressionSegment) {
            result.add((ParameterMarkerExpressionSegment) expression);
            return result;
        }
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryOperationExpression = (BinaryOperationExpression) expression;
            result.addAll(createParameterMarkerSegments(binaryOperationExpression.getLeft()));
            result.addAll(createParameterMarkerSegments(binaryOperationExpression.getRight()));
            return result;
        }
        if (expression instanceof InExpression) {
            InExpression inExpression = (InExpression) expression;
            result.addAll(createParameterMarkerSegments(inExpression.getLeft()));
            result.addAll(createParameterMarkerSegments(inExpression.getExpressionList()));
            return result;
        }
        if (expression instanceof BetweenExpression) {
            BetweenExpression betweenExpression = (BetweenExpression) expression;
            result.addAll(createParameterMarkerSegments(betweenExpression.getLeft()));
            result.addAll(createParameterMarkerSegments(betweenExpression.getBetweenExpr()));
            result.addAll(createParameterMarkerSegments(betweenExpression.getAndExpr()));
            return result;
        }
        if (expression instanceof ListExpression) {
            result.addAll(createParameterMarkerSegments(((ListExpression) expression).getItems()));
        }
        return result;
    }
    
    private OwnerSegment createOwnerSegment(final String ownerName) {
        return new OwnerSegment(0, 0, new IdentifierValue(ownerName));
    }
    
    private ListExpression createListExpression(final ExpressionSegment... expressions) {
        ListExpression result = new ListExpression(0, 0);
        result.getItems().addAll(Arrays.asList(expressions));
        return result;
    }
}
