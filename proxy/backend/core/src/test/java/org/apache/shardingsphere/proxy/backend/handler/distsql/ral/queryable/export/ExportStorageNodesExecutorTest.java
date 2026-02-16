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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.export;

import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportStorageNodesStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExportStorageNodesExecutorTest {
    
    private final ExportStorageNodesExecutor executor = (ExportStorageNodesExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ExportStorageNodesStatement.class);
    
    @TempDir
    private Path tempDir;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase emptyDatabase;
    
    @BeforeEach
    void setUp() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(emptyDatabase.getProtocolType()).thenReturn(databaseType);
    }
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(new ExportStorageNodesStatement(null, null)), is(Arrays.asList("id", "create_time", "storage_nodes")));
    }
    
    @Test
    void assertExecuteWithWrongDatabaseName() {
        assertThrows(IllegalArgumentException.class, () -> executor.getRows(new ExportStorageNodesStatement("foo", null), mockEmptyContextManager()));
    }
    
    @Test
    void assertExecuteWithEmptyMetaData() {
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ExportStorageNodesStatement(null, null), mockEmptyContextManager());
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is("{\"storage_nodes\":{}}"));
    }
    
    private ContextManager mockEmptyContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        when(result.getMetaDataContexts()).thenReturn(new MetaDataContexts(metaData, new ShardingSphereStatistics()));
        when(result.getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        return result;
    }
    
    @Test
    void assertExecute() {
        prepareNormalDatabase();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ExportStorageNodesStatement(null, null), mockContextManager(database));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    @Test
    void assertExecuteWithDatabaseName() {
        prepareNormalDatabase();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ExportStorageNodesStatement("normal_db", null), mockContextManager(database));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    @Test
    void assertExecuteWithFilePath() throws IOException {
        prepareNormalDatabase();
        ContextManager contextManager = mockContextManager(database);
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId()).thenReturn("file_id");
        Path tempFile = tempDir.resolve("export-storage-nodes.json");
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ExportStorageNodesStatement(null, tempFile.toString()), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("file_id"));
        assertThat(row.getCell(3), is(String.format("Successfully exported to：'%s'", tempFile)));
        assertThat(new String(Files.readAllBytes(tempFile)), is(loadExpectedRow()));
    }
    
    @Test
    void assertExecuteWithEmptyStorageNodeDatabase() {
        prepareNormalDatabase();
        when(emptyDatabase.getName()).thenReturn("empty_db");
        when(emptyDatabase.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.emptySet());
        when(emptyDatabase.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ExportStorageNodesStatement(null, null), mockContextManager(emptyDatabase, database));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    private void prepareNormalDatabase() {
        when(database.getName()).thenReturn("normal_db");
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(storageUnits.keySet());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase... databases) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Arrays.asList(databases),
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.singleton(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build()))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true"))));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
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
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String loadExpectedRow() {
        URL url = Objects.requireNonNull(ExportStorageNodesExecutorTest.class.getResource("/expected/export-storage-nodes.json"));
        return Files.readAllLines(Paths.get(url.toURI())).stream().filter(each -> !each.startsWith("#") && !each.trim().isEmpty()).collect(Collectors.joining(System.lineSeparator()));
    }
}
