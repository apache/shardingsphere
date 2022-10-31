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
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GroupByStreamMergedResultTest {
    
    @Test
    public void assertNextForResultSetsAllEmpty() throws SQLException {
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeFactory.getInstance("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(mockQueryResult(), mockQueryResult(), mockQueryResult()),
                createSelectStatementContext(), createDatabase(), mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForSomeResultSetsEmpty() throws SQLException {
        QueryResult queryResult1 = mockQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(4, Object.class)).thenReturn(new Date(0L));
        when(queryResult1.getValue(5, Object.class)).thenReturn(2);
        when(queryResult1.getValue(6, Object.class)).thenReturn(20);
        QueryResult queryResult2 = mockQueryResult();
        QueryResult queryResult3 = mockQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(20, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(0);
        when(queryResult3.getValue(3, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(4, Object.class)).thenReturn(new Date(0L));
        when(queryResult3.getValue(5, Object.class)).thenReturn(2, 2, 3);
        when(queryResult3.getValue(6, Object.class)).thenReturn(20, 20, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeFactory.getInstance("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), createDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getCalendarValue(4, Date.class, Calendar.getInstance()), is(new Date(0L)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(40)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(30)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getCalendarValue(4, Date.class, Calendar.getInstance()), is(new Date(0L)));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(3)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(30)));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForMix() throws SQLException {
        QueryResult queryResult1 = mockQueryResult();
        when(queryResult1.next()).thenReturn(true, false);
        when(queryResult1.getValue(1, Object.class)).thenReturn(20);
        when(queryResult1.getValue(2, Object.class)).thenReturn(0);
        when(queryResult1.getValue(3, Object.class)).thenReturn(2);
        when(queryResult1.getValue(5, Object.class)).thenReturn(2);
        when(queryResult1.getValue(6, Object.class)).thenReturn(20);
        QueryResult queryResult2 = mockQueryResult();
        when(queryResult2.next()).thenReturn(true, true, true, false);
        when(queryResult2.getValue(1, Object.class)).thenReturn(20, 30, 40);
        when(queryResult2.getValue(2, Object.class)).thenReturn(0);
        when(queryResult2.getValue(3, Object.class)).thenReturn(2, 2, 3, 3, 3, 4);
        when(queryResult2.getValue(5, Object.class)).thenReturn(2, 2, 3, 3, 3, 4);
        when(queryResult2.getValue(6, Object.class)).thenReturn(20, 20, 30, 30, 30, 40);
        QueryResult queryResult3 = mockQueryResult();
        when(queryResult3.next()).thenReturn(true, true, false);
        when(queryResult3.getValue(1, Object.class)).thenReturn(10, 30);
        when(queryResult3.getValue(2, Object.class)).thenReturn(10);
        when(queryResult3.getValue(3, Object.class)).thenReturn(1, 1, 1, 1, 3);
        when(queryResult3.getValue(5, Object.class)).thenReturn(1, 1, 3);
        when(queryResult3.getValue(6, Object.class)).thenReturn(10, 10, 30);
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(DatabaseTypeFactory.getInstance("MySQL"));
        MergedResult actual = resultMerger.merge(Arrays.asList(queryResult1, queryResult2, queryResult3), createSelectStatementContext(), createDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(10)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(1));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(1)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(10)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(2));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(40)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(60)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(3));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(6)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(60)));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is(new BigDecimal(40)));
        assertThat(((BigDecimal) actual.getValue(2, Object.class)).intValue(), is(10));
        assertThat(actual.getValue(3, Object.class), is(4));
        assertThat(actual.getValue(5, Object.class), is(new BigDecimal(4)));
        assertThat(actual.getValue(6, Object.class), is(new BigDecimal(40)));
        assertFalse(actual.next());
    }
    
    private SelectStatementContext createSelectStatementContext() {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl")));
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(tableSegment);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "(*)"));
        projectionsSegment.getProjections().add(new AggregationProjectionSegment(0, 0, AggregationType.AVG, "(num)"));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setGroupBy(new GroupBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC))));
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Collections.singletonList(new IndexOrderByItemSegment(0, 0, 3, OrderDirection.ASC, OrderDirection.ASC))));
        return new SelectStatementContext(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("col1", 0, false, false, false, true);
        ShardingSphereColumn column2 = new ShardingSphereColumn("col2", 0, false, false, false, true);
        ShardingSphereColumn column3 = new ShardingSphereColumn("col3", 0, false, false, false, true);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Arrays.asList(column1, column2, column3), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.singletonMap("tbl", table), Collections.emptyMap());
        return new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, DatabaseTypeFactory.getInstance("MySQL"),
                mock(ShardingSphereResourceMetaData.class), mock(ShardingSphereRuleMetaData.class), Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema));
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(6);
        when(result.getMetaData().getColumnLabel(1)).thenReturn("COUNT(*)");
        when(result.getMetaData().getColumnLabel(2)).thenReturn("AVG(num)");
        when(result.getMetaData().getColumnLabel(3)).thenReturn("id");
        when(result.getMetaData().getColumnLabel(4)).thenReturn("date");
        when(result.getMetaData().getColumnLabel(5)).thenReturn("AVG_DERIVED_COUNT_0");
        when(result.getMetaData().getColumnLabel(6)).thenReturn("AVG_DERIVED_SUM_0");
        when(result.getMetaData().getColumnName(1)).thenReturn("col1");
        when(result.getMetaData().getColumnName(2)).thenReturn("col2");
        when(result.getMetaData().getColumnName(3)).thenReturn("col3");
        return result;
    }
}
