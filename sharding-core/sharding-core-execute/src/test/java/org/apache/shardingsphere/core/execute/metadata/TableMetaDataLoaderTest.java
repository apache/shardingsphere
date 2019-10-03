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

package org.apache.shardingsphere.core.execute.metadata;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.execute.ShardingExecuteEngine;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableMetaDataLoaderTest {

    private TableMetaDataLoader tableMetaDataLoader;

    private ShardingRule shardingRule;

    @Before
    public void setUp() {
        shardingRule = createShardingRule();
        ShardingExecuteEngine executeEngine = new ShardingExecuteEngine(1);
        DataSourceMetas dataSourceMetas = buildDataSourceMetas();
        tableMetaDataLoader = new TableMetaDataLoader(dataSourceMetas, executeEngine, getConnectionManager(), 1, false);
    }

    private TableMetaDataConnectionManager getConnectionManager() {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        TableMetaDataConnectionManager connectionManager = mock(TableMetaDataConnectionManager.class);
        try {
            ResultSet tableResultSet = mock(ResultSet.class);
            ResultSet indexResultSet = mock(ResultSet.class);
            ResultSet cloumnResultSet = mock(ResultSet.class);
            ResultSet primaryKeyResultSet = mock(ResultSet.class);
            when(connectionManager.getConnection(ArgumentMatchers.<String>any())).thenReturn(connection);
            when(connectionManager.getConnection(ArgumentMatchers.<String>any()).getMetaData()).thenReturn(databaseMetaData);
            when(connectionManager.getConnection(ArgumentMatchers.<String>any()).getMetaData().getTables(ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                    ArgumentMatchers.<String>any(), ArgumentMatchers.<String[]>any())).thenReturn(tableResultSet);
            when(tableResultSet.next()).thenReturn(true);
            when(connectionManager.getConnection(ArgumentMatchers.<String>any()).getMetaData().getPrimaryKeys(ArgumentMatchers.<String>any(),
                    ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(primaryKeyResultSet);
            when(primaryKeyResultSet.next()).thenReturn(false);
            when(connectionManager.getConnection(ArgumentMatchers.<String>any()).getMetaData().getColumns(ArgumentMatchers.<String>any(),
                    ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(cloumnResultSet);
            when(cloumnResultSet.next()).thenReturn(false);
            when(connectionManager.getConnection(ArgumentMatchers.<String>any()).getMetaData().getPrimaryKeys(ArgumentMatchers.<String>any(),
                    ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any())).thenReturn(indexResultSet);
            when(indexResultSet.next()).thenReturn(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectionManager;
    }

    private DataSourceMetas buildDataSourceMetas() {
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<>();
        shardingDataSourceURLs.put("ds_0", "jdbc:mysql://localhost:3306/ds_0");
        shardingDataSourceURLs.put("ds_1", "jdbc:mysql://localhost:3306/ds_1");
        return new DataSourceMetas(shardingDataSourceURLs, DatabaseTypes.getActualDatabaseType("MySQL"));
    }

    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = createTableRuleConfiguration("TEST_TABLE", "ds_${0..1}.test_table_${0..2}");
        shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
        return new ShardingRule(shardingRuleConfiguration, createDataSourceNames());
    }

    private TableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        return new TableRuleConfiguration(logicTableName, actualDataNodes);
    }

    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1");
    }

    @Ignore
    @Test
    public void AssertLogicIndexes() throws SQLException {
        TableMetaData tableMetaData = tableMetaDataLoader.load("TEST_TABLE", shardingRule);
        assertThat(tableMetaData.getIndexes().size(), is(0));
    }
}
