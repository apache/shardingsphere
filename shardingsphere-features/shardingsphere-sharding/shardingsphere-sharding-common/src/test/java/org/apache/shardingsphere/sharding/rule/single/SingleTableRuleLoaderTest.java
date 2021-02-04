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

package org.apache.shardingsphere.sharding.rule.single;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SingleTableRuleLoaderTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds0", mockDataSource("ds0", Arrays.asList("employee", "dept", "salary")));
        dataSourceMap.put("ds1", mockDataSource("ds1", Arrays.asList("student", "teacher", "class", "salary")));
    }
    
    private DataSource mockDataSource(final String dataSourceName, final Collection<String> tableNames) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(result.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        ResultSet resultSet = mock(ResultSet.class);
        List<String> tableList = Lists.newArrayList(tableNames);
        if (tableList.isEmpty()) {
            when(resultSet.next()).thenReturn(false);
        } else if (1 == tableList.size()) {
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(TABLE_NAME)).thenReturn(tableList.get(0));
        } else {
            List<String> subTableList = tableList.subList(1, tableNames.size());
            List<Boolean> subNextList = subTableList.stream().map(item -> true).collect(Collectors.toList());
            subNextList.add(false);
            String[] subTableArray = new String[subTableList.size()];
            Boolean[] subNextArray = new Boolean[subNextList.size()];
            subTableList.toArray(subTableArray);
            subNextList.toArray(subNextArray);
            when(resultSet.next()).thenReturn(true, subNextArray);
            when(resultSet.getString(TABLE_NAME)).thenReturn(tableList.get(0), subTableArray);
        }
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{TABLE_TYPE, VIEW_TYPE})).thenReturn(resultSet);
        return result;
    }
    
    @Test
    public void assertLoad() {
        Map<String, SingleTableRule> singleTableRuleMap = SingleTableRuleLoader.load(mock(DatabaseType.class), dataSourceMap, Collections.emptyList());
        Set<String> tableSet = singleTableRuleMap.keySet();
        assertTrue(tableSet.contains("employee"));
        assertTrue(tableSet.contains("dept"));
        assertTrue(tableSet.contains("salary"));
        assertTrue(tableSet.contains("student"));
        assertTrue(tableSet.contains("teacher"));
        assertTrue(tableSet.contains("class"));
    }
    
    @Test
    public void assertLoadWithExcludeTable() {
        Map<String, SingleTableRule> singleTableRuleMap = SingleTableRuleLoader.load(mock(DatabaseType.class), dataSourceMap, Arrays.asList("salary", "employee", "student"));
        Set<String> tableSet = singleTableRuleMap.keySet();
        assertFalse(tableSet.contains("employee"));
        assertFalse(tableSet.contains("salary"));
        assertFalse(tableSet.contains("student"));
        assertTrue(tableSet.contains("dept"));
        assertTrue(tableSet.contains("teacher"));
        assertTrue(tableSet.contains("class"));
    }
}
