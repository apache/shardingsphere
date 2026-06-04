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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
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
        shardingConditionEngine = new InsertClauseShardingConditionEngine(database, rule, new TimestampServiceRule(new TimestampServiceRuleConfiguration("System", new Properties())));
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
        when(result.getSchema("foo_db")).thenReturn(schema);
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
        return new InsertValueContext(Collections.singleton(new CommonExpressionSegment(0, 10, "null")), Collections.emptyList(), 0);
    }
    
    private InsertValueContext createInsertValueContextAsCommonExpressionSegmentWithNow() {
        return new InsertValueContext(Collections.singleton(new CommonExpressionSegment(0, 10, "now()")), Collections.emptyList(), 0);
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
    
    @Test
    void assertCreateShardingConditionsWithTypeCastParameterMarker() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(7), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(7));
        assertThat(shardingConditions.size(), is(1));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getColumnName(), is("foo_col_1"));
        assertThat(actual.getTableName(), is("foo_tbl"));
        assertThat(actual.getValues(), is(Collections.singletonList(7)));
        assertThat(actual.getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastLiteral() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "1::int4", new LiteralExpressionSegment(0, 10, 1), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.emptyList(), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.size(), is(1));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(1)));
        assertTrue(actual.getParameterMarkerIndexes().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithNestedCompatibleTypeCastParameterMarker() {
        TypeCastExpression inner = new TypeCastExpression(0, 5, "?::int8", new ParameterMarkerExpressionSegment(0, 0, 0), "int8");
        TypeCastExpression outer = new TypeCastExpression(0, 12, "?::int8::int4", inner, "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(outer), Collections.singletonList(42), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(42));
        assertThat(shardingConditions.size(), is(1));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(42)));
        assertThat(actual.getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    void assertCreateShardingConditionsWithNestedIntToTextCastRoutesByCastedString() {
        TypeCastExpression inner = new TypeCastExpression(0, 5, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        TypeCastExpression outer = new TypeCastExpression(0, 12, "?::int4::text", inner, "text");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(outer), Collections.singletonList(42), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(42));
        assertThat(shardingConditions.size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList("42")));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastStringParameterToIntRoutesByCastedInteger() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList("1"), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList("1"));
        assertThat(shardingConditions.size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(1)));
        assertThat(actual.getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastStringParameterToTextRoutes() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::text", new ParameterMarkerExpressionSegment(0, 0, 0), "text");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList("foo"), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList("foo"));
        assertThat(shardingConditions.size(), is(1));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList("foo")));
        assertThat(actual.getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastNullParameterDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(null), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(null));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastLiteralStringToIntRoutesByCastedInteger() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "'1'::int4", new LiteralExpressionSegment(0, 10, "1"), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.emptyList(), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.size(), is(1));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(1)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastBigDecimalPositiveOneHalfRoundsAwayFromZero() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(new BigDecimal("1.5")), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(new BigDecimal("1.5")));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(2)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastBigDecimalPositiveTwoHalfRoundsAwayFromZero() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(new BigDecimal("2.5")), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(new BigDecimal("2.5")));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(3)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastBigDecimalNegativeTwoHalfRoundsAwayFromZero() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(new BigDecimal("-2.5")), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(new BigDecimal("-2.5")));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(-3)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastDoublePositiveTwoHalfRoundsHalfEven() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(2.5D), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(2.5D));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(2)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastDoubleNegativeTwoHalfRoundsHalfEven() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(-2.5D), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(-2.5D));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(-2)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastFloatPositiveTwoHalfRoundsHalfEven() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(2.5F), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(2.5F));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList(2)));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastCharNoTypmodTruncatesToFirstChar() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::char", new ParameterMarkerExpressionSegment(0, 0, 0), "char");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList("ab"), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList("ab"));
        ListShardingConditionValue<?> actual = (ListShardingConditionValue<?>) shardingConditions.get(0).getValues().get(0);
        assertThat(actual.getValues(), is(Collections.singletonList("a")));
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastBooleanToNumericDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::numeric", new ParameterMarkerExpressionSegment(0, 0, 0), "numeric");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(Boolean.TRUE), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(Boolean.TRUE));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastDoubleToBoolDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::bool", new ParameterMarkerExpressionSegment(0, 0, 0), "bool");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(2.5D), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(2.5D));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastStringWithDecimalToIntDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList("1.5"), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList("1.5"));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastTypmodTargetDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::varchar(1)", new ParameterMarkerExpressionSegment(0, 0, 0), "varchar(1)");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList("ab"), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList("ab"));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastOverflowDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList(new BigDecimal("2147483648")), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(new BigDecimal("2147483648")));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastUnparseableStringDoesNotRoute() {
        TypeCastExpression typeCast = new TypeCastExpression(0, 10, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.singletonList("abc"), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList("abc"));
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    void assertCreateShardingConditionsWithTypeCastSubqueryDoesNotCrashAndExtractsNoCondition() {
        SubqueryExpressionSegment subquery = new SubqueryExpressionSegment(new SubquerySegment(0, 9, "(SELECT 1)"));
        TypeCastExpression typeCast = new TypeCastExpression(0, 15, "(SELECT 1)::int4", subquery, "int4");
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singleton(typeCast), Collections.emptyList(), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(rule.findShardingColumn("foo_col_1", "foo_tbl")).thenReturn(Optional.of("foo_col_1"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.size(), is(1));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
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
