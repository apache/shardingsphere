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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsertClauseShardingConditionEngineTest {
    
    private InsertClauseShardingConditionEngine shardingConditionEngine;
    
    @Mock
    private ShardingRule rule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InsertStatementContext insertStatementContext;
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mockDatabase();
        InsertStatement insertStatement = createInsertStatement();
        shardingConditionEngine = new InsertClauseShardingConditionEngine(database, rule, mock(TimestampServiceRule.class));
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getColumnNames()).thenReturn(Arrays.asList("foo_col_1", "foo_col_3"));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(createInsertValueContext()));
        when(insertStatementContext.getInsertSelectContext()).thenReturn(null);
        when(insertStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        IdentifierValue fooTable = new IdentifierValue("foo_tbl");
        when(result.getName()).thenReturn("foo_db");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(schema.containsTable(fooTable)).thenReturn(true);
        when(schema.getTable("foo_tbl").findColumnNamesIfNotExistedFrom(new LinkedHashSet<>(Arrays.asList("foo_col_1", "foo_col_3")))).thenReturn(Collections.singleton("foo_col_2"));
        when(schema.getTable(fooTable).findColumnNamesIfNotExistedFrom(new LinkedHashSet<>(Arrays.asList("foo_col_1", "foo_col_3")))).thenReturn(Collections.singleton("foo_col_2"));
        when(result.findDefaultSchema()).thenReturn(Optional.of(schema));
        when(result.getDefaultSchemaName()).thenReturn("foo_default_schema");
        return result;
    }
    
    private InsertStatement createInsertStatement() {
        return InsertStatement.builder().databaseType(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"))
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))).build();
    }
    
    private InsertValueContext createInsertValueContext() {
        return new InsertValueContext(Collections.singleton(new LiteralExpressionSegment(0, 10, "1")), Collections.emptyList(), 0);
    }
    
    private InsertValueContext createInsertValueContextAsCommonExpressionSegmentEmptyText() {
        return new InsertValueContext(Collections.singleton(new CommonExpressionSegment(0, 10, "null", Collections.emptyList())), Collections.emptyList(), 0);
    }
    
    private InsertValueContext createInsertValueContextAsCommonExpressionSegmentWithNow() {
        return new InsertValueContext(Collections.singleton(new CommonExpressionSegment(0, 10, "now()", Collections.emptyList())), Collections.emptyList(), 0);
    }
    
    @Test
    void assertCreateShardingConditionsInsertStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextUsingCommonExpressionSegmentEmpty() {
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(createInsertValueContextAsCommonExpressionSegmentEmptyText()));
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
    }
    
    @Test
    void assertCreateShardingConditionsInsertStatementWithMismatchColumns() {
        InsertValueContext insertValueContext = new InsertValueContext(Arrays.asList(new LiteralExpressionSegment(0, 10, "1"), new LiteralExpressionSegment(0, 10, "1")), Collections.emptyList(), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("foo_col_1"));
        assertThrows(InsertColumnsAndValuesMismatchedException.class, () -> shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()));
    }
    
    @Test
    void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextUsingCommonExpressionSegmentNow() {
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(createInsertValueContextAsCommonExpressionSegmentWithNow()));
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertFalse(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithoutDefaultSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        InsertClauseShardingConditionEngine engine = new InsertClauseShardingConditionEngine(database, rule, mock(TimestampServiceRule.class));
        assertThrows(SchemaNotFoundException.class, () -> engine.createShardingConditions(insertStatementContext, Collections.emptyList()));
    }
    
    @Test
    void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContext() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextAndTableRule() {
        GeneratedKeyContext generatedKeyContext = mock(GeneratedKeyContext.class);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        when(generatedKeyContext.isGenerated()).thenReturn(true);
        when(generatedKeyContext.getGeneratedValues()).thenReturn(Collections.singleton("foo_col_1"));
        when(rule.findShardingTable("foo_tbl")).thenReturn(Optional.of(new ShardingTable(Collections.singleton("foo_col_1"), "test")));
        when(rule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertFalse(shardingConditions.get(0).getValues().isEmpty());
        verify(rule).generateKeys(argThat(each -> "foo_default_schema".equals(each.getSchemaName())), anyInt());
    }
    
    @Test
    void assertCreateShardingConditionsWithParameterMarkers() {
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(new ParameterMarkerExpressionSegment(0, 0, 0)), Collections.singletonList(1), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(1));
        assertThat(shardingConditions.size(), is(1));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
        assertThat(shardingConditions.get(0).getValues().get(0).getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @ParameterizedTest(name = "{0}: {1}, parameter={2}")
    @MethodSource("getIntegerCasts")
    void assertCreateShardingConditionsWithIntegerCast(final String dataType, final Integer value, final boolean parameterized) {
        TypeCastExpression expression = new TypeCastExpression(0, 8, (parameterized ? "?" : value.toString()) + "::" + dataType,
                parameterized ? new ParameterMarkerExpressionSegment(0, 0, 0) : new LiteralExpressionSegment(0, 0, value), dataType);
        List<Object> params = parameterized ? Collections.singletonList(value) : Collections.emptyList();
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(expression), params, 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(insertStatementContext, params);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getValues().size(), is(1));
        ListShardingConditionValue<?> actualValue = (ListShardingConditionValue<?>) actual.get(0).getValues().get(0);
        assertThat(actualValue.getColumnName(), is("foo_col_1"));
        assertThat(actualValue.getTableName(), is("foo_tbl"));
        assertThat(actualValue.getValues(), is(Collections.singletonList(value)));
        assertThat(actualValue.getValues().iterator().next(), isA(Integer.class));
        assertThat(actualValue.getParameterMarkerIndexes(), is(parameterized ? Collections.singletonList(0) : Collections.emptyList()));
        assertThat(insertValueContext.getValueExpressions().get(0), sameInstance(expression));
    }
    
    private static Object[][] getIntegerCasts() {
        return new Object[][]{
                {"int4", 1, true},
                {"int4", Integer.MIN_VALUE, true},
                {"int4", Integer.MAX_VALUE, true},
                {"int", -1, true},
                {"INTEGER", 0, true},
                {"int4", 1, false},
                {"int4", Integer.MIN_VALUE, false},
                {"int4", Integer.MAX_VALUE, false},
                {"int", -1, false},
                {"INTEGER", 0, false}};
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getUnsupportedIntegerCasts")
    void assertCreateShardingConditionsWithUnsupportedIntegerCast(final TypeCastExpression expression, final List<Object> params) {
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(expression), params, 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(insertStatementContext, params);
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(0).getValues().isEmpty());
        assertThat(insertValueContext.getValueExpressions().get(0), sameInstance(expression));
    }
    
    private static Object[][] getUnsupportedIntegerCasts() {
        ParameterMarkerExpressionSegment parameterMarker = new ParameterMarkerExpressionSegment(0, 0, 0);
        TypeCastExpression integerCast = new TypeCastExpression(0, 8, "?::int4", parameterMarker, "int4");
        return new Object[][]{
                {Named.of("string parameter", integerCast), Collections.singletonList("01")},
                {Named.of("decimal parameter", integerCast), Collections.singletonList(BigDecimal.valueOf(15, 1))},
                {Named.of("long parameter", integerCast), Collections.singletonList(1L)},
                {Named.of("null parameter", integerCast), Collections.singletonList(null)},
                {Named.of("text target", new TypeCastExpression(0, 8, "?::text", parameterMarker, "text")), Collections.singletonList(1)},
                {Named.of("numeric modifier", new TypeCastExpression(0, 8, "?::numeric(4,1)", parameterMarker, "numeric(4,1)")), Collections.singletonList(1)},
                {Named.of("user type", new TypeCastExpression(0, 8, "?::foo_type", parameterMarker, "foo_type")), Collections.singletonList(1)},
                {Named.of("nested cast", new TypeCastExpression(0, 14, "?::int4::int4", integerCast, "int4")), Collections.singletonList(1)},
                {Named.of("string literal", new TypeCastExpression(0, 8, "'01'::int4", new LiteralExpressionSegment(0, 3, "01"), "int4")), Collections.emptyList()},
                {Named.of("decimal literal", new TypeCastExpression(0, 8, "1.5::int4", new LiteralExpressionSegment(0, 2, BigDecimal.valueOf(15, 1)), "int4")), Collections.emptyList()},
                {Named.of("long literal", new TypeCastExpression(0, 8, "2147483648::int4", new LiteralExpressionSegment(0, 9, 2147483648L), "int4")), Collections.emptyList()},
                {Named.of("null literal", new TypeCastExpression(0, 8, "null::int4", new LiteralExpressionSegment(0, 3, null), "int4")), Collections.emptyList()}};
    }
    
    @Test
    void assertCreateShardingConditionsWithIntegerParameterCastsInMultipleRows() {
        List<Object> params = Arrays.asList("foo_first", 1, "foo_second", 2);
        InsertValueContext firstRow = new InsertValueContext(Arrays.asList(
                new TypeCastExpression(0, 8, "?::text", new ParameterMarkerExpressionSegment(0, 0, 0), "text"),
                new TypeCastExpression(10, 18, "?::int4", new ParameterMarkerExpressionSegment(10, 10, 1), "int4")), params, 0);
        InsertValueContext secondRow = new InsertValueContext(Arrays.asList(
                new TypeCastExpression(20, 28, "?::text", new ParameterMarkerExpressionSegment(20, 20, 2), "text"),
                new TypeCastExpression(30, 38, "?::int4", new ParameterMarkerExpressionSegment(30, 30, 3), "int4")), params, 2);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Arrays.asList(firstRow, secondRow));
        when(rule.findShardingColumn("foo_col_3", "foo_tbl")).thenReturn(Optional.of("foo_col_3"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(insertStatementContext, params);
        assertThat(actual.size(), is(2));
        assertThat(((ListShardingConditionValue<?>) actual.get(0).getValues().get(0)).getValues(), is(Collections.singletonList(1)));
        assertThat(actual.get(0).getValues().get(0).getParameterMarkerIndexes(), is(Collections.singletonList(1)));
        assertThat(((ListShardingConditionValue<?>) actual.get(1).getValues().get(0)).getValues(), is(Collections.singletonList(2)));
        assertThat(actual.get(1).getValues().get(0).getParameterMarkerIndexes(), is(Collections.singletonList(3)));
    }
    
    @Test
    void assertCreateShardingConditionsSelectStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        when(insertStatementContext.getInsertSelectContext()).thenReturn(mock(InsertSelectContext.class));
        assertTrue(shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()).isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsSelectStatementWithGeneratedKeyContext() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(insertStatementContext.getInsertSelectContext()).thenReturn(mock(InsertSelectContext.class));
        assertTrue(shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()).isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithoutShardingColumn() {
        when(rule.findShardingColumn("foo_col_2", "foo_tbl")).thenReturn(Optional.of("foo_col_2"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getValues().size(), is(1));
        assertThat(actual.get(0).getValues().get(0).getColumnName(), is("foo_col_2"));
        assertThat(actual.get(0).getValues().get(0).getTableName(), is("foo_tbl"));
    }
    
    @Test
    void assertCreateShardingConditionsWithCaseSensitiveField() {
        when(rule.findShardingColumn("foo_Col_3", "foo_tbl")).thenReturn(Optional.of("foo_Col_3"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(actual.size(), is(1));
        
    }
}
