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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule.ShowSingleTableExecutor;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

@RunWith(MockitoJUnitRunner.class)
public final class ShowSingleTableExecutorTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order", Collections.singletonList(new DataNode("ds_1", "t_order")));
        singleTableDataNodeMap.put("t_order_item", Collections.singletonList(new DataNode("ds_2", "t_order_item")));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(mockSingleTableRule(singleTableDataNodeMap))));
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    public void assertGetRowData() {
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
    public void assertGetRowDataMultipleRules() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order_multiple", Collections.singletonList(new DataNode("ds_1_multiple", "t_order_multiple")));
        singleTableDataNodeMap.put("t_order_item_multiple", Collections.singletonList(new DataNode("ds_2_multiple", "t_order_item_multiple")));
        addShardingSphereRule(mockSingleTableRule(singleTableDataNodeMap));
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
    public void assertGetRowDataWithOtherRules() {
        addShardingSphereRule(new ShadowRule(mock(ShadowRuleConfiguration.class)));
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
    public void assertGetSingleTableWithLikeLiteral() {
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
    public void assertGetColumns() {
        RQLExecutor<ShowSingleTableStatement> executor = new ShowSingleTableExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("table_name"));
        assertThat(iterator.next(), is("storage_unit_name"));
    }
    
    private SingleRule mockSingleTableRule(final Map<String, Collection<DataNode>> singleTableDataNodeMap) {
        SingleRule result = mock(SingleRule.class);
        when(result.getSingleTableDataNodes()).thenReturn(singleTableDataNodeMap);
        return result;
    }
    
    private void addShardingSphereRule(final ShardingSphereRule... rules) {
        database.getRuleMetaData().getRules().addAll(Arrays.asList(rules));
    }
}
