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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableStatement;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule.SingleTableQueryResultSet;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SingleTableQueryResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Before
    public void before() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order", Collections.singletonList(new DataNode("ds_1", "t_order")));
        singleTableDataNodeMap.put("t_order_item", Collections.singletonList(new DataNode("ds_2", "t_order_item")));
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        rules.add(mockSingleTableRule(singleTableDataNodeMap));
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getRules()).thenReturn(rules);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    public void assertGetRowData() {
        DistSQLResultSet resultSet = new SingleTableQueryResultSet();
        resultSet.init(shardingSphereMetaData, mock(ShowSingleTableStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order_item"));
        assertThat(rowData.next(), is("ds_2"));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order"));
        assertThat(rowData.next(), is("ds_1"));
    }
    
    @Test
    public void assertGetRowDataMultipleRules() {
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order_multiple", Collections.singletonList(new DataNode("ds_1_multiple", "t_order_multiple")));
        singleTableDataNodeMap.put("t_order_item_multiple", Collections.singletonList(new DataNode("ds_2_multiple", "t_order_item_multiple")));
        addShardingSphereRule(mockSingleTableRule(singleTableDataNodeMap));
        DistSQLResultSet resultSet = new SingleTableQueryResultSet();
        resultSet.init(shardingSphereMetaData, mock(ShowSingleTableStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order_item"));
        assertThat(rowData.next(), is("ds_2"));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order"));
        assertThat(rowData.next(), is("ds_1"));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order_multiple"));
        assertThat(rowData.next(), is("ds_1_multiple"));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order_item_multiple"));
        assertThat(rowData.next(), is("ds_2_multiple"));
    }
    
    @Test
    public void assertGetRowDataWithOtherRules() {
        addShardingSphereRule(new ShadowRule(mock(ShadowRuleConfiguration.class)));
        DistSQLResultSet resultSet = new SingleTableQueryResultSet();
        resultSet.init(shardingSphereMetaData, mock(ShowSingleTableStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order_item"));
        assertThat(rowData.next(), is("ds_2"));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("t_order"));
        assertThat(rowData.next(), is("ds_1"));
    }
    
    private SingleTableRule mockSingleTableRule(final Map<String, Collection<DataNode>> singleTableDataNodeMap) {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.getSingleTableDataNodes()).thenReturn(singleTableDataNodeMap);
        return result;
    }
    
    private void addShardingSphereRule(final ShardingSphereRule... rules) {
        shardingSphereMetaData.getRuleMetaData().getRules().addAll(Arrays.asList(rules));
    }
}
