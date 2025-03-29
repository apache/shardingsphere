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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.imports;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.distsql.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportMetaDataExecutorTest {
    
    private static final String METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7Im5vcm1hbF9kYiI6ImRhdGFiYXNlTmFtZTogbm9ybWFsX2RiXG5kYXRhU291cmNlczpcbiAgZHNfMDpcbiA"
            + "gICBwYXNzd29yZDogXG4gICAgdXJsOiBqZGJjOmgyOm1lbTpkZW1vX2RzXzA7REJfQ0xPU0VfREVMQVk9LTE7REFUQUJBU0VfVE9fVVBQRVI9ZmFsc2U7TU9ERT1NeVNRTFxuICAgIHVzZXJuYW1lOiByb290XG4gICAgbWluUG9"
            + "vbFNpemU6IDFcbiAgICBtYXhQb29sU2l6ZTogNTBcbiAgZHNfMTpcbiAgICBwYXNzd29yZDogXG4gICAgdXJsOiBqZGJjOmgyOm1lbTpkZW1vX2RzXzE7REJfQ0xPU0VfREVMQVk9LTE7REFUQUJBU0VfVE9fVVBQRVI9ZmFsc2"
            + "U7TU9ERT1NeVNRTFxuICAgIHVzZXJuYW1lOiByb290XG4gICAgbWluUG9vbFNpemU6IDFcbiAgICBtYXhQb29sU2l6ZTogNTBcbiJ9LCJwcm9wcyI6InByb3BzOlxuICBzcWwtc2hvdzogdHJ1ZVxuIiwicnVsZXMiOiJydWxlczpcbi0g"
            + "IUFVVEhPUklUWVxuICBwcml2aWxlZ2U6XG4gICAgdHlwZTogQUxMX1BFUk1JVFRFRFxuICB1c2VyczpcbiAgLSBhZG1pbjogdHJ1ZVxuICAgIGF1dGhlbnRpY2F0aW9uTWV0aG9kTmFtZTogJydcbiAgIC"
            + "BwYXNzd29yZDogcm9vdFxuICAgIHVzZXI6IHJvb3RAJVxuLSAhR0xPQkFMX0NMT0NLXG4gIGVuYWJsZWQ6IGZhbHNlXG4gIHByb3ZpZGVyOiBsb2NhbFxuICB0eXBlOiBUU09cbiJ9fQ==";
    
    private static final String EMPTY_DATABASE_NAME = "empty_metadata";
    
    private final Map<String, String> featureMap = new HashMap<>(1, 1F);
    
    @BeforeEach
    void setup() {
        featureMap.put(EMPTY_DATABASE_NAME, "/conf/import/empty-metadata.json");
    }
    
    @Test
    void assertImportEmptyMetaData() {
        ImportMetaDataExecutor executor = new ImportMetaDataExecutor();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        assertThrows(EmptyStorageUnitException.class, () -> executor.executeUpdate(
                new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataExecutorTest.class.getResource(featureMap.get(EMPTY_DATABASE_NAME))).getPath()), contextManager));
    }
    
    @Test
    void assertImportMetaDataFromJsonValue() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ImportMetaDataExecutor executor = new ImportMetaDataExecutor();
        executor.executeUpdate(new ImportMetaDataStatement(METADATA_VALUE, null), contextManager);
        assertNotNull(contextManager.getDatabase("normal_db"));
    }
    
    @Test
    void assertImportExistedMetaDataFromFile() {
        ImportMetaDataExecutor executor = new ImportMetaDataExecutor();
        ContextManager contextManager = mockContextManager();
        assertThrows(DatabaseCreateExistsException.class, () -> executor.executeUpdate(
                new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataExecutorTest.class.getResource(featureMap.get(EMPTY_DATABASE_NAME))).getPath()), contextManager));
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereDatabase database = mockShardingSphereDatabase();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Arrays.asList(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build()),
                        new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build()))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true"))));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), new EventBusContext());
        computeNodeInstanceContext.init(new StandaloneWorkerIdGenerator());
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        return result;
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase() {
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getName()).thenReturn(EMPTY_DATABASE_NAME);
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(storageUnits.keySet());
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return result;
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
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", createDataSource("demo_ds_0"));
        result.put("ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", name));
        result.setUsername("root");
        result.setPassword("");
        result.setMaxPoolSize(50);
        result.setMinPoolSize(1);
        return result;
    }
}
