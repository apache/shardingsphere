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
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.YamlDatabaseConfigurationImportExecutor;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ImportDatabaseConfigurationUpdaterTest {
    
    private ImportDatabaseConfigurationUpdater importDatabaseConfigUpdater;
    
    @Test
    void assertImportDatabaseExecutorForSharding() throws SQLException {
        assertExecute("sharding_db", "/conf/import/config-sharding.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForReadwriteSplitting() throws SQLException {
        assertExecute("readwrite_splitting_db", "/conf/import/config-readwrite-splitting.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForEncrypt() throws SQLException {
        assertExecute("encrypt_db", "/conf/import/config-encrypt.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForShadow() throws SQLException {
        assertExecute("shadow_db", "/conf/import/config-shadow.yaml");
    }
    
    @Test
    void assertImportDatabaseExecutorForMask() throws SQLException {
        assertExecute("mask_db", "/conf/import/config-mask.yaml");
    }
    
    @Test
    void assertImportExistedDatabase() {
        String databaseName = "sharding_db";
        when(ProxyContext.getInstance().databaseExists(databaseName)).thenReturn(true);
        assertThrows(UnsupportedSQLOperationException.class, () -> assertExecute(databaseName, "/conf/import/config-sharding.yaml"));
    }
    
    @Test
    void assertImportEmptyDatabaseName() {
        assertThrows(UnsupportedSQLOperationException.class, () -> assertExecute("sharding_db", "/conf/import/config-empty-database-name.yaml"));
    }
    
    @Test
    void assertImportEmptyDataSource() {
        assertThrows(MissingRequiredDataSourcesException.class, () -> assertExecute("sharding_db", "/conf/import/config-empty-data-source.yaml"));
    }
    
    @Test
    void assertImportDuplicatedLogicTable() {
        assertThrows(DuplicateRuleException.class, () -> assertExecute("sharding_db", "/conf/import/config-duplicated-logic-table.yaml"));
    }
    
    @Test
    void assertImportInvalidAlgorithm() {
        assertThrows(ServiceProviderNotFoundServerException.class, () -> assertExecute("sharding_db", "/conf/import/config-invalid-algorithm.yaml"));
    }
    
    private void assertExecute(final String databaseName, final String filePath) throws SQLException {
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(ImportDatabaseConfigurationUpdaterTest.class.getResource(filePath).getPath()));
    }
    
    @SneakyThrows({IllegalAccessException.class, NoSuchFieldException.class})
    private void init(final String databaseName) {
        ContextManager contextManager = mockContextManager(databaseName);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        importDatabaseConfigUpdater = new ImportDatabaseConfigurationUpdater();
        YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor = new YamlDatabaseConfigurationImportExecutor();
        Plugins.getMemberAccessor().set(importDatabaseConfigUpdater.getClass().getDeclaredField("databaseConfigImportExecutor"), importDatabaseConfigUpdater, databaseConfigImportExecutor);
        Plugins.getMemberAccessor().set(databaseConfigImportExecutor.getClass().getDeclaredField("validateHandler"), databaseConfigImportExecutor, mock(DataSourcePropertiesValidateHandler.class));
    }
    
    private ContextManager mockContextManager(final String databaseName) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(dataSourceContainedRule));
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
