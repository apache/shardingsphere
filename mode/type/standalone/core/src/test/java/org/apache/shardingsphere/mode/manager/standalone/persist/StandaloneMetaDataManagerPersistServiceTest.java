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

package org.apache.shardingsphere.mode.manager.standalone.persist;

import lombok.SneakyThrows;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.event.builder.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
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
    private MetaDataPersistService metaDataPersistService;
    
    @BeforeEach
    @SneakyThrows(ReflectiveOperationException.class)
    void setUp() {
        metaDataManagerPersistService = new StandaloneMetaDataManagerPersistService(mock(PersistRepository.class), metaDataContextManager);
        Plugins.getMemberAccessor().set(StandaloneMetaDataManagerPersistService.class.getDeclaredField("metaDataPersistService"), metaDataManagerPersistService, metaDataPersistService);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        verify(metaDataContextManager.getSchemaMetaDataManager()).addDatabase("foo_db");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getDatabase()).add("foo_db");
    }
    
    @Test
    void assertDropDatabase() {
        metaDataManagerPersistService.dropDatabase("foo_db");
        verify(metaDataContextManager.getSchemaMetaDataManager()).dropDatabase("foo_db");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getDatabase()).drop("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema("foo_db", "foo_schema");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchemaWithEmptyAlteredSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData()).thenReturn(metaData);
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = mock(DatabaseMetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getDatabaseMetaDataFacade()).thenReturn(databaseMetaDataFacade);
        metaDataManagerPersistService.alterSchema(new AlterSchemaPOJO("foo_db", "foo_schema", "bar_schema", Collections.singleton("foo_ds")));
        verify(databaseMetaDataFacade.getSchema(), times(0)).add("foo_db", "bar_schema");
        verify(databaseMetaDataFacade.getTable()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataFacade.getView()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataFacade.getSchema()).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchemaWithNotEmptyAlteredSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.isEmpty()).thenReturn(true);
        when(database.getSchema("foo_schema")).thenReturn(schema);
        when(database.getSchema("bar_schema")).thenReturn(schema);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData()).thenReturn(metaData);
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = mock(DatabaseMetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getDatabaseMetaDataFacade()).thenReturn(databaseMetaDataFacade);
        metaDataManagerPersistService.alterSchema(new AlterSchemaPOJO("foo_db", "foo_schema", "bar_schema", Collections.singleton("foo_ds")));
        verify(databaseMetaDataFacade.getSchema()).add("foo_db", "bar_schema");
        verify(databaseMetaDataFacade.getTable()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataFacade.getView()).persist("foo_db", "bar_schema", new HashMap<>());
        verify(databaseMetaDataFacade.getSchema()).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertDropSchema() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData()).thenReturn(metaData);
        metaDataManagerPersistService.dropSchema("foo_db", Collections.singleton("foo_schema"));
        verify(database).dropSchema(any());
    }
    
    @Test
    void assertAlterSchemaMetaData() {
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = mock(DatabaseMetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getDatabaseMetaDataFacade()).thenReturn(databaseMetaDataFacade);
        metaDataManagerPersistService.alterSchemaMetaData(new AlterSchemaMetaDataPOJO("foo_db", "foo_schema", Collections.singleton("foo_ds")));
        verify(databaseMetaDataFacade.getTable()).persist("foo_db", "foo_schema", new HashMap<>());
    }
    
    @Test
    void assertAlterSingleRuleConfiguration() throws SQLException {
        RuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterSingleRuleConfiguration("foo_db", new LinkedList<>(Arrays.asList(singleRuleConfig, mock(RuleConfiguration.class))));
        verify(metaDataPersistService.getMetaDataVersionPersistService()).switchActiveVersion(any());
        verify(metaDataContextManager.getDatabaseRuleConfigurationManager()).alterRuleConfiguration("foo_db", singleRuleConfig);
    }
    
    @Test
    void assertAlterNullRuleConfiguration() throws SQLException {
        metaDataManagerPersistService.alterRuleConfiguration("foo_db", null);
        verify(metaDataPersistService, times(0)).getMetaDataVersionPersistService();
    }
    
    @Test
    void assertAlterRuleConfiguration() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData()).thenReturn(metaData);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, RETURNS_DEEP_STUBS);
        Collection<MetaDataVersion> metaDataVersion = Collections.singleton(mock(MetaDataVersion.class));
        when(metaDataPersistService.getDatabaseRulePersistService().persist("foo_db", Collections.singleton(ruleConfig))).thenReturn(metaDataVersion);
        AlterRuleItemEvent event = mock(AlterRuleItemEvent.class);
        RuleConfigurationEventBuilder ruleConfigurationEventBuilder = mock(RuleConfigurationEventBuilder.class);
        when(ruleConfigurationEventBuilder.build(eq("foo_db"), any())).thenReturn(Optional.of(event));
        setRuleConfigurationEventBuilder(ruleConfigurationEventBuilder);
        metaDataManagerPersistService.alterRuleConfiguration("foo_db", ruleConfig);
        verify(metaDataPersistService.getMetaDataVersionPersistService()).switchActiveVersion(metaDataVersion);
        verify(metaDataContextManager.getRuleItemManager()).alterRuleItem(event);
    }
    
    @Test
    void assertRemoveNullRuleConfigurationItem() throws SQLException {
        metaDataManagerPersistService.removeRuleConfigurationItem("foo_db", null);
        verify(metaDataPersistService, times(0)).getMetaDataVersionPersistService();
    }
    
    @Test
    void assertRemoveRuleConfigurationItem() throws SQLException {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class, RETURNS_DEEP_STUBS);
        Collection<MetaDataVersion> metaDataVersion = Collections.singleton(mock(MetaDataVersion.class));
        when(metaDataPersistService.getDatabaseRulePersistService().delete("foo_db", Collections.singleton(ruleConfig))).thenReturn(metaDataVersion);
        RuleConfigurationEventBuilder ruleConfigurationEventBuilder = mock(RuleConfigurationEventBuilder.class);
        DropRuleItemEvent event = mock(DropRuleItemEvent.class);
        when(ruleConfigurationEventBuilder.build(eq("foo_db"), any())).thenReturn(Optional.of(event));
        setRuleConfigurationEventBuilder(ruleConfigurationEventBuilder);
        metaDataManagerPersistService.removeRuleConfigurationItem("foo_db", ruleConfig);
        verify(metaDataContextManager.getRuleItemManager()).dropRuleItem(event);
    }
    
    @Test
    void assertRemoveRuleConfiguration() {
        metaDataManagerPersistService.removeRuleConfiguration("foo_db", "foo_rule");
        verify(metaDataPersistService.getDatabaseRulePersistService()).delete("foo_db", "foo_rule");
    }
    
    @Test
    void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        metaDataManagerPersistService.alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataContextManager.getGlobalConfigurationManager()).alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataPersistService.getGlobalRuleService()).persist(Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertAlterProperties() {
        Properties props = new Properties();
        metaDataManagerPersistService.alterProperties(props);
        verify(metaDataContextManager.getGlobalConfigurationManager()).alterProperties(props);
        verify(metaDataPersistService.getPropsService()).persist(props);
    }
    
    @Test
    void assertCreateTable() {
        ShardingSphereTable table = new ShardingSphereTable();
        metaDataManagerPersistService.createTable("foo_db", "foo_schema", table, "foo_ds");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Maps.of("", table));
    }
    
    @Test
    void assertDropTables() {
        metaDataManagerPersistService.dropTables("foo_db", "foo_schema", Collections.singleton("foo_tbl"));
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getTable()).drop("foo_db", "foo_schema", "foo_tbl");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setRuleConfigurationEventBuilder(final RuleConfigurationEventBuilder ruleConfigurationEventBuilder) {
        Plugins.getMemberAccessor().set(StandaloneMetaDataManagerPersistService.class.getDeclaredField("ruleConfigurationEventBuilder"), metaDataManagerPersistService, ruleConfigurationEventBuilder);
    }
}
