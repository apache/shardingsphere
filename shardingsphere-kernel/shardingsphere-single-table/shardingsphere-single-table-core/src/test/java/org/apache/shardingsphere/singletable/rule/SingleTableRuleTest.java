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

package org.apache.shardingsphere.singletable.rule;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SingleTableRuleTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("ds_0", mockDataSource("ds_0", Arrays.asList("employee", "t_order_0", "t_order_1")));
        dataSourceMap.put("ds_1", mockDataSource("ds_1", Arrays.asList("student", "t_order_0", "t_order_1")));
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(result.getConnection().getCatalog()).thenReturn(dataSourceName);
        ResultSet resultSet = mockResultSet(tableNames);
        when(result.getConnection().getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, VIEW_TYPE})).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final List<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        Collection<Boolean> nextResults = tableNames.stream().map(each -> true).collect(Collectors.toList());
        nextResults.add(false);
        when(result.next()).thenReturn(true, nextResults.toArray(new Boolean[tableNames.size()]));
        String firstTableName = tableNames.get(0);
        String[] nextTableNames = tableNames.subList(1, tableNames.size()).toArray(new String[tableNames.size() - 1]);
        when(result.getString(TABLE_NAME)).thenReturn(firstTableName, nextTableNames);
        return result;
    }
    
    @Test
    public void assertGetRuleType() {
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), mock(DatabaseType.class),
                Collections.emptyMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        assertThat(singleTableRule.getType(), is(SingleTableRule.class.getSimpleName()));
    }
    
    @Test
    public void assertGetSingleTableDataNodes() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getAllTables()).thenReturn(Arrays.asList("t_order", "t_order_0", "t_order_1"));
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), mock(DatabaseType.class), dataSourceMap, 
                Collections.singletonList(dataNodeContainedRule), new ConfigurationProperties(new Properties()));
        Map<String, Collection<DataNode>> actual = singleTableRule.getSingleTableDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    public void assertGetSingleTableDataNodesWithUpperCase() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getAllTables()).thenReturn(Arrays.asList("T_ORDER", "T_ORDER_0", "T_ORDER_1"));
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), mock(DatabaseType.class), dataSourceMap,
                Collections.singletonList(dataNodeContainedRule), new ConfigurationProperties(new Properties()));
        Map<String, Collection<DataNode>> actual = singleTableRule.getSingleTableDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
}
