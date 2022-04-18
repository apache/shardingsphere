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

import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByMemoryMergedResultTest {
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(createQueryResult(), createQueryResult(), createQueryResult()), createSelectStatementContext(), null);
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(0));
        assertNull(actual.getValue(2, Object.class));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForSomeResultSetsEmpty() throws SQLException {
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
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), null);
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
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.AVG, "(num)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDefaultSchema()).thenReturn(mock(ShardingSphereSchema.class));
        selectStatement.setProjections(projectionsSegment);
        return new SelectStatementContext(Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData), Collections.emptyList(), selectStatement, DefaultSchema.LOGIC_NAME);
    }
    
    private SelectStatementContext createSelectStatementContext(final ShardingSphereMetaData metaData) {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.DESC, OrderDirection.ASC))));
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        selectStatement.setProjections(projectionsSegment);
        return new SelectStatementContext(Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData), Collections.emptyList(), selectStatement, DefaultSchema.LOGIC_NAME);
    }

    @Test
    public void assertNextForAggregationResultSetsEmpty() throws SQLException {
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
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), null);
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
    public void assertNextForDistinctShorthandResultSetsEmpty() throws SQLException {
        QueryResult queryResult = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(queryResult.getMetaData().getColumnCount()).thenReturn(2);
        when(queryResult.getMetaData().getColumnLabel(1)).thenReturn("order_id");
        when(queryResult.getMetaData().getColumnLabel(2)).thenReturn("content");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        TableMetaData tableMetaData = mock(TableMetaData.class);
        when(schema.get("t_order")).thenReturn(tableMetaData);
        when(tableMetaData.getColumns()).thenReturn(Collections.emptyMap());
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchemaByName(DefaultSchema.LOGIC_NAME)).thenReturn(schema);
        when(schema.getAllColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "content"));
        ShardingDQLResultMerger merger = new ShardingDQLResultMerger(DatabaseTypeRegistry.getActualDatabaseType("MySQL"));
        MergedResult actual = merger.merge(Arrays.asList(queryResult, queryResult, queryResult), createSelectStatementContext(metaData), schema);
        assertFalse(actual.next());
    }
}
