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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.handler.exception.datasource.MissingRequiredDataSourcesException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.MissingDatabaseNameException;
import org.apache.shardingsphere.proxy.backend.util.YamlDatabaseConfigurationImportExecutor;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDriver;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportDatabaseConfigurationExecutorTest {
    
    private ImportDatabaseConfigurationExecutor executor;
    
    @BeforeAll
    static void setUp() throws ClassNotFoundException {
        Class.forName(MockedDriver.class.getName());
    }
    
    @Test
    void assertImportDatabaseExecutorForSharding() throws SQLException {
        assertExecute("sharding_db", "/conf/import/database-sharding.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForReadwriteSplitting() throws SQLException {
        assertExecute("readwrite_splitting_db", "/conf/import/database-readwrite-splitting.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForEncrypt() throws SQLException {
        assertExecute("encrypt_db", "/conf/import/database-encrypt.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForShadow() throws SQLException {
        assertExecute("shadow_db", "/conf/import/database-shadow.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForMask() throws SQLException {
        assertExecute("mask_db", "/conf/import/database-mask.yaml");
    }
    
    @Test
    void assertImportExistedDatabase() {
        String databaseName = "sharding_db";
        when(ProxyContext.getInstance().databaseExists(databaseName)).thenReturn(true);
        assertThrows(UnsupportedSQLOperationException.class, () -> assertExecute(databaseName, "/conf/import/database-sharding.yaml"));
    }
    
    @Test
    void assertImportEmptyDatabaseName() {
        assertThrows(MissingDatabaseNameException.class, () -> assertExecute("sharding_db", "/conf/import/database-empty-database-name.yaml"));
    }
    
    @Test
    void assertImportEmptyDataSource() {
        assertThrows(MissingRequiredDataSourcesException.class, () -> assertExecute("sharding_db", "/conf/import/database-empty-data-source.yaml"));
    }
    
    @Test
    void assertImportDuplicatedLogicTable() {
        assertThrows(DuplicateRuleException.class, () -> assertExecute("sharding_db", "/conf/import/database-duplicated-logic-table.yaml"));
    }
    
    @Test
    void assertImportInvalidAlgorithm() {
        assertThrows(ServiceProviderNotFoundException.class, () -> assertExecute("sharding_db", "/conf/import/database-invalid-algorithm.yaml"));
    }
    
    private void assertExecute(final String databaseName, final String filePath) throws SQLException {
        init(databaseName);
        executor.executeUpdate(new ImportDatabaseConfigurationStatement(ImportDatabaseConfigurationExecutorTest.class.getResource(filePath).getPath()), mock(ContextManager.class));
    }
    
    @SneakyThrows({IllegalAccessException.class, NoSuchFieldException.class})
    private void init(final String databaseName) {
        ContextManager contextManager = mockContextManager(databaseName);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor = new ImportDatabaseConfigurationExecutor();
        YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor = new YamlDatabaseConfigurationImportExecutor();
        Plugins.getMemberAccessor().set(executor.getClass().getDeclaredField("databaseConfigImportExecutor"), executor, databaseConfigImportExecutor);
        Plugins.getMemberAccessor().set(databaseConfigImportExecutor.getClass().getDeclaredField("validateHandler"), databaseConfigImportExecutor, mock(DataSourcePoolPropertiesValidator.class));
    }
    
    private ContextManager mockContextManager(final String databaseName) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(new HashMap<>(Collections.singletonMap("foo_ds", storageUnit)));
        when(result.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap(databaseName, database));
        when(result.getMetaDataContexts().getMetaData().getDatabase(databaseName)).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(createProperties()));
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "MySQL");
        return result;
    }
}
