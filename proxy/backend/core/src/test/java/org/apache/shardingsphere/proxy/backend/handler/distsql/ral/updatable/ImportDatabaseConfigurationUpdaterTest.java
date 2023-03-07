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
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
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
public final class ImportDatabaseConfigurationUpdaterTest {
    
    private ImportDatabaseConfigurationUpdater importDatabaseConfigUpdater;
    
    @Test
    public void assertImportDatabaseExecutorForSharding() throws SQLException {
        String databaseName = "sharding_db";
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-sharding.yaml")));
    }
    
    @Test
    public void assertImportDatabaseExecutorForReadwriteSplitting() throws SQLException {
        String databaseName = "readwrite_splitting_db";
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-readwrite-splitting.yaml")));
    }
    
    @Test
    public void assertImportDatabaseExecutorForDatabaseDiscovery() throws SQLException {
        String databaseName = "database_discovery_db";
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-database-discovery.yaml")));
    }
    
    @Test
    public void assertImportDatabaseExecutorForEncrypt() throws SQLException {
        String databaseName = "encrypt_db";
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-encrypt.yaml")));
    }
    
    @Test
    public void assertImportDatabaseExecutorForShadow() throws SQLException {
        String databaseName = "shadow_db";
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-shadow.yaml")));
    }
    
    @Test
    public void assertImportDatabaseExecutorForMask() throws SQLException {
        String databaseName = "mask_db";
        init(databaseName);
        importDatabaseConfigUpdater.executeUpdate(databaseName, new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-mask.yaml")));
    }
    
    @Test
    public void assertImportExistedDatabase() {
        String databaseName = "sharding_db";
        init(databaseName);
        when(ProxyContext.getInstance().databaseExists(databaseName)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigUpdater.executeUpdate(databaseName,
                new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-sharding.yaml"))));
    }
    
    @Test
    public void assertImportEmptyDatabaseName() {
        String databaseName = "sharding_db";
        init(databaseName);
        assertThrows(NullPointerException.class, () -> importDatabaseConfigUpdater.executeUpdate(databaseName,
                new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-empty-database-name.yaml"))));
    }
    
    @Test
    public void assertImportEmptyDataSource() {
        String databaseName = "sharding_db";
        init(databaseName);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigUpdater.executeUpdate(databaseName,
                new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-empty-data-source.yaml"))));
    }
    
    @Test
    public void assertImportDuplicatedLogicTable() {
        String databaseName = "sharding_db";
        init(databaseName);
        assertThrows(DuplicateRuleException.class, () -> importDatabaseConfigUpdater.executeUpdate(databaseName,
                new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-duplicated-logic-table.yaml"))));
    }
    
    @Test
    public void assertImportInvalidAlgorithm() {
        String databaseName = "sharding_db";
        init(databaseName);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> importDatabaseConfigUpdater.executeUpdate(databaseName,
                new ImportDatabaseConfigurationStatement(getDatabaseConfigurationFilePath("/conf/import/config-invalid-algorithm.yaml"))));
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
    
    private String getDatabaseConfigurationFilePath(final String filePath) {
        return ImportDatabaseConfigurationUpdaterTest.class.getResource(filePath).getPath();
    }
}
