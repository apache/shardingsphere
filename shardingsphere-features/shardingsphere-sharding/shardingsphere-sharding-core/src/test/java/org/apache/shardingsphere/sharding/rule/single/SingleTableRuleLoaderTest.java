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

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
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
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(result.getConnection().getCatalog()).thenReturn(dataSourceName);
        ResultSet resultSet = mockResultSet(tableNames);
        when(result.getConnection().getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, VIEW_TYPE})).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final List<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        String firstTableName = tableNames.get(0);
        Collection<String> remainTableNames = tableNames.subList(1, tableNames.size());
        Collection<Boolean> remainNextResults = remainTableNames.stream().map(each -> true).collect(Collectors.toList());
        remainNextResults.add(false);
        when(result.next()).thenReturn(true, remainNextResults.toArray(new Boolean[tableNames.size()]));
        when(result.getString(TABLE_NAME)).thenReturn(firstTableName, remainTableNames.toArray(new String[tableNames.size() - 1]));
        return result;
    }
    
    @Test
    public void assertLoad() {
        Collection<String> tableNames = SingleTableRuleLoader.load(mock(DatabaseType.class), dataSourceMap, Collections.emptyList()).keySet();
        assertTrue(tableNames.contains("employee"));
        assertTrue(tableNames.contains("dept"));
        assertTrue(tableNames.contains("salary"));
        assertTrue(tableNames.contains("student"));
        assertTrue(tableNames.contains("teacher"));
        assertTrue(tableNames.contains("class"));
    }
    
    @Test
    public void assertLoadWithExcludeTables() {
        Collection<String> tableNames = SingleTableRuleLoader.load(mock(DatabaseType.class), dataSourceMap, Arrays.asList("salary", "employee", "student")).keySet();
        assertFalse(tableNames.contains("employee"));
        assertFalse(tableNames.contains("salary"));
        assertFalse(tableNames.contains("student"));
        assertTrue(tableNames.contains("dept"));
        assertTrue(tableNames.contains("teacher"));
        assertTrue(tableNames.contains("class"));
    }
}
