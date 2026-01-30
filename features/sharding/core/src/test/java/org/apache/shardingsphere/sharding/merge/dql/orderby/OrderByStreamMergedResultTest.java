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

package org.apache.shardingsphere.sharding.merge.dql.orderby;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.merge.dql.ShardingDQLResultMerger;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderByStreamMergedResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQL92");
    
    private SelectStatementContext selectStatementContext;
    
    @BeforeEach
    void setUp() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 13, new IdentifierValue("tbl")));
        selectStatement.setFrom(tableSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setOrderBy(new OrderBySegment(0, 0, Arrays.asList(
                new IndexOrderByItemSegment(0, 0, 1, OrderDirection.ASC, NullsOrderType.FIRST),
                new IndexOrderByItemSegment(0, 0, 2, OrderDirection.ASC, NullsOrderType.FIRST))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatementContext = new SelectStatementContext(selectStatement, createShardingSphereMetaData(), "foo_db", Collections.emptyList());
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        return new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertNextForResultSetsAllEmpty() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS), mock(QueryResult.class, RETURNS_DEEP_STUBS));
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(queryResults, selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForSomeResultSetsEmpty() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class), mock(QueryResult.class), mock(QueryResult.class));
        for (int i = 0; i < 3; i++) {
            QueryResultMetaData metaData = mock(QueryResultMetaData.class);
            when(queryResults.get(i).getMetaData()).thenReturn(metaData);
            when(metaData.getColumnName(1)).thenReturn("col1");
            when(metaData.getColumnName(2)).thenReturn("col2");
        }
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        when(queryResults.get(0).next()).thenReturn(true, false);
        when(queryResults.get(0).getValue(1, Object.class)).thenReturn("2");
        when(queryResults.get(2).next()).thenReturn(true, true, false);
        when(queryResults.get(2).getValue(1, Object.class)).thenReturn("1", "1", "3", "3");
        MergedResult actual = resultMerger.merge(queryResults, selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("2"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("3"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForMix() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class), mock(QueryResult.class), mock(QueryResult.class));
        for (int i = 0; i < 3; i++) {
            QueryResultMetaData metaData = mock(QueryResultMetaData.class);
            when(queryResults.get(i).getMetaData()).thenReturn(metaData);
            when(metaData.getColumnName(1)).thenReturn("col1");
            when(metaData.getColumnName(2)).thenReturn("col2");
        }
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        when(queryResults.get(0).next()).thenReturn(true, false);
        when(queryResults.get(0).getValue(1, Object.class)).thenReturn("2");
        when(queryResults.get(1).next()).thenReturn(true, true, true, false);
        when(queryResults.get(1).getValue(1, Object.class)).thenReturn("2", "2", "3", "3", "4", "4");
        when(queryResults.get(2).next()).thenReturn(true, true, false);
        when(queryResults.get(2).getValue(1, Object.class)).thenReturn("1", "1", "3", "3");
        MergedResult actual = resultMerger.merge(queryResults, selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("1"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("2"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("2"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("3"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("3"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("4"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForCaseSensitive() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class), mock(QueryResult.class), mock(QueryResult.class));
        for (int i = 0; i < 3; i++) {
            QueryResultMetaData metaData = mock(QueryResultMetaData.class);
            when(queryResults.get(i).getMetaData()).thenReturn(metaData);
            when(metaData.getColumnName(1)).thenReturn("col1");
            when(metaData.getColumnName(2)).thenReturn("col2");
        }
        when(queryResults.get(0).next()).thenReturn(true, false);
        when(queryResults.get(0).getValue(1, Object.class)).thenReturn("b");
        when(queryResults.get(1).next()).thenReturn(true, true, false);
        when(queryResults.get(1).getValue(1, Object.class)).thenReturn("B", "B", "a", "a");
        when(queryResults.get(2).next()).thenReturn(true, false);
        when(queryResults.get(2).getValue(1, Object.class)).thenReturn("A");
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(queryResults, selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("A"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("B"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("a"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class).toString(), is("b"));
        assertFalse(actual.next());
    }
    
    @Test
    void assertNextForCaseInsensitive() throws SQLException {
        List<QueryResult> queryResults = Arrays.asList(mock(QueryResult.class), mock(QueryResult.class), mock(QueryResult.class));
        for (int i = 0; i < 3; i++) {
            QueryResultMetaData metaData = mock(QueryResultMetaData.class);
            when(queryResults.get(i).getMetaData()).thenReturn(metaData);
            when(metaData.getColumnName(1)).thenReturn("col1");
            when(metaData.getColumnName(2)).thenReturn("col2");
        }
        when(queryResults.get(0).next()).thenReturn(true, false);
        when(queryResults.get(0).getValue(2, Object.class)).thenReturn("b");
        when(queryResults.get(1).next()).thenReturn(true, true, false);
        when(queryResults.get(1).getValue(2, Object.class)).thenReturn("a", "a", "B", "B");
        when(queryResults.get(2).next()).thenReturn(true, false);
        when(queryResults.get(2).getValue(2, Object.class)).thenReturn("A");
        ShardingDQLResultMerger resultMerger = new ShardingDQLResultMerger(databaseType);
        MergedResult actual = resultMerger.merge(queryResults, selectStatementContext, createDatabase(), mock(ConnectionContext.class));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, Object.class).toString(), is("a"));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, Object.class).toString(), is("A"));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, Object.class).toString(), is("B"));
        assertTrue(actual.next());
        assertThat(actual.getValue(2, Object.class).toString(), is("b"));
        assertFalse(actual.next());
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("col1", 0, false, false, true, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("col2", 0, false, false, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("tbl", Arrays.asList(column1, column2), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), Collections.singleton(schema));
    }
}
