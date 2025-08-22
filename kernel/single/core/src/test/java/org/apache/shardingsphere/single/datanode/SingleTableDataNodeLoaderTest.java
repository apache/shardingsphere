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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
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
    
    private static final String PARTITIONED_TABLE_TYPE = "PARTITIONED TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String SYSTEM_TABLE_TYPE = "SYSTEM TABLE";
    
    private static final String SYSTEM_VIEW_TYPE = "SYSTEM VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private Map<String, DataSource> dataSourceMap;
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("ds0", mockDataSource("ds0", Arrays.asList("employee", "dept", "salary")));
        dataSourceMap.put("ds1", mockDataSource("ds1", Arrays.asList("student", "teacher", "class", "salary")));
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        ResultSet resultSet = mockResultSet(tableNames);
        when(connection.getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, PARTITIONED_TABLE_TYPE, VIEW_TYPE, SYSTEM_TABLE_TYPE, SYSTEM_VIEW_TYPE})).thenReturn(resultSet);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
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
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(Arrays.asList("salary", "employee", "student"));
        when(builtRule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load(
                "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule), Collections.singleton("*.*"));
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
    
    @Test
    void assertLoadWithConflictTables() {
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load("foo_db", databaseType, dataSourceMap, Collections.emptyList(), Collections.singleton("*.*.*"));
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
    
    @Test
    void assertLoadWithEmptyConfiguredTables() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(Arrays.asList("salary", "employee", "student"));
        when(builtRule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load("foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule), Collections.emptyList());
        assertTrue(actual.isEmpty());
    }
}
