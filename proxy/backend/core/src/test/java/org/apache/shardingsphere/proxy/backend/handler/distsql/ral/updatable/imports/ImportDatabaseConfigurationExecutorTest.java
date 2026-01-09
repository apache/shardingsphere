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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.metadata.MissingRequiredDatabaseException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportDatabaseConfigurationExecutorTest {
    
    @Test
    void assertImportDatabaseExecutorForSharding() {
        ContextManager contextManager = mockContextManager("sharding_db");
        assertExecute(contextManager, "/conf/import/database-sharding.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForReadwriteSplitting() {
        ContextManager contextManager = mockContextManager("readwrite_splitting_db");
        assertExecute(contextManager, "/conf/import/database-readwrite-splitting.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForEncrypt() {
        ContextManager contextManager = mockContextManager("encrypt_db");
        assertExecute(contextManager, "/conf/import/database-encrypt.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForShadow() {
        ContextManager contextManager = mockContextManager("shadow_db");
        assertExecute(contextManager, "/conf/import/database-shadow.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForMask() {
        ContextManager contextManager = mockContextManager("mask_db");
        assertExecute(contextManager, "/conf/import/database-mask.yaml");
    }
    
    @Test
    void assertImportExistedDatabase() {
        ContextManager contextManager = mockContextManager("sharding_db");
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("sharding_db")).thenReturn(true);
        assertThrows(DatabaseCreateExistsException.class, () -> assertExecute(contextManager, "/conf/import/database-sharding.yaml"));
    }
    
    @Test
    void assertImportEmptyDatabaseName() {
        ContextManager contextManager = mockContextManager("sharding_db");
        assertThrows(MissingRequiredDatabaseException.class, () -> assertExecute(contextManager, "/conf/import/database-empty-database-name.yaml"));
    }
    
    @Test
    void assertImportDuplicatedLogicTable() {
        ContextManager contextManager = mockContextManager("sharding_db");
        assertThrows(DuplicateRuleException.class, () -> assertExecute(contextManager, "/conf/import/database-duplicated-logic-table.yaml"));
    }
    
    @Test
    void assertImportInvalidAlgorithm() {
        ContextManager contextManager = mockContextManager("sharding_db");
        assertThrows(ServiceProviderNotFoundException.class, () -> assertExecute(contextManager, "/conf/import/database-invalid-algorithm.yaml"));
    }
    
    private void assertExecute(final ContextManager contextManager, final String filePath) {
        ImportDatabaseConfigurationExecutor executor = new ImportDatabaseConfigurationExecutor();
        URL url = ImportDatabaseConfigurationExecutorTest.class.getResource(filePath);
        assertNotNull(url);
        executor.executeUpdate(new ImportDatabaseConfigurationStatement(url.getPath()), contextManager);
    }
    
    private ContextManager mockContextManager(final String databaseName) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(databaseName);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData().getRules()).thenReturn(new LinkedList<>());
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        StorageUnit storageUnit = mock(StorageUnit.class);
        DataSource dataSource = new MockedDataSource();
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(new HashMap<>(Collections.singletonMap("foo_ds", storageUnit)));
        when(database.getResourceMetaData().getDataSourceMap()).thenReturn(Collections.singletonMap("foo_ds", dataSource));
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(result.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
        when(result.getMetaDataContexts().getMetaData().getDatabase(databaseName)).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "MySQL"))));
        when(result.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        return result;
    }
}
