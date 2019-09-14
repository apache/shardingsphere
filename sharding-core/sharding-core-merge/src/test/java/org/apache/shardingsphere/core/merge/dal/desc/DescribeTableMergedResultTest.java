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

package org.apache.shardingsphere.core.merge.dal.desc;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.fixture.DescribeQueryResultFixture;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DescribeTableMergedResultTest {

    private List<QueryResult> queryResults;

    private OptimizedStatement optimizedStatement;

    @Before
    public void setUp() throws SQLException {
        optimizedStatement = mock(OptimizedStatement.class);
        Tables tables = mock(Tables.class);
        when(tables.getSingleTableName()).thenReturn("user");
        when(optimizedStatement.getTables()).thenReturn(tables);
        List<QueryResult> mockQueryResults = mockQueryResults();
        QueryResult queryResult = new DescribeQueryResultFixture(mockQueryResults.iterator());
        queryResults = Lists.newArrayList(queryResult);
    }

    private List<QueryResult> mockQueryResults() throws SQLException {
        List<QueryResult> queryResults = new LinkedList<>();
        queryResults.add(mockQueryResult(6, "id", "int(11) unsigned", "NO", "PRI", "", "auto_increment"));
        queryResults.add(mockQueryResult(6, "name", "varchar(100)", "YES", "", "", ""));
        queryResults.add(mockQueryResult(6, "pre_name", "varchar(100)", "YES", "", "", ""));
        queryResults.add(mockQueryResult(6, "name_assisted", "varchar(100)", "YES", "", "", ""));
        return queryResults;
    }

    private QueryResult mockQueryResult(final int columnNum, final String field, final String type, final String nullValue, final String key, final String defaultValue,
                                        final String extra) throws SQLException {
        QueryResult queryResult = mock(QueryResult.class);
        when(queryResult.getColumnCount()).thenReturn(columnNum);
        when(queryResult.getValue(1, String.class)).thenReturn(field);
        when(queryResult.getValue(1, Object.class)).thenReturn(field);
        when(queryResult.getValue("Field", String.class)).thenReturn(field);
        when(queryResult.getValue("Field", Object.class)).thenReturn(field);
        when(queryResult.getValue(2, String.class)).thenReturn(type);
        when(queryResult.getValue(2, Object.class)).thenReturn(type);
        when(queryResult.getValue("Type", String.class)).thenReturn(type);
        when(queryResult.getValue("Type", Object.class)).thenReturn(type);
        when(queryResult.getValue(3, String.class)).thenReturn(nullValue);
        when(queryResult.getValue(3, Object.class)).thenReturn(nullValue);
        when(queryResult.getValue("Null", String.class)).thenReturn(nullValue);
        when(queryResult.getValue("Null", Object.class)).thenReturn(nullValue);
        when(queryResult.getValue(4, String.class)).thenReturn(key);
        when(queryResult.getValue(4, Object.class)).thenReturn(key);
        when(queryResult.getValue("Key", String.class)).thenReturn(key);
        when(queryResult.getValue("Key", Object.class)).thenReturn(key);
        when(queryResult.getValue(5, String.class)).thenReturn(defaultValue);
        when(queryResult.getValue(5, Object.class)).thenReturn(defaultValue);
        when(queryResult.getValue("Default", String.class)).thenReturn(defaultValue);
        when(queryResult.getValue("Default", Object.class)).thenReturn(defaultValue);
        when(queryResult.getValue(6, String.class)).thenReturn(extra);
        when(queryResult.getValue(6, Object.class)).thenReturn(extra);
        when(queryResult.getValue("Extra", String.class)).thenReturn(extra);
        when(queryResult.getValue("Extra", Object.class)).thenReturn(extra);
        return queryResult;
    }

    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        ShardingRule shardingRule = mock(ShardingRule.class);
        List<QueryResult> queryResults = Collections.emptyList();
        DescribeTableMergedResult describeTableMergedResult = new DescribeTableMergedResult(shardingRule, queryResults, optimizedStatement);
        assertFalse(describeTableMergedResult.next());
    }

    @Test
    public void assertFieldWithEncryptRule() throws SQLException {
        ShardingRule shardingRule = mockShardingRuleWithEncryptRule();
        DescribeTableMergedResult describeTableMergedResult = new DescribeTableMergedResult(shardingRule, queryResults, optimizedStatement);
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "id");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "id");
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "logic_name");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "logic_name");
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "pre_name");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "pre_name");
        assertFalse(describeTableMergedResult.next());
    }

    @Test
    public void assertFieldWithoutEncryptRule() throws SQLException {
        ShardingRule shardingRule = mockShardingRuleWithoutEncryptRule();
        DescribeTableMergedResult describeTableMergedResult = new DescribeTableMergedResult(shardingRule, queryResults, optimizedStatement);
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "id");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "id");
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "name");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "name");
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "pre_name");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "pre_name");
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "name_assisted");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "name_assisted");
    }

    @Test
    public void assertAllWithoutEncryptRule() throws SQLException {
        ShardingRule shardingRule = mockShardingRuleWithoutEncryptRule();
        DescribeTableMergedResult describeTableMergedResult = new DescribeTableMergedResult(shardingRule, queryResults, optimizedStatement);
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "id");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "id");
        assertEquals(describeTableMergedResult.getValue(2, String.class), "int(11) unsigned");
        assertEquals(describeTableMergedResult.getValue("Type", String.class), "int(11) unsigned");
        assertEquals(describeTableMergedResult.getValue(3, String.class), "NO");
        assertEquals(describeTableMergedResult.getValue("Null", String.class), "NO");
        assertEquals(describeTableMergedResult.getValue(4, String.class), "PRI");
        assertEquals(describeTableMergedResult.getValue("Key", String.class), "PRI");
        assertEquals(describeTableMergedResult.getValue(5, String.class), "");
        assertEquals(describeTableMergedResult.getValue("Default", String.class), "");
        assertEquals(describeTableMergedResult.getValue(6, String.class), "auto_increment");
        assertEquals(describeTableMergedResult.getValue("Extra", String.class), "auto_increment");
    }

    @Test
    public void assertAllWithEncryptRule() throws SQLException {
        ShardingRule shardingRule = mockShardingRuleWithEncryptRule();
        DescribeTableMergedResult describeTableMergedResult = new DescribeTableMergedResult(shardingRule, queryResults, optimizedStatement);
        assertTrue(describeTableMergedResult.next());
        assertEquals(describeTableMergedResult.getValue(1, String.class), "id");
        assertEquals(describeTableMergedResult.getValue("Field", String.class), "id");
        assertEquals(describeTableMergedResult.getValue(2, String.class), "int(11) unsigned");
        assertEquals(describeTableMergedResult.getValue("Type", String.class), "int(11) unsigned");
        assertEquals(describeTableMergedResult.getValue(3, String.class), "NO");
        assertEquals(describeTableMergedResult.getValue("Null", String.class), "NO");
        assertEquals(describeTableMergedResult.getValue(4, String.class), "PRI");
        assertEquals(describeTableMergedResult.getValue("Key", String.class), "PRI");
        assertEquals(describeTableMergedResult.getValue(5, String.class), "");
        assertEquals(describeTableMergedResult.getValue("Default", String.class), "");
        assertEquals(describeTableMergedResult.getValue(6, String.class), "auto_increment");
        assertEquals(describeTableMergedResult.getValue("Extra", String.class), "auto_increment");
    }

    private ShardingRule mockShardingRuleWithoutEncryptRule() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        EncryptRule encryptRule = mock(EncryptRule.class);
        when(encryptRule.findEncryptTable("user")).thenReturn(Optional.<EncryptTable>absent());
        when(shardingRule.getEncryptRule()).thenReturn(encryptRule);
        return shardingRule;
    }

    private ShardingRule mockShardingRuleWithEncryptRule() {
        ShardingRule shardingRule = mock(ShardingRule.class);
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(shardingRule.getEncryptRule()).thenReturn(encryptRule);
        when(encryptRule.findEncryptTable("user")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.getAssistedQueryColumns()).thenReturn(Collections.singletonList("name_assisted"));
        when(encryptTable.getCipherColumns()).thenReturn(Collections.singletonList("name"));
        when(encryptTable.getLogicColumn("name")).thenReturn("logic_name");
        return shardingRule;
    }

}
