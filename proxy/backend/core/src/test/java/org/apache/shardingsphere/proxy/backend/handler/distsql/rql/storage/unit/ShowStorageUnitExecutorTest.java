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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.storage.unit;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowStorageUnitExecutorTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void before() {
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
    void assertAllStorageUnit() {
        ShowStorageUnitExecutor executor = new ShowStorageUnitExecutor();
        ShowStorageUnitsStatement showStorageUnitsStatement = new ShowStorageUnitsStatement(mock(DatabaseSegment.class), null);
        Map<Integer, String> nameMap = new HashMap<>(3, 1F);
        nameMap.put(0, "ds_2");
        nameMap.put(1, "ds_1");
        nameMap.put(2, "ds_0");
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, showStorageUnitsStatement);
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        assertThat(actual.size(), is(3));
        int index = 0;
        while (rowData.hasNext()) {
            LocalDataQueryResultRow data = rowData.next();
            assertThat(data.getCell(1), is(nameMap.get(index)));
            assertThat(data.getCell(2), is("MySQL"));
            assertThat(data.getCell(3), is("localhost"));
            assertThat(data.getCell(4), is(3307));
            assertThat(data.getCell(5), is(nameMap.get(index)));
            assertThat(data.getCell(6), is(""));
            assertThat(data.getCell(7), is(""));
            assertThat(data.getCell(8), is(""));
            assertThat(data.getCell(9), is("100"));
            assertThat(data.getCell(10), is("10"));
            assertThat(data.getCell(11), is(""));
            assertThat(data.getCell(12), is("{\"openedConnections\":[]}"));
            index++;
        }
    }
    
    @Test
    void assertUnusedStorageUnit() {
        RQLExecutor<ShowStorageUnitsStatement> executor = new ShowStorageUnitExecutor();
        ShowStorageUnitsStatement showStorageUnitsStatement = new ShowStorageUnitsStatement(mock(DatabaseSegment.class), 0);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, showStorageUnitsStatement);
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        LocalDataQueryResultRow data = rowData.next();
        assertThat(data.getCell(1), is("ds_2"));
        assertThat(data.getCell(2), is("MySQL"));
        assertThat(data.getCell(3), is("localhost"));
        assertThat(data.getCell(4), is(3307));
        assertThat(data.getCell(5), is("ds_2"));
        assertThat(data.getCell(6), is(""));
        assertThat(data.getCell(7), is(""));
        assertThat(data.getCell(8), is(""));
        assertThat(data.getCell(9), is("100"));
        assertThat(data.getCell(10), is("10"));
        assertThat(data.getCell(11), is(""));
        assertThat(data.getCell(12), is("{\"openedConnections\":[]}"));
    }
    
    @Test
    void assertGetColumns() {
        RQLExecutor<ShowStorageUnitsStatement> executor = new ShowStorageUnitExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(12));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("name"));
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("host"));
        assertThat(iterator.next(), is("port"));
        assertThat(iterator.next(), is("db"));
        assertThat(iterator.next(), is("connection_timeout_milliseconds"));
        assertThat(iterator.next(), is("idle_timeout_milliseconds"));
        assertThat(iterator.next(), is("max_lifetime_milliseconds"));
        assertThat(iterator.next(), is("max_pool_size"));
        assertThat(iterator.next(), is("min_pool_size"));
        assertThat(iterator.next(), is("read_only"));
        assertThat(iterator.next(), is("other_attributes"));
    }
}
