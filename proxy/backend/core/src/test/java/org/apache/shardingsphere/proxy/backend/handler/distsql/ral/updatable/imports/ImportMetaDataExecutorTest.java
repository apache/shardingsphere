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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.codec.binary.Base64;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.generic.FileIOException;
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
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportMetaDataExecutorTest {
    
    private static final String METADATA_VALUE = "{\"meta_data\":{\"databases\":{\"normal_db\":\"databaseName: normal_db\\n"
            + "dataSources:\\n"
            + "  ds_0:\\n    password: \\n    url: jdbc:h2:mem:demo_ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL\\n    username: root\\n    minPoolSize: 1\\n    maxPoolSize: 50\\n"
            + "  ds_1:\\n    password: \\n    url: jdbc:h2:mem:demo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL\\n    username: root\\n    minPoolSize: 1\\n    maxPoolSize: 50\\n\"},"
            + "\"props\":\"props:\\n  sql-show: true\\n\","
            + "\"rules\":\"rules:\\n"
            + "- !AUTHORITY\\n  privilege:\\n    type: ALL_PERMITTED\\n  users:\\n  - admin: true\\n    authenticationMethodName: ''\\n    password: root\\n    user: root@%\\n"
            + "- !GLOBAL_CLOCK\\n  enabled: false\\n  provider: local\\n  type: TSO\\n\"}}";
    
    private static final String EMPTY_DATABASE_NAME = "empty_metadata";
    
    private static final String EMPTY_METADATA_FILE_PATH = "/conf/import/empty-metadata.json";
    
    private static final String NOT_EXIST_METADATA_FILE_PATH = "/__not_exist__/empty-metadata.json";
    
    private final ImportMetaDataExecutor executor = (ImportMetaDataExecutor) TypedSPILoader.getService(DistSQLUpdateExecutor.class, ImportMetaDataStatement.class);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("importFailureCases")
    void assertExecuteUpdateFailure(final String caseName, final ImportMetaDataStatement sqlStatement, final ContextManager contextManager, final Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> executor.executeUpdate(sqlStatement, contextManager));
    }
    
    @Test
    void assertExecuteUpdateFromJsonValue() {
        ContextManager contextManager = mockContextManager();
        executor.executeUpdate(new ImportMetaDataStatement(Base64.encodeBase64String(METADATA_VALUE.getBytes(StandardCharsets.UTF_8)), null), contextManager);
        assertNotNull(contextManager.getDatabase("normal_db"));
    }
    
    private static Stream<Arguments> importFailureCases() {
        return Stream.of(
                Arguments.of("import empty metadata", createImportMetaDataStatement(EMPTY_METADATA_FILE_PATH), mockContextManager(), EmptyStorageUnitException.class),
                Arguments.of("import existed metadata", createImportMetaDataStatement(EMPTY_METADATA_FILE_PATH), createContextManagerWithExistedDatabase(), DatabaseCreateExistsException.class),
                Arguments.of("import metadata from missing file", new ImportMetaDataStatement(null, NOT_EXIST_METADATA_FILE_PATH), mockContextManager(), FileIOException.class),
                Arguments.of("import metadata with empty file path in file branch", createStatementWithEmptyFilePathInFileBranch(), mockContextManager(), JsonProcessingException.class));
    }
    
    private static ImportMetaDataStatement createImportMetaDataStatement(final String filePath) {
        URL url = ImportMetaDataExecutorTest.class.getResource(filePath);
        assertNotNull(url);
        return new ImportMetaDataStatement(null, url.getPath());
    }
    
    private static ImportMetaDataStatement createStatementWithEmptyFilePathInFileBranch() {
        ImportMetaDataStatement result = mock(ImportMetaDataStatement.class);
        when(result.getFilePath()).thenReturn(Optional.of("/tmp/mock-metadata.json"), Optional.empty());
        when(result.getMetaDataValue()).thenReturn("");
        return result;
    }
    
    private static ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        return result;
    }
    
    private static ContextManager createContextManagerWithExistedDatabase() {
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()),
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
    
    private static ShardingSphereDatabase mockDatabase() {
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getName()).thenReturn(EMPTY_DATABASE_NAME);
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(storageUnits.keySet());
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private static Map<String, StorageUnit> createStorageUnits() {
        Map<String, DataSourcePoolProperties> propsMap = createDataSourceMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue())));
        Map<String, StorageUnit> result = new LinkedHashMap<>(propsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            when(storageUnit.getDataSourcePoolProperties()).thenReturn(entry.getValue());
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
}
