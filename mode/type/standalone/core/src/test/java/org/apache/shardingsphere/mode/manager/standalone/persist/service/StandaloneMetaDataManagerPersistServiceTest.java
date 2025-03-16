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

package org.apache.shardingsphere.mode.manager.standalone.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.changed.RuleItemChangedNodePathBuilder;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.MetaDataVersion;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandaloneMetaDataManagerPersistServiceTest {
    
    private StandaloneMetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistFacade metaDataPersistFacade;
    
    @BeforeEach
    @SneakyThrows(ReflectiveOperationException.class)
    void setUp() {
        metaDataManagerPersistService = new StandaloneMetaDataManagerPersistService(metaDataContextManager);
        Plugins.getMemberAccessor().set(StandaloneMetaDataManagerPersistService.class.getDeclaredField("metaDataPersistFacade"), metaDataManagerPersistService, metaDataPersistFacade);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        verify(metaDataContextManager.getDatabaseMetaDataManager()).addDatabase("foo_db");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase()).add("foo_db");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabaseByAlter(eq("foo_db"), any(ShardingSphereDatabase.class), any(ShardingSphereDatabase.class));
    }
    
    @Test
    void assertDropDatabase() {
        metaDataManagerPersistService.dropDatabase(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()));
        verify(metaDataContextManager.getDatabaseMetaDataManager()).dropDatabase("foo_db");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase()).drop("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), "foo_schema");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchema() {
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = mock(DatabaseMetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getDatabaseMetaDataFacade()).thenReturn(databaseMetaDataFacade);
        metaDataManagerPersistService.alterSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()),
                "foo_schema", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        verify(databaseMetaDataFacade).alterSchema(any(), eq("foo_schema"), anyCollection(), anyCollection(), anyCollection(), anyCollection());
    }
    
    @Test
    void assertRenameSchemaNameWithEmptyAlteredSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = mock(DatabaseMetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getDatabaseMetaDataFacade()).thenReturn(databaseMetaDataFacade);
        metaDataManagerPersistService.renameSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), "foo_schema", "bar_schema");
        verify(databaseMetaDataFacade.getSchema(), times(0)).add("foo_db", "bar_schema");
        verify(databaseMetaDataFacade).renameSchema(any(), any(), eq("foo_schema"), eq("bar_schema"));
    }
    
    @Test
    void assertRenameSchemaNameWithNotEmptyAlteredSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getSchema("foo_schema")).thenReturn(new ShardingSphereSchema("foo_schema"));
        when(database.getSchema("bar_schema")).thenReturn(new ShardingSphereSchema("bar_schema"));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = mock(DatabaseMetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getDatabaseMetaDataFacade()).thenReturn(databaseMetaDataFacade);
        metaDataManagerPersistService.renameSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), "foo_schema", "bar_schema");
        verify(databaseMetaDataFacade).renameSchema(any(), any(), eq("foo_schema"), eq("bar_schema"));
    }
    
    @Test
    void assertDropSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getSchema("foo_schema")).thenReturn(new ShardingSphereSchema("foo_schema"));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        metaDataManagerPersistService.dropSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), Collections.singleton("foo_schema"));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
        verify(metaDataContextManager.getDatabaseMetaDataManager()).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSingleRuleConfiguration() throws SQLException {
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        SingleRule singleRule = mock(SingleRule.class);
        when(singleRule.getConfiguration()).thenReturn(singleRuleConfig);
        metaDataManagerPersistService.alterSingleRuleConfiguration(
                new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), new RuleMetaData(Collections.singleton(singleRule)));
        verify(metaDataContextManager.getDatabaseRuleConfigurationManager()).refresh("foo_db", singleRuleConfig, true);
    }
    
    @Test
    void assertAlterNullRuleConfiguration() throws SQLException {
        metaDataManagerPersistService.alterRuleConfiguration(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), null);
        verify(metaDataPersistFacade, times(0)).getVersionService();
    }
    
    @Test
    void assertAlterRuleConfiguration() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, RETURNS_DEEP_STUBS);
        MetaDataVersion metaDataVersion = mock(MetaDataVersion.class);
        DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath("foo_db", "fixture", new DatabaseRuleItem("foo_type"));
        when(metaDataVersion.getNodePath()).thenReturn(databaseRuleNodePath);
        when(metaDataPersistFacade.getDatabaseRuleService().persist("foo_db", Collections.singleton(ruleConfig))).thenReturn(Collections.singleton(metaDataVersion));
        RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder = mock(RuleItemChangedNodePathBuilder.class);
        when(ruleItemChangedNodePathBuilder.build(eq("foo_db"), any())).thenReturn(Optional.of(databaseRuleNodePath));
        setRuleItemChangedBuildExecutor(ruleItemChangedNodePathBuilder);
        when(metaDataPersistFacade.getRepository().query("/metadata/foo_db/rules/fixture/foo_type/active_version")).thenReturn("0");
        metaDataManagerPersistService.alterRuleConfiguration(database, ruleConfig);
        verify(metaDataContextManager.getDatabaseRuleItemManager()).alter(databaseRuleNodePath);
    }
    
    @Test
    void assertRemoveNullRuleConfigurationItem() throws SQLException {
        metaDataManagerPersistService.removeRuleConfigurationItem(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), null);
        verify(metaDataPersistFacade, times(0)).getVersionService();
    }
    
    @Test
    void assertRemoveRuleConfigurationItem() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, RETURNS_DEEP_STUBS);
        MetaDataVersion metaDataVersion = mock(MetaDataVersion.class);
        when(metaDataPersistFacade.getDatabaseRuleService().delete("foo_db", Collections.singleton(ruleConfig))).thenReturn(Collections.singleton(metaDataVersion));
        RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder = mock(RuleItemChangedNodePathBuilder.class);
        DatabaseRuleNodePath databaseRuleNodePath = mock(DatabaseRuleNodePath.class);
        when(metaDataVersion.getNodePath()).thenReturn(databaseRuleNodePath);
        when(ruleItemChangedNodePathBuilder.build(eq("foo_db"), any())).thenReturn(Optional.of(databaseRuleNodePath));
        setRuleItemChangedBuildExecutor(ruleItemChangedNodePathBuilder);
        metaDataManagerPersistService.removeRuleConfigurationItem(database, ruleConfig);
        verify(metaDataContextManager.getDatabaseRuleItemManager()).drop(databaseRuleNodePath);
    }
    
    @Test
    void assertRemoveRuleConfiguration() {
        metaDataManagerPersistService.removeRuleConfiguration(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), "foo_rule");
        verify(metaDataPersistFacade.getDatabaseRuleService()).delete("foo_db", "foo_rule");
    }
    
    @Test
    void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        metaDataManagerPersistService.alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataContextManager.getGlobalConfigurationManager()).alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataPersistFacade.getGlobalRuleService()).persist(Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertAlterProperties() {
        Properties props = new Properties();
        metaDataManagerPersistService.alterProperties(props);
        verify(metaDataContextManager.getGlobalConfigurationManager()).alterProperties(props);
        verify(metaDataPersistFacade.getPropsService()).persist(props);
    }
    
    @Test
    void assertCreateTable() throws SQLException {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE);
        metaDataManagerPersistService.createTable(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList()), "foo_schema", table);
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Collections.singleton(table));
    }
    
    @Test
    void assertDropTables() throws SQLException {
        metaDataManagerPersistService.dropTables(new ShardingSphereDatabase(
                "foo_db", mock(), mock(), mock(), Collections.emptyList()), "foo_schema", Collections.singleton("foo_tbl"));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getTable()).drop("foo_db", "foo_schema", "foo_tbl");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setRuleItemChangedBuildExecutor(final RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder) {
        Plugins.getMemberAccessor().set(
                StandaloneMetaDataManagerPersistService.class.getDeclaredField("ruleItemChangedNodePathBuilder"), metaDataManagerPersistService, ruleItemChangedNodePathBuilder);
    }
}
