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
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SingleTableDataNodeLoaderTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1);
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
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        Map<String, SingleTableDataNode> dataNodeMap = SingleTableDataNodeLoader.load(mock(DatabaseType.class), dataSourceMap, Collections.emptyList(), props);
        assertTrue(dataNodeMap.containsKey("employee"));
        assertTrue(dataNodeMap.containsKey("dept"));
        assertTrue(dataNodeMap.containsKey("salary"));
        assertTrue(dataNodeMap.containsKey("student"));
        assertTrue(dataNodeMap.containsKey("teacher"));
        assertTrue(dataNodeMap.containsKey("class"));
        assertThat(dataNodeMap.get("employee").getDataSourceName(), is("ds0"));
        assertThat(dataNodeMap.get("dept").getDataSourceName(), is("ds0"));
        assertThat(dataNodeMap.get("salary").getDataSourceName(), is("ds0"));
        assertThat(dataNodeMap.get("student").getDataSourceName(), is("ds1"));
        assertThat(dataNodeMap.get("teacher").getDataSourceName(), is("ds1"));
        assertThat(dataNodeMap.get("class").getDataSourceName(), is("ds1"));
    }
    
    @Test
    public void assertLoadWithExcludeTables() {
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        Collection<String> excludedTables = Arrays.asList("salary", "employee", "student");
        Map<String, SingleTableDataNode> dataNodeMap = SingleTableDataNodeLoader.load(mock(DatabaseType.class), dataSourceMap, excludedTables, props);
        assertFalse(dataNodeMap.containsKey("employee"));
        assertFalse(dataNodeMap.containsKey("salary"));
        assertFalse(dataNodeMap.containsKey("student"));
        assertTrue(dataNodeMap.containsKey("dept"));
        assertTrue(dataNodeMap.containsKey("teacher"));
        assertTrue(dataNodeMap.containsKey("class"));
        assertThat(dataNodeMap.get("dept").getDataSourceName(), is("ds0"));
        assertThat(dataNodeMap.get("teacher").getDataSourceName(), is("ds1"));
        assertThat(dataNodeMap.get("class").getDataSourceName(), is("ds1"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertLoadWithCheckOption() {
        Properties properties = new Properties();
        properties.setProperty(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED.getKey(), "true");
        ConfigurationProperties props = new ConfigurationProperties(properties);
        SingleTableDataNodeLoader.load(mock(DatabaseType.class), dataSourceMap, Collections.emptyList(), props);
    }
    
    @Test
    public void assertLoadWithExcludeTablesCheckOption() {
        Properties properties = new Properties();
        properties.setProperty(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED.getKey(), "true");
        Collection<String> excludedTables = Arrays.asList("salary", "employee", "student");
        ConfigurationProperties props = new ConfigurationProperties(properties);
        Map<String, SingleTableDataNode> dataNodeMap = SingleTableDataNodeLoader.load(mock(DatabaseType.class), dataSourceMap, excludedTables, props);
        assertFalse(dataNodeMap.containsKey("employee"));
        assertFalse(dataNodeMap.containsKey("salary"));
        assertFalse(dataNodeMap.containsKey("student"));
        assertTrue(dataNodeMap.containsKey("dept"));
        assertTrue(dataNodeMap.containsKey("teacher"));
        assertTrue(dataNodeMap.containsKey("class"));
        assertThat(dataNodeMap.get("dept").getDataSourceName(), is("ds0"));
        assertThat(dataNodeMap.get("teacher").getDataSourceName(), is("ds1"));
        assertThat(dataNodeMap.get("class").getDataSourceName(), is("ds1"));
    }
}
