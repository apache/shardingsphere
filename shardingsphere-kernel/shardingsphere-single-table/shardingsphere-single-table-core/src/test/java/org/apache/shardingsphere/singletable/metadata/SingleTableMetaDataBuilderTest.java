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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SingleTableMetaDataBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleBasedTableMetaDataBuilder.class);
    }
    
    private SingleTableRule singleTableRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    @Before
    public void setUp() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        mockSingleTableLoad(connection);
        singleTableRule = new SingleTableRule(databaseType, Collections.singletonMap("ds", dataSource), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(databaseType.formatTableNamePattern("tbl")).thenReturn("tbl");
        mockTableIsExist(connection);
        mockTables(connection);
        when(databaseType.getQuoteCharacter().wrap("tbl")).thenReturn("tbl");
    }
    
    private void mockTables(final Connection connection) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString("TABLE_NAME")).thenReturn("tbl");
        when(resultSet.getString("COLUMN_NAME")).thenReturn("id", "name", "doc");
        when(resultSet.getInt("DATA_TYPE")).thenReturn(4, 12, -1);
        when(resultSet.getString("TYPE_NAME")).thenReturn("int", "varchar", "json");
        when(connection.getMetaData().getColumns(any(), any(), any(), eq("%"))).thenReturn(resultSet);
        ResultSet indexResultSet = mock(ResultSet.class, Answers.RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getIndexInfo(any(), any(), eq("tbl"), eq(false), eq(false))).thenReturn(indexResultSet);
        when(indexResultSet.getString("INDEX_NAME")).thenReturn("id", "idx_name_tbl");
        when(indexResultSet.next()).thenReturn(true, true, false);
        ResultSet primaryResultSet = mock(ResultSet.class);
        when(connection.getMetaData().getPrimaryKeys(any(), any(), eq("tbl"))).thenReturn(primaryResultSet);
        when(primaryResultSet.next()).thenReturn(true, false);
        when(primaryResultSet.getString("COLUMN_NAME")).thenReturn("id");
    }
    
    private void mockTableIsExist(final Connection connection) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq("tbl"), eq(null))).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
    }
    
    private void mockSingleTableLoad(final Connection connection) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq(null), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("TABLE_NAME")).thenReturn("tbl");
    }
    
    @Test
    public void testLoad() throws SQLException {
        Collection<ShardingSphereRule> rules = Collections.singletonList(singleTableRule);
        final SingleTableMetaDataBuilder builder = (SingleTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(singleTableRule);
        Map<String, TableMetaData> actual = builder.load(Collections.singleton("tbl"), singleTableRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource),
                rules, props));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("tbl").getColumns().size(), is(3));
        assertThat(actual.get("tbl").getColumnMetaData(0), is(new ColumnMetaData("id", 4, true, false, false)));
        assertThat(actual.get("tbl").getColumnMetaData(1), is(new ColumnMetaData("name", 12, false, false, false)));
        assertThat(actual.get("tbl").getColumnMetaData(2), is(new ColumnMetaData("doc", -1, false, false, false)));
    }
    
    @Test
    public void testDecorate() throws SQLException {
        Collection<ShardingSphereRule> rules = Collections.singletonList(singleTableRule);
        final SingleTableMetaDataBuilder builder = (SingleTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(singleTableRule);
        Map<String, TableMetaData> actual = builder.load(Collections.singleton("tbl"), singleTableRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource),
                rules, props));
        assertFalse(actual.isEmpty());
        assertThat(actual.get("tbl").getColumns().size(), is(3));
        assertThat(actual.get("tbl").getColumnMetaData(0), is(new ColumnMetaData("id", 4, true, false, false)));
        assertThat(actual.get("tbl").getColumnMetaData(1), is(new ColumnMetaData("name", 12, false, false, false)));
        assertThat(actual.get("tbl").getColumnMetaData(2), is(new ColumnMetaData("doc", -1, false, false, false)));
        TableMetaData tableMetaData = builder.decorate("tbl", actual.get("tbl"), singleTableRule);
        assertThat(tableMetaData.getColumnMetaData(0), is(new ColumnMetaData("id", 4, true, false, false)));
        assertThat(tableMetaData.getColumnMetaData(1), is(new ColumnMetaData("name", 12, false, false, false)));
        assertThat(tableMetaData.getColumnMetaData(2), is(new ColumnMetaData("doc", -1, false, false, false)));
        assertThat(tableMetaData.getIndexes().size(), is(2));
        assertThat(tableMetaData.getIndexes().get("id"), is(new IndexMetaData("id")));
        assertThat(tableMetaData.getIndexes().get("idx_name"), is(new IndexMetaData("idx_name")));
    }
}
