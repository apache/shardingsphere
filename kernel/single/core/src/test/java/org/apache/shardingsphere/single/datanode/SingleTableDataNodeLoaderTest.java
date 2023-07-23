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

package org.apache.shardingsphere.single.datanode;

import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleTableDataNodeLoaderTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String SYSTEM_TABLE_TYPE = "SYSTEM TABLE";
    
    private static final String SYSTEM_VIEW_TYPE = "SYSTEM VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    private Collection<String> configuredSingleTables;
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("ds0", mockDataSource("ds0", Arrays.asList("employee", "dept", "salary")));
        dataSourceMap.put("ds1", mockDataSource("ds1", Arrays.asList("student", "teacher", "class", "salary")));
        configuredSingleTables = Collections.singletonList("*.*");
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        ResultSet resultSet = mockResultSet(tableNames);
        when(connection.getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, VIEW_TYPE, SYSTEM_TABLE_TYPE, SYSTEM_VIEW_TYPE})).thenReturn(resultSet);
        return new MockedDataSource(connection);
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
    void assertLoad() {
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                dataSourceMap, getBuiltRulesWithExcludedTables(), configuredSingleTables);
        assertFalse(actual.containsKey("employee"));
        assertFalse(actual.containsKey("salary"));
        assertFalse(actual.containsKey("student"));
        assertTrue(actual.containsKey("dept"));
        assertTrue(actual.containsKey("teacher"));
        assertTrue(actual.containsKey("class"));
        assertThat(actual.get("dept").iterator().next().getDataSourceName(), is("ds0"));
        assertThat(actual.get("teacher").iterator().next().getDataSourceName(), is("ds1"));
        assertThat(actual.get("class").iterator().next().getDataSourceName(), is("ds1"));
    }
    
    private Collection<ShardingSphereRule> getBuiltRulesWithExcludedTables() {
        Collection<String> excludedTables = Arrays.asList("salary", "employee", "student");
        TableContainedRule tableContainedRule = mock(TableContainedRule.class, RETURNS_DEEP_STUBS);
        when(tableContainedRule.getDistributedTableMapper().getTableNames()).thenReturn(excludedTables);
        return Collections.singletonList(tableContainedRule);
    }
    
    @Test
    void assertLoadWithConflictTables() {
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class),
                dataSourceMap, Collections.emptyList(), configuredSingleTables);
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("salary"));
        assertTrue(actual.containsKey("student"));
        assertTrue(actual.containsKey("dept"));
        assertTrue(actual.containsKey("teacher"));
        assertTrue(actual.containsKey("class"));
        assertThat(actual.get("employee").iterator().next().getDataSourceName(), is("ds0"));
        assertThat(actual.get("salary").iterator().next().getDataSourceName(), is("ds0"));
        assertThat(actual.get("student").iterator().next().getDataSourceName(), is("ds1"));
        assertThat(actual.get("dept").iterator().next().getDataSourceName(), is("ds0"));
        assertThat(actual.get("teacher").iterator().next().getDataSourceName(), is("ds1"));
        assertThat(actual.get("class").iterator().next().getDataSourceName(), is("ds1"));
    }
}
