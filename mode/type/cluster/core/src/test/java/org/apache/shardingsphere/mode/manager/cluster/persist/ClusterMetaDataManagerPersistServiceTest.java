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

package org.apache.shardingsphere.mode.manager.cluster.persist;

import lombok.SneakyThrows;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.persist.service.ListenerAssistedPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    
    @BeforeEach
    void setUp() {
        metaDataManagerPersistService = new ClusterMetaDataManagerPersistService(mock(PersistRepository.class), metaDataContextManager);
        setField("metaDataPersistService", metaDataPersistService);
        setField("listenerAssistedPersistService", listenerAssistedPersistService);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    void setField(final String filedName, final Object fieldValue) {
        Field field = metaDataManagerPersistService.getClass().getDeclaredField(filedName);
        field.setAccessible(true);
        field.set(metaDataManagerPersistService, fieldValue);
        field.setAccessible(false);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        verify(metaDataPersistService.getDatabaseMetaDataService()).addDatabase("foo_db");
        verify(listenerAssistedPersistService).persistDatabaseNameListenerAssisted(any());
    }
    
    @Test
    void assertDropDatabase() {
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        metaDataManagerPersistService.dropDatabase("foo_db");
        verify(listenerAssistedPersistService).persistDatabaseNameListenerAssisted(any());
        verify(metaDataPersistService.getDatabaseMetaDataService()).dropDatabase("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema("foo_db", "foo_schema");
        verify(metaDataPersistService.getDatabaseMetaDataService()).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterNotEmptySchema() {
        assertAlterSchema(mock(ShardingSphereSchema.class));
        verify(metaDataPersistService.getDatabaseMetaDataService(), times(0)).addSchema("foo_db", "bar_schema");
    }
    
    @Test
    void assertAlterEmptySchema() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.isEmpty()).thenReturn(true);
        assertAlterSchema(schema);
        verify(metaDataPersistService.getDatabaseMetaDataService()).addSchema("foo_db", "bar_schema");
    }
    
    private void assertAlterSchema(final ShardingSphereSchema schema) {
        when(metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(schema);
        metaDataManagerPersistService.alterSchema(new AlterSchemaPOJO("foo_db", "foo_schema", "bar_schema", Collections.singleton("foo_ds")));
        verify(metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService()).persist("foo_db", "bar_schema", Collections.emptyMap());
        verify(metaDataPersistService.getDatabaseMetaDataService().getViewMetaDataPersistService()).persist("foo_db", "bar_schema", Collections.emptyMap());
        verify(metaDataPersistService.getDatabaseMetaDataService()).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertDropSchema() {
        metaDataManagerPersistService.dropSchema("foo_db", Collections.singleton("foo_schema"));
        verify(metaDataPersistService.getDatabaseMetaDataService()).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSchemaMetaData() {
        metaDataManagerPersistService.alterSchemaMetaData(new AlterSchemaMetaDataPOJO("foo_db", "foo_schema", Collections.singleton("foo_ds")));
        verify(metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService()).persist("foo_db", "foo_schema", Collections.emptyMap());
        verify(metaDataPersistService.getDatabaseMetaDataService().getViewMetaDataPersistService()).persist("foo_db", "foo_schema", Collections.emptyMap());
    }
    
    @Test
    void assertAlterSingleRuleConfiguration() {
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(Arrays.asList(new SingleRuleConfiguration(), mock(RuleConfiguration.class)));
        when(metaDataPersistService.getDatabaseRulePersistService().persistConfigurations("foo_db", ruleConfigs)).thenReturn(Collections.emptyList());
        metaDataManagerPersistService.alterSingleRuleConfiguration("foo_db", ruleConfigs);
        assertThat(ruleConfigs.size(), is(1));
        verify(metaDataPersistService.getMetaDataVersionPersistService()).switchActiveVersion(Collections.emptyList());
    }
    
    @Test
    void assertAlterNullRuleConfiguration() {
        metaDataManagerPersistService.alterRuleConfiguration("foo_db", null);
        verify(metaDataPersistService.getDatabaseRulePersistService(), times(0)).persistConfigurations(eq("foo_db"), any());
    }
    
    @Test
    void assertAlterRuleConfiguration() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterRuleConfiguration("foo_db", ruleConfig);
        verify(metaDataPersistService.getDatabaseRulePersistService()).persistConfigurations("foo_db", Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertRemoveNullRuleConfigurationItem() {
        metaDataManagerPersistService.removeRuleConfigurationItem("foo_db", null);
        verify(metaDataPersistService.getDatabaseRulePersistService(), times(0)).deleteConfigurations(eq("foo_db"), any());
    }
    
    @Test
    void assertRemoveRuleConfigurationItem() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.removeRuleConfigurationItem("foo_db", ruleConfig);
        verify(metaDataPersistService.getDatabaseRulePersistService()).deleteConfigurations("foo_db", Collections.singleton(ruleConfig));
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
        when(table.getName()).thenReturn("foo_tbl");
        metaDataManagerPersistService.createTable("foo_db", "foo_schema", table, "foo_ds");
        verify(metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService()).persist("foo_db", "foo_schema", Maps.of("foo_tbl", table));
    }
    
    @Test
    void assertDropTables() {
        metaDataManagerPersistService.dropTables("foo_db", "foo_schema", Collections.singleton("foo_tbl"));
        verify(metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService()).delete("foo_db", "foo_schema", "foo_tbl");
    }
}
