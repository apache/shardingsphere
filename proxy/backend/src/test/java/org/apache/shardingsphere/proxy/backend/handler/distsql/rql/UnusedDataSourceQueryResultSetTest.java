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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowUnusedResourcesStatement;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.resource.UnusedDataSourceQueryResultSet;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UnusedDataSourceQueryResultSetTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", createDataSources());
        ShardingSphereRuleMetaData metaData = new ShardingSphereRuleMetaData(Collections.singleton(createShardingRule()));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(metaData);
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = createTableRuleConfiguration("order", "ds_${0..1}.order_${0..1}");
        shardingTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "increment"));
        shardingRuleConfig.getTables().add(shardingTableRuleConfig);
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration(final String logicTableName, final String actualDataNodes) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(logicTableName, actualDataNodes);
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "database_inline"));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "table_inline"));
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1", "ds_2");
    }
    
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> result = new HashMap<>();
        for (String each : createDataSourceNames()) {
            result.put(each, createDataSource(each));
        }
        return result;
    }
    
    private MockedDataSource createDataSource(final String dataSourceName) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl("jdbc:mysql://localhost:3307/" + dataSourceName);
        result.setUsername("root");
        result.setPassword("root");
        result.setMaxPoolSize(100);
        result.setMinPoolSize(10);
        return result;
    }
    
    @Test
    public void assertGetRowData() {
        DatabaseDistSQLResultSet resultSet = new UnusedDataSourceQueryResultSet();
        resultSet.init(database, mock(ShowUnusedResourcesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(12));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("ds_2"));
        assertThat(rowData.next(), is("MySQL"));
        assertThat(rowData.next(), is("localhost"));
        assertThat(rowData.next(), is(3307));
        assertThat(rowData.next(), is("ds_2"));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is("100"));
        assertThat(rowData.next(), is("10"));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is(""));
    }
}
