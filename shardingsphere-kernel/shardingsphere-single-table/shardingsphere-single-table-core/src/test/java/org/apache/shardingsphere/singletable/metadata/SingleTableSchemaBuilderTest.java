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

package org.apache.shardingsphere.singletable.metadata;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SingleTableSchemaBuilderTest {
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private final String[] singleTableNames = {"single_table1", "single_table2"};
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    @Test
    public void assertBuildOfSingleTables() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:h2:mem:db");
        DataSource dataSource = new MockedDataSource(connection);
        Collection<ShardingSphereRule> rules = Collections.singleton(mockSingleTableRuleLoad(dataSource, connection));
        mockSQLLoad(connection);
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(
                databaseType, databaseType, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, dataSource), rules, new ConfigurationProperties(new Properties()), DefaultDatabase.LOGIC_NAME);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(Arrays.asList(singleTableNames), materials);
        assertThat(actual.size(), is(1));
        assertThat(actual.values().iterator().next().getTables().size(), is(2));
        assertActualOfSingleTables(actual.values().iterator().next().getTables().values());
    }
    
    private void mockSQLLoad(final Connection connection) throws SQLException {
        when(databaseType.formatTableNamePattern("single_table1")).thenReturn("single_table1");
        when(databaseType.getQuoteCharacter().wrap("single_table1")).thenReturn("single_table1");
        when(databaseType.formatTableNamePattern("single_table2")).thenReturn("single_table2");
        when(databaseType.getQuoteCharacter().wrap("single_table2")).thenReturn("single_table2");
        ResultSet resultSet1 = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq("single_table1"), eq(null))).thenReturn(resultSet1);
        when(resultSet1.next()).thenReturn(true);
        ResultSet resultSet2 = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq("single_table2"), eq(null))).thenReturn(resultSet2);
        when(resultSet2.next()).thenReturn(true);
        ResultSet column = mock(ResultSet.class);
        when(column.next()).thenReturn(true, true, true, false);
        when(column.getString("TABLE_NAME")).thenReturn("single_table1");
        when(column.getString("COLUMN_NAME")).thenReturn("id", "name", "doc");
        when(column.getInt("DATA_TYPE")).thenReturn(4, 12, -1);
        when(connection.getMetaData().getColumns(any(), any(), eq("single_table1"), eq("%"))).thenReturn(column);
        ResultSet column2 = mock(ResultSet.class);
        when(column2.next()).thenReturn(true, true, true, false);
        when(column2.getString("TABLE_NAME")).thenReturn("single_table2");
        when(column2.getString("COLUMN_NAME")).thenReturn("id", "name", "doc");
        when(column2.getInt("DATA_TYPE")).thenReturn(4, 12, -1);
        when(connection.getMetaData().getColumns(any(), any(), eq("single_table2"), eq("%"))).thenReturn(column2);
    }
    
    private SingleTableRule mockSingleTableRuleLoad(final DataSource dataSource, final Connection connection) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq(null), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, true, true, false);
        when(resultSet.getString(TABLE_NAME)).thenReturn(singleTableNames[0], singleTableNames[1]);
        return new SingleTableRule(new SingleTableRuleConfiguration(),
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap("logic_db", dataSource), Collections.emptyList(), new ConfigurationProperties(new Properties()));
    }
    
    private void assertActualOfSingleTables(final Collection<ShardingSphereTable> actual) {
        Map<String, ShardingSphereTable> tables = actual.stream().collect(Collectors.toMap(ShardingSphereTable::getName, value -> value));
        assertTrue(tables.containsKey(singleTableNames[0]));
        assertFalse(tables.get(singleTableNames[0]).getColumns().isEmpty());
        assertTrue(tables.containsKey(singleTableNames[1]));
        assertFalse(tables.get(singleTableNames[1]).getColumns().isEmpty());
    }
}
