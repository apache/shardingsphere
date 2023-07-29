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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportStorageNodesStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ExportStorageNodesExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    }
    
    @Test
    void assertGetColumns() {
        Collection<String> columns = new ExportStorageNodesExecutor().getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> columnIterator = columns.iterator();
        assertThat(columnIterator.next(), is("id"));
        assertThat(columnIterator.next(), is("create_time"));
        assertThat(columnIterator.next(), is("storage_nodes"));
    }
    
    @Test
    void assertExecuteWithWrongDatabaseName() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        ExportStorageNodesStatement sqlStatement = new ExportStorageNodesStatement("foo", null);
        assertThrows(IllegalArgumentException.class, () -> new ExportStorageNodesExecutor().getRows(contextManager.getMetaDataContexts().getMetaData(), sqlStatement));
    }
    
    @Test
    void assertExecuteWithEmptyMetaData() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        ExportStorageNodesStatement sqlStatement = new ExportStorageNodesStatement(null, null);
        Collection<LocalDataQueryResultRow> actual = new ExportStorageNodesExecutor().getRows(contextManager.getMetaDataContexts().getMetaData(), sqlStatement);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is("{\"storage_nodes\":{}}"));
    }
    
    private ContextManager mockEmptyContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(new HashMap<>(),
                new ShardingSphereResourceMetaData(Collections.emptyMap()), new ShardingSphereRuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    void assertExecute() {
        when(database.getName()).thenReturn("normal_db");
        when(database.getResourceMetaData().getDataSources()).thenReturn(createDataSourceMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<LocalDataQueryResultRow> actual = new ExportStorageNodesExecutor().getRows(contextManager.getMetaDataContexts().getMetaData(), new ExportStorageNodesStatement(null, null));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    @Test
    void assertExecuteWithDatabaseName() {
        when(database.getName()).thenReturn("normal_db");
        when(database.getResourceMetaData().getDataSources()).thenReturn(createDataSourceMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<LocalDataQueryResultRow> actual = new ExportStorageNodesExecutor().getRows(contextManager.getMetaDataContexts().getMetaData(), new ExportStorageNodesStatement("normal_db", null));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(Collections.singletonMap(database.getName(), database),
                new ShardingSphereResourceMetaData(Collections.emptyMap()),
                new ShardingSphereRuleMetaData(Collections.singleton(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build(), Collections.emptyMap()))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true")))));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", createDataSource("demo_ds_0"));
        result.put("ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl(String.format("jdbc:mock://127.0.0.1/%s", name));
        result.setUsername("root");
        result.setPassword("test");
        result.setMaxPoolSize(50);
        result.setMinPoolSize(1);
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "ds_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        result.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${order_id % 2}"))));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private String loadExpectedRow() {
        StringBuilder result = new StringBuilder();
        String fileName = Objects.requireNonNull(ExportStorageNodesExecutorTest.class.getResource("/expected/export-storage-nodes.json")).getFile();
        try (
                FileReader fileReader = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                result.append(line);
            }
        }
        return result.toString();
    }
}
