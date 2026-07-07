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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupByMemoryMergedResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQL92");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertNextForResultSetsAllEmpty() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult()), createSelectStatementContext(), database, mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForSomeResultSetsEmpty() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        QueryResult queryResult2 = createQueryResult();
        QueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private SelectStatementContext createSelectStatementContext() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(num)"));
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .groupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))))
                .build();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
    }
    
    private SelectStatementContext createSelectStatementContext(final ShardingSphereDatabase database) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, NullsOrderType.FIRST))))
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))))
                .build();
        return new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
    }
    
    @Test
    void assertNextForAggregationResultSetsEmpty() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(20);
        QueryResult queryResult2 = createQueryResult();
        QueryResult queryResult3 = createQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(30)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(4, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(5);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getMetaData().getColumnLabel(2)).thenReturn("AVG(num)");
        when(result.getMetaData().getColumnLabel(3)).thenReturn("id");
        when(result.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getMetaData().getColumnLabel(5)).thenReturn("AVG_DERIVED_SUM_0");
        return result;
    }
    
    @Test
    void assertNextForDistinctShorthandResultSetsEmpty() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(2);
        when(queryResult.getMetaData().getColumnLabel(1)).thenReturn("order_id");
        when(queryResult.getMetaData().getColumnLabel(2)).thenReturn("content");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.getTable(new IdentifierValue("t_order"))).thenReturn(table);
        when(schema.containsTable(new IdentifierValue("t_order"))).thenReturn(true);
        when(table.getAllColumns()).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getSchema("foo_db")).thenReturn(schema);
        when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        ShardingDQLResultMerger merger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = merger.merge(Arrays.asList(queryResult, queryResult, queryResult), createSelectStatementContext(database), database, mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForEmptyResultWithWindowAggregation() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createEmptyQueryResultWithWindowAggregation();
        QueryResult queryResult2 = createEmptyQueryResultWithWindowAggregation();
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2), createSelectStatementContextForWindowAggregation(), database, mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    private QueryResult createEmptyQueryResultWithWindowAggregation() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("c_5");
        when(result.next()).thenReturn(false);
        return result;
    }
    
    private SelectStatementContext createSelectStatementContextForWindowAggregation() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(
                0, 0, AggregationType.MAX, "pg_catalog.max(ref_0.c36) over (partition by ref_0.c39 order by ref_0.vkey desc)");
        aggregationProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("c_5")));
        aggregationProjectionSegment.setWindow(new WindowItemSegment(29, 86));
        projectionsSegment.getProjections().add(aggregationProjectionSegment);
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(projectionsSegment).build();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
    }
    
    @Test
    void assertNextForEmptyResultWithCountAndGroupBy() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createEmptyQueryResultWithCountGroupBy();
        QueryResult queryResult2 = createEmptyQueryResultWithCountGroupBy();
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2), createSelectStatementContextForCountGroupBy(), database, mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForEmptyResultWithCountGroupByDifferentOrderBy() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult = createEmptyQueryResultWithCountGroupBy();
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Collections.singletonList(queryResult), createSelectStatementContextForCountGroupByDifferentOrderBy(), database, mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    private QueryResult createEmptyQueryResultWithCountGroupBy() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(3);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getMetaData().getColumnLabel(2)).thenReturn("user_id");
        when(result.getMetaData().getColumnLabel(3)).thenReturn("order_id");
        when(result.next()).thenReturn(false);
        return result;
    }
    
    private SelectStatementContext createSelectStatementContextForCountGroupBy() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .groupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))))
                .projections(projectionsSegment)
                .build();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
    }
    
    private SelectStatementContext createSelectStatementContextForCountGroupByDifferentOrderBy() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .groupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST))))
                .projections(projectionsSegment)
                .build();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
    }
    
    @Test
    void assertGroupByVarBinaryFromMultipleResults() throws SQLException {
        when(database.getName()).thenReturn("db_schema");
        QueryResult queryResult1 = createGroupByVarBinaryQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(1);
        when(queryResult1.getValue(2, Object.class)).thenReturn(new byte[]{0x10});
        QueryResult queryResult2 = createGroupByVarBinaryQueryResult();
        when(queryResult2.next()).thenReturn(true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(1);
        when(queryResult2.getValue(2, Object.class)).thenReturn(new byte[]{0x10});
        QueryResult queryResult3 = createGroupByVarBinaryQueryResult();
        when(queryResult3.next()).thenReturn(true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(1);
        when(queryResult3.getValue(2, Object.class)).thenReturn(new byte[]{(byte) 0x80});
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContextForCountGroupBy(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat((byte[]) actual.getValue(2, Object.class), is(new byte[]{0x10}));
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(2)));
        assertTrue(actual.next());
        assertThat((byte[]) actual.getValue(2, Object.class), is(new byte[]{(byte) 0x80}));
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(1)));
        assertFalse(actual.next());
    }
    
    private QueryResult createGroupByVarBinaryQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(2);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getMetaData().getColumnLabel(2)).thenReturn("varbinary_col");
        return result;
    }
    
    @Test
    void assertDistinctVarBinaryFromMultipleResults() throws SQLException {
        when(database.getName()).thenReturn("foo_db");
        QueryResult queryResult1 = createDistinctVarBinaryQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(new byte[]{0x10});
        QueryResult queryResult2 = createDistinctVarBinaryQueryResult();
        when(queryResult2.next()).thenReturn(true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(new byte[]{0x10});
        QueryResult queryResult3 = createDistinctVarBinaryQueryResult();
        when(queryResult3.next()).thenReturn(true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(new byte[]{(byte) 0x80});
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(
                Arrays.asList(queryResult1, queryResult2, queryResult3), createDistinctVarBinaryContext(), database, mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat((byte[]) actual.getValue(1, Object.class), is(new byte[]{0x10}));
        assertTrue(actual.next());
        assertThat((byte[]) actual.getValue(1, Object.class), is(new byte[]{(byte) 0x80}));
        assertFalse(actual.next());
    }
    
    @Test
    void assertMergeWithIfNullExpression() throws SQLException {
        QueryResult queryResult = createIfNullQueryResult(null);
        GroupByMemoryMergedResult actual = new GroupByMemoryMergedResult(
                Collections.singletonList(queryResult), createIfNullSelectStatementContext(), createIfNullSchemaMock());
        
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("0"));
    }
    
    @Test
    void assertMergeWithIfNullExpressionWhenSumIsNotNull() throws SQLException {
        QueryResult queryResult = createIfNullQueryResult(15);
        GroupByMemoryMergedResult actual = new GroupByMemoryMergedResult(
                Collections.singletonList(queryResult), createIfNullSelectStatementContext(), createIfNullSchemaMock());
        
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("15"));
    }
    
    @Test
    void assertMergeWithIfNullAvgExpression() throws SQLException {
        FunctionSegment functionSegment = new FunctionSegment(0, 21, "IFNULL", "IFNULL(AVG(price), 0)");
        AggregationProjectionSegment avgSegment = new AggregationProjectionSegment(7, 16, AggregationType.AVG, "AVG(price)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(19, 19, 0);
        functionSegment.getParameters().add(avgSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 21, "IFNULL(AVG(price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 21);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(28, 34, new IdentifierValue("t_order")));
        
        GroupBySegment groupBySegment = new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST)));
        OrderBySegment orderBySegment = new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST)));
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .from(tableSegment)
                .groupBy(groupBySegment)
                .orderBy(orderBySegment)
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        
        AggregationProjection avgProjection = selectStatementContext.getProjectionsContext().getAggregationProjections().get(0);
        avgProjection.setIndex(2);
        avgProjection.getDerivedAggregationProjections().get(0).setIndex(3);
        avgProjection.getDerivedAggregationProjections().get(1).setIndex(4);
        
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsTable(any(IdentifierValue.class))).thenReturn(true);
        
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.getTable(any(IdentifierValue.class))).thenReturn(table);
        when(table.containsColumn(anyString())).thenReturn(false);
        
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(5);
        
        when(queryResult.getMetaData().getColumnLabel(1)).thenReturn("IFNULL(AVG(price), 0)");
        when(queryResult.getMetaData().getColumnName(1)).thenReturn("ifnull_col");
        
        when(queryResult.getMetaData().getColumnLabel(2)).thenReturn("AVG(price)");
        when(queryResult.getMetaData().getColumnName(2)).thenReturn("avg_col");
        
        when(queryResult.getMetaData().getColumnLabel(3)).thenReturn("AVG_DERIVED_COUNT_0");
        when(queryResult.getMetaData().getColumnName(3)).thenReturn("avg_derived_count_0");
        
        when(queryResult.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_SUM_0");
        when(queryResult.getMetaData().getColumnName(4)).thenReturn("avg_derived_sum_0");
        
        when(queryResult.getMetaData().getColumnLabel(5)).thenReturn("user_id");
        when(queryResult.getMetaData().getColumnName(5)).thenReturn("user_id");
        
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getValue(1, Object.class)).thenReturn(null);
        when(queryResult.getValue(2, Object.class)).thenReturn(new BigDecimal("5"));
        when(queryResult.getValue(3, Object.class)).thenReturn(2);
        when(queryResult.getValue(4, Object.class)).thenReturn(10);
        when(queryResult.getValue(5, Object.class)).thenReturn(100);
        
        GroupByMemoryMergedResult actual = new GroupByMemoryMergedResult(Collections.singletonList(queryResult), selectStatementContext, schema);
        
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("5.0000"));
    }
    
    @Test
    void assertMergeWithCoalesceAvgExpression() throws SQLException {
        FunctionSegment functionSegment = new FunctionSegment(0, 23, "COALESCE", "COALESCE(AVG(price), 0)");
        AggregationProjectionSegment avgSegment = new AggregationProjectionSegment(9, 18, AggregationType.AVG, "AVG(price)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(21, 21, 0);
        functionSegment.getParameters().add(avgSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 23, "COALESCE(AVG(price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 23);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(30, 36, new IdentifierValue("t_order")));
        
        GroupBySegment groupBySegment = new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST)));
        OrderBySegment orderBySegment = new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST)));
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .from(tableSegment)
                .groupBy(groupBySegment)
                .orderBy(orderBySegment)
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        
        org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection avgProjection =
                selectStatementContext.getProjectionsContext().getAggregationProjections().get(0);
        avgProjection.setIndex(2);
        avgProjection.getDerivedAggregationProjections().get(0).setIndex(3);
        avgProjection.getDerivedAggregationProjections().get(1).setIndex(4);
        
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsTable(any(IdentifierValue.class))).thenReturn(true);
        
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.getTable(any(IdentifierValue.class))).thenReturn(table);
        when(table.containsColumn(anyString())).thenReturn(false);
        
        QueryResult queryResult1 = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult1.getMetaData().getColumnCount()).thenReturn(5);
        when(queryResult1.getMetaData().getColumnLabel(1)).thenReturn("COALESCE(AVG(price), 0)");
        when(queryResult1.getMetaData().getColumnName(1)).thenReturn("coalesce_col");
        when(queryResult1.getMetaData().getColumnLabel(2)).thenReturn("AVG(price)");
        when(queryResult1.getMetaData().getColumnName(2)).thenReturn("avg_col");
        when(queryResult1.getMetaData().getColumnLabel(3)).thenReturn("AVG_DERIVED_COUNT_0");
        when(queryResult1.getMetaData().getColumnName(3)).thenReturn("avg_derived_count_0");
        when(queryResult1.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_SUM_0");
        when(queryResult1.getMetaData().getColumnName(4)).thenReturn("avg_derived_sum_0");
        when(queryResult1.getMetaData().getColumnLabel(5)).thenReturn("user_id");
        when(queryResult1.getMetaData().getColumnName(5)).thenReturn("user_id");
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(null);
        when(queryResult1.getValue(2, Object.class)).thenReturn(new BigDecimal("5"));
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(10);
        when(queryResult1.getValue(5, Object.class)).thenReturn(100);
        
        QueryResult queryResult2 = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult2.getMetaData().getColumnCount()).thenReturn(5);
        when(queryResult2.getMetaData().getColumnLabel(1)).thenReturn("COALESCE(AVG(price), 0)");
        when(queryResult2.getMetaData().getColumnName(1)).thenReturn("coalesce_col");
        when(queryResult2.getMetaData().getColumnLabel(2)).thenReturn("AVG(price)");
        when(queryResult2.getMetaData().getColumnName(2)).thenReturn("avg_col");
        when(queryResult2.getMetaData().getColumnLabel(3)).thenReturn("AVG_DERIVED_COUNT_0");
        when(queryResult2.getMetaData().getColumnName(3)).thenReturn("avg_derived_count_0");
        when(queryResult2.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_SUM_0");
        when(queryResult2.getMetaData().getColumnName(4)).thenReturn("avg_derived_sum_0");
        when(queryResult2.getMetaData().getColumnLabel(5)).thenReturn("user_id");
        when(queryResult2.getMetaData().getColumnName(5)).thenReturn("user_id");
        when(queryResult2.next()).thenReturn(true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(null);
        when(queryResult2.getValue(2, Object.class)).thenReturn(new BigDecimal("10"));
        when(queryResult2.getValue(3, Object.class)).thenReturn(3);
        when(queryResult2.getValue(4, Object.class)).thenReturn(30);
        when(queryResult2.getValue(5, Object.class)).thenReturn(100);
        
        GroupByMemoryMergedResult actual = new GroupByMemoryMergedResult(Arrays.asList(queryResult1, queryResult2), selectStatementContext, schema);
        
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("8.0000"));
    }
    
    @Test
    void assertMergeWithIfNullDistinctExpression() throws SQLException {
        FunctionSegment functionSegment = new FunctionSegment(0, 30, "IFNULL", "IFNULL(SUM(DISTINCT price), 0)");
        AggregationDistinctProjectionSegment sumSegment = new AggregationDistinctProjectionSegment(7, 25, AggregationType.SUM, "SUM(DISTINCT price)", "price");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(29, 29, 0);
        functionSegment.getParameters().add(sumSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 30, "IFNULL(SUM(DISTINCT price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 30);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(37, 43, new IdentifierValue("t_order")));
        
        GroupBySegment groupBySegment = new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST)));
        OrderBySegment orderBySegment = new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST)));
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .from(tableSegment)
                .groupBy(groupBySegment)
                .orderBy(orderBySegment)
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        
        selectStatementContext.getProjectionsContext().getAggregationProjections().get(0).setIndex(2);
        
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsTable(any(IdentifierValue.class))).thenReturn(true);
        
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.getTable(any(IdentifierValue.class))).thenReturn(table);
        when(table.containsColumn(anyString())).thenReturn(false);
        
        QueryResult queryResult1 = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult1.getMetaData().getColumnCount()).thenReturn(3);
        when(queryResult1.getMetaData().getColumnLabel(1)).thenReturn("IFNULL(SUM(DISTINCT price), 0)");
        when(queryResult1.getMetaData().getColumnName(1)).thenReturn("ifnull_col");
        when(queryResult1.getMetaData().getColumnLabel(2)).thenReturn("price");
        when(queryResult1.getMetaData().getColumnName(2)).thenReturn("price");
        when(queryResult1.getMetaData().getColumnLabel(3)).thenReturn("user_id");
        when(queryResult1.getMetaData().getColumnName(3)).thenReturn("user_id");
        
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(null);
        when(queryResult1.getValue(2, Object.class)).thenReturn(10);
        when(queryResult1.getValue(3, Object.class)).thenReturn(100);
        
        QueryResult queryResult2 = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult2.getMetaData().getColumnCount()).thenReturn(3);
        when(queryResult2.getMetaData().getColumnLabel(1)).thenReturn("IFNULL(SUM(DISTINCT price), 0)");
        when(queryResult2.getMetaData().getColumnName(1)).thenReturn("ifnull_col");
        when(queryResult2.getMetaData().getColumnLabel(2)).thenReturn("price");
        when(queryResult2.getMetaData().getColumnName(2)).thenReturn("price");
        when(queryResult2.getMetaData().getColumnLabel(3)).thenReturn("user_id");
        when(queryResult2.getMetaData().getColumnName(3)).thenReturn("user_id");
        when(queryResult2.next()).thenReturn(true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(null);
        when(queryResult2.getValue(2, Object.class)).thenReturn(10);
        when(queryResult2.getValue(3, Object.class)).thenReturn(100);
        
        GroupByMemoryMergedResult actual = new GroupByMemoryMergedResult(Arrays.asList(queryResult1, queryResult2), selectStatementContext, schema);
        
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("10"));
    }
    
    @Test
    void assertMergeWithCoalesceAvgDistinctExpression() throws SQLException {
        FunctionSegment functionSegment = new FunctionSegment(0, 32, "COALESCE", "COALESCE(AVG(DISTINCT price), 0)");
        AggregationDistinctProjectionSegment avgSegment = new AggregationDistinctProjectionSegment(9, 27, AggregationType.AVG, "AVG(DISTINCT price)", "price");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(31, 31, 0);
        functionSegment.getParameters().add(avgSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 32, "COALESCE(AVG(DISTINCT price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 32);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(39, 45, new IdentifierValue("t_order")));
        
        GroupBySegment groupBySegment = new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST)));
        OrderBySegment orderBySegment = new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 5, OrderDirection.ASC, NullsOrderType.FIRST)));
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .from(tableSegment)
                .groupBy(groupBySegment)
                .orderBy(orderBySegment)
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatementContext selectStatementContext = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        
        AggregationProjection avgProjection =
                selectStatementContext.getProjectionsContext().getAggregationProjections().get(0);
        avgProjection.getDerivedAggregationProjections().get(0).setIndex(3);
        avgProjection.getDerivedAggregationProjections().get(1).setIndex(4);
        
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsTable(any(IdentifierValue.class))).thenReturn(true);
        
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.getTable(any(IdentifierValue.class))).thenReturn(table);
        when(table.containsColumn(anyString())).thenReturn(false);
        
        QueryResult queryResult1 = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult1.getMetaData().getColumnCount()).thenReturn(5);
        when(queryResult1.getMetaData().getColumnLabel(1)).thenReturn("COALESCE(AVG(DISTINCT price), 0)");
        when(queryResult1.getMetaData().getColumnName(1)).thenReturn("coalesce_col");
        when(queryResult1.getMetaData().getColumnLabel(2)).thenReturn("price");
        when(queryResult1.getMetaData().getColumnName(2)).thenReturn("price");
        when(queryResult1.getMetaData().getColumnLabel(3)).thenReturn("AVG_DERIVED_COUNT_0");
        when(queryResult1.getMetaData().getColumnName(3)).thenReturn("avg_derived_count_0");
        when(queryResult1.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_SUM_0");
        when(queryResult1.getMetaData().getColumnName(4)).thenReturn("avg_derived_sum_0");
        when(queryResult1.getMetaData().getColumnLabel(5)).thenReturn("user_id");
        when(queryResult1.getMetaData().getColumnName(5)).thenReturn("user_id");
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(null);
        when(queryResult1.getValue(2, Object.class)).thenReturn(new BigDecimal("10"));
        when(queryResult1.getValue(3, Object.class)).thenReturn(1);
        when(queryResult1.getValue(4, Object.class)).thenReturn(10);
        when(queryResult1.getValue(5, Object.class)).thenReturn(100);
        
        QueryResult queryResult2 = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult2.getMetaData().getColumnCount()).thenReturn(5);
        when(queryResult2.getMetaData().getColumnLabel(1)).thenReturn("COALESCE(AVG(DISTINCT price), 0)");
        when(queryResult2.getMetaData().getColumnName(1)).thenReturn("coalesce_col");
        when(queryResult2.getMetaData().getColumnLabel(2)).thenReturn("price");
        when(queryResult2.getMetaData().getColumnName(2)).thenReturn("price");
        when(queryResult2.getMetaData().getColumnLabel(3)).thenReturn("AVG_DERIVED_COUNT_0");
        when(queryResult2.getMetaData().getColumnName(3)).thenReturn("avg_derived_count_0");
        when(queryResult2.getMetaData().getColumnLabel(4)).thenReturn("AVG_DERIVED_SUM_0");
        when(queryResult2.getMetaData().getColumnName(4)).thenReturn("avg_derived_sum_0");
        when(queryResult2.getMetaData().getColumnLabel(5)).thenReturn("user_id");
        when(queryResult2.getMetaData().getColumnName(5)).thenReturn("user_id");
        
        when(queryResult2.next()).thenReturn(true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(null);
        when(queryResult2.getValue(2, Object.class)).thenReturn(new BigDecimal("10"));
        when(queryResult2.getValue(3, Object.class)).thenReturn(1);
        when(queryResult2.getValue(4, Object.class)).thenReturn(10);
        when(queryResult2.getValue(5, Object.class)).thenReturn(100);
        
        GroupByMemoryMergedResult actual = new GroupByMemoryMergedResult(Arrays.asList(queryResult1, queryResult2), selectStatementContext, schema);
        
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("10.0000"));
    }
    
    private SelectStatementContext createIfNullSelectStatementContext() {
        FunctionSegment functionSegment = new FunctionSegment(0, 21, "IFNULL", "IFNULL(SUM(price), 0)");
        AggregationProjectionSegment sumSegment = new AggregationProjectionSegment(7, 16, AggregationType.SUM, "SUM(price)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(19, 19, 0);
        functionSegment.getParameters().add(sumSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 21, "IFNULL(SUM(price), 0)", functionSegment);
        
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 21);
        projectionsSegment.getProjections().add(expressionSegment);
        
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(28, 34, new IdentifierValue("t_order")));
        
        GroupBySegment groupBySegment = new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST)));
        OrderBySegment orderBySegment = new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, NullsOrderType.FIRST)));
        
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .from(tableSegment)
                .groupBy(groupBySegment)
                .orderBy(orderBySegment)
                .build();
        
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        SelectStatementContext context = new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
        
        context.getProjectionsContext().getAggregationProjections().get(0).setIndex(2);
        
        return context;
    }
    
    private ShardingSphereSchema createIfNullSchemaMock() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.containsTable(any(IdentifierValue.class))).thenReturn(true);
        
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("t_order")).thenReturn(table);
        when(schema.getTable(any(IdentifierValue.class))).thenReturn(table);
        when(table.containsColumn(anyString())).thenReturn(false);
        
        return schema;
    }
    
    private QueryResult createIfNullQueryResult(final Integer sumValue) throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(3);
        
        when(queryResult.getMetaData().getColumnLabel(1)).thenReturn("IFNULL(SUM(price), 0)");
        when(queryResult.getMetaData().getColumnName(1)).thenReturn("ifnull_col");
        
        when(queryResult.getMetaData().getColumnLabel(2)).thenReturn("EXPR_DERIVED_0");
        when(queryResult.getMetaData().getColumnName(2)).thenReturn("expr_derived_0");
        
        when(queryResult.getMetaData().getColumnLabel(3)).thenReturn("user_id");
        when(queryResult.getMetaData().getColumnName(3)).thenReturn("user_id");
        
        when(queryResult.next()).thenReturn(true, false);
        when(queryResult.getValue(1, Object.class)).thenReturn(null);
        when(queryResult.getValue(2, Object.class)).thenReturn(sumValue);
        when(queryResult.getValue(3, Object.class)).thenReturn(100);
        
        return queryResult;
    }
    
    private QueryResult createDistinctVarBinaryQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("varbinary_col");
        return result;
    }
    
    private SelectStatementContext createDistinctVarBinaryContext() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("varbinary_col"))));
        SelectStatement selectStatement = SelectStatement.builder()
                .databaseType(databaseType)
                .projections(projectionsSegment)
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST))))
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))))
                .build();
        return new SelectStatementContext(
                selectStatement, new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock()), "foo_db", Collections.emptyList());
    }
}
