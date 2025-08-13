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
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExportDatabaseConfigurationExecutorTest {
    
    @Test
    void assertExecute() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("normal_db");
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createShardingRuleConfiguration()));
        ExportDatabaseConfigurationExecutor executor = new ExportDatabaseConfigurationExecutor();
        executor.setDatabase(database);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ExportDatabaseConfigurationStatement(null, mock(FromDatabaseSegment.class)), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is(loadExpectedRow()));
    }
    
    private Map<String, StorageUnit> createStorageUnits() {
        Map<String, DataSourcePoolProperties> propsMap = createDataSourceMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, StorageUnit> result = new LinkedHashMap<>(propsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            when(storageUnit.getDataSourcePoolProperties()).thenReturn(entry.getValue());
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    @Test
    void assertExecuteWithEmptyDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("empty_db");
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        ExportDatabaseConfigurationStatement sqlStatement = new ExportDatabaseConfigurationStatement(null, new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("empty_db"))));
        ExportDatabaseConfigurationExecutor executor = new ExportDatabaseConfigurationExecutor();
        executor.setDatabase(database);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(sqlStatement, mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("databaseName: empty_db" + System.lineSeparator()));
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
        result.setPassword("");
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
        URL url = Objects.requireNonNull(ExportDatabaseConfigurationExecutorTest.class.getResource("/expected/export-database-configuration.yaml"));
        return Files.readAllLines(Paths.get(url.toURI())).stream().filter(each -> !each.startsWith("#") && !each.trim().isEmpty()).collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator();
    }
}
