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

package org.apache.shardingsphere.mode.manager.cluster.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.persist.service.unified.ListenerAssistedType;
import org.apache.shardingsphere.mode.persist.service.unified.ListenerAssistedPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterMetaDataManagerPersistServiceTest {
    
    private ClusterMetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService metaDataPersistService;
    
    @Mock
    private ListenerAssistedPersistService listenerAssistedPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @BeforeEach
    void setUp() {
        metaDataManagerPersistService = new ClusterMetaDataManagerPersistService(mock(PersistRepository.class), metaDataContextManager);
        Plugins.getMemberAccessor().set(ClusterMetaDataManagerPersistService.class.getDeclaredField("metaDataPersistService"), metaDataManagerPersistService, metaDataPersistService);
        Plugins.getMemberAccessor().set(ClusterMetaDataManagerPersistService.class.getDeclaredField("listenerAssistedPersistService"), metaDataManagerPersistService, listenerAssistedPersistService);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getDatabase()).add("foo_db");
        verify(listenerAssistedPersistService).persistDatabaseNameListenerAssisted("foo_db", ListenerAssistedType.CREATE_DATABASE);
    }
    
    @Test
    void assertDropDatabase() {
        when(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        metaDataManagerPersistService.dropDatabase("foo_db");
        verify(listenerAssistedPersistService).persistDatabaseNameListenerAssisted("foo_db", ListenerAssistedType.DROP_DATABASE);
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getDatabase()).drop("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema("foo_db", "foo_schema");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterNotEmptySchema() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        assertAlterSchema(schema);
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getSchema(), times(0)).add("foo_db", "bar_schema");
    }
    
    @Test
    void assertAlterEmptySchema() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.isEmpty()).thenReturn(true);
        assertAlterSchema(schema);
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "bar_schema");
    }
    
    private void assertAlterSchema(final ShardingSphereSchema schema) {
        when(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(schema);
        metaDataManagerPersistService.alterSchema(new AlterSchemaPOJO("foo_db", "foo_schema", "bar_schema", Collections.singleton("foo_ds")));
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getTable()).persist(eq("foo_db"), eq("bar_schema"), anyCollection());
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getView()).persist(eq("foo_db"), eq("bar_schema"), anyCollection());
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertDropSchema() {
        metaDataManagerPersistService.dropSchema("foo_db", Collections.singleton("foo_schema"));
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchemaMetaData() {
        metaDataManagerPersistService.alterSchemaMetaData(new AlterSchemaMetaDataPOJO("foo_db", "foo_schema", Collections.singleton("foo_ds")));
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Collections.emptyList());
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getView()).persist("foo_db", "foo_schema", Collections.emptyList());
    }
    
    @Test
    void assertAlterSingleRuleConfiguration() {
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(Arrays.asList(new SingleRuleConfiguration(), mock(RuleConfiguration.class)));
        when(metaDataPersistService.getDatabaseRulePersistService().persist("foo_db", ruleConfigs)).thenReturn(Collections.emptyList());
        metaDataManagerPersistService.alterSingleRuleConfiguration("foo_db", ruleConfigs);
        assertThat(ruleConfigs.size(), is(1));
        verify(metaDataPersistService.getMetaDataVersionPersistService()).switchActiveVersion(Collections.emptyList());
    }
    
    @Test
    void assertAlterNullRuleConfiguration() {
        metaDataManagerPersistService.alterRuleConfiguration("foo_db", null);
        verify(metaDataPersistService.getDatabaseRulePersistService(), times(0)).persist(eq("foo_db"), any());
    }
    
    @Test
    void assertAlterRuleConfiguration() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterRuleConfiguration("foo_db", ruleConfig);
        verify(metaDataPersistService.getDatabaseRulePersistService()).persist("foo_db", Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertRemoveNullRuleConfigurationItem() {
        metaDataManagerPersistService.removeRuleConfigurationItem("foo_db", null);
        verify(metaDataPersistService.getDatabaseRulePersistService(), times(0)).delete(eq("foo_db"), anyCollection());
    }
    
    @Test
    void assertRemoveRuleConfigurationItem() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.removeRuleConfigurationItem("foo_db", ruleConfig);
        verify(metaDataPersistService.getDatabaseRulePersistService()).delete("foo_db", Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertRemoveRuleConfiguration() {
        metaDataManagerPersistService.removeRuleConfiguration("foo_db", "fixtureRule");
        verify(metaDataPersistService.getDatabaseRulePersistService()).delete("foo_db", "fixtureRule");
    }
    
    @Test
    void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataPersistService.getGlobalRuleService()).persist(Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertAlterProperties() {
        Properties props = new Properties();
        metaDataManagerPersistService.alterProperties(props);
        verify(metaDataPersistService.getPropsService()).persist(props);
    }
    
    @Test
    void assertCreateTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        metaDataManagerPersistService.createTable("foo_db", "foo_schema", table, "foo_ds");
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Collections.singleton(table));
    }
    
    @Test
    void assertDropTables() {
        metaDataManagerPersistService.dropTables("foo_db", "foo_schema", Collections.singleton("foo_tbl"));
        verify(metaDataPersistService.getDatabaseMetaDataFacade().getTable()).drop("foo_db", "foo_schema", "foo_tbl");
    }
}
