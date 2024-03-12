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
import org.apache.shardingsphere.distsql.statement.ral.queryable.export.ExportStorageNodesStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(ProxyContext.class)
class ExportStorageNodesExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    }
    
    @Test
    void assertExecuteWithWrongDatabaseName() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        ExportStorageNodesStatement sqlStatement = new ExportStorageNodesStatement("foo", null);
        assertThrows(IllegalArgumentException.class, () -> new ExportStorageNodesExecutor().getRows(sqlStatement, contextManager));
    }
    
    @Test
    void assertExecuteWithEmptyMetaData() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        ExportStorageNodesStatement sqlStatement = new ExportStorageNodesStatement(null, null);
        Collection<LocalDataQueryResultRow> actual = new ExportStorageNodesExecutor().getRows(sqlStatement, contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is("{\"storage_nodes\":{}}"));
    }
    
    private ContextManager mockEmptyContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(new HashMap<>(),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    void assertExecute() {
        when(database.getName()).thenReturn("normal_db");
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<LocalDataQueryResultRow> actual = new ExportStorageNodesExecutor().getRows(new ExportStorageNodesStatement(null, null), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    @Test
    void assertExecuteWithDatabaseName() {
        when(database.getName()).thenReturn("normal_db");
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<LocalDataQueryResultRow> actual = new ExportStorageNodesExecutor().getRows(new ExportStorageNodesStatement("normal_db", null), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(Collections.singletonMap(database.getName(), database),
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.singleton(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build()))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true")))));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, StorageUnit> createStorageUnits() {
        StorageUnit storageUnit1 = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit1.getDataSource()).thenReturn(createDataSource("ds_0"));
        StorageUnit storageUnit2 = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit2.getDataSource()).thenReturn(createDataSource("ds_2"));
        Map<String, StorageUnit> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", storageUnit1);
        result.put("ds_1", storageUnit2);
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
