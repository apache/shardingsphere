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

package org.apache.shardingsphere.single.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowSingleTableExecutorTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void before() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order", Collections.singletonList(new DataNode("ds_1", "t_order")));
        singleTableDataNodeMap.put("t_order_item", Collections.singletonList(new DataNode("ds_2", "t_order_item")));
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(Collections.singleton(mockSingleRule(singleTableDataNodeMap))));
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowSingleTableStatement> executor = new ShowSingleTableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowSingleTableStatement.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        LocalDataQueryResultRow firstRow = rowData.next();
        assertThat(firstRow.getCell(1), is("t_order"));
        assertThat(firstRow.getCell(2), is("ds_1"));
        LocalDataQueryResultRow secondRow = rowData.next();
        assertThat(secondRow.getCell(1), is("t_order_item"));
        assertThat(secondRow.getCell(2), is("ds_2"));
    }
    
    @Test
    void assertGetRowDataMultipleRules() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order_multiple", Collections.singletonList(new DataNode("ds_1_multiple", "t_order_multiple")));
        singleTableDataNodeMap.put("t_order_item_multiple", Collections.singletonList(new DataNode("ds_2_multiple", "t_order_item_multiple")));
        addShardingSphereRule(mockSingleRule(singleTableDataNodeMap));
        RQLExecutor<ShowSingleTableStatement> executor = new ShowSingleTableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowSingleTableStatement.class));
        assertThat(actual.size(), is(4));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        LocalDataQueryResultRow firstRow = rowData.next();
        assertThat(firstRow.getCell(1), is("t_order"));
        assertThat(firstRow.getCell(2), is("ds_1"));
        LocalDataQueryResultRow secondRow = rowData.next();
        assertThat(secondRow.getCell(1), is("t_order_item"));
        assertThat(secondRow.getCell(2), is("ds_2"));
        LocalDataQueryResultRow thirdRow = rowData.next();
        assertThat(thirdRow.getCell(1), is("t_order_item_multiple"));
        assertThat(thirdRow.getCell(2), is("ds_2_multiple"));
        LocalDataQueryResultRow fourthRow = rowData.next();
        assertThat(fourthRow.getCell(1), is("t_order_multiple"));
        assertThat(fourthRow.getCell(2), is("ds_1_multiple"));
    }
    
    @Test
    void assertGetSingleTableWithLikeLiteral() {
        RQLExecutor<ShowSingleTableStatement> executor = new ShowSingleTableExecutor();
        ShowSingleTableStatement statement = new ShowSingleTableStatement(null, "%item", null);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, statement);
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        LocalDataQueryResultRow firstRow = rowData.next();
        assertThat(firstRow.getCell(1), is("t_order_item"));
        assertThat(firstRow.getCell(2), is("ds_2"));
    }
    
    @Test
    void assertGetColumns() {
        RQLExecutor<ShowSingleTableStatement> executor = new ShowSingleTableExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("table_name"));
        assertThat(iterator.next(), is("storage_unit_name"));
    }
    
    private SingleRule mockSingleRule(final Map<String, Collection<DataNode>> singleTableDataNodeMap) {
        SingleRule result = mock(SingleRule.class);
        when(result.getSingleTableDataNodes()).thenReturn(singleTableDataNodeMap);
        return result;
    }
    
    private void addShardingSphereRule(final ShardingSphereRule... rules) {
        database.getRuleMetaData().getRules().addAll(Arrays.asList(rules));
    }
}
