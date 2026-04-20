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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerCoordinatorType;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerPersistCoordinator;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterMetaDataManagerPersistServiceTest {
    
    private ClusterMetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistFacade metaDataPersistFacade;
    
    @Mock
    private ClusterDatabaseListenerPersistCoordinator clusterDatabaseListenerPersistCoordinator;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @BeforeEach
    void setUp() {
        metaDataManagerPersistService = new ClusterMetaDataManagerPersistService(metaDataContextManager, mock(ClusterPersistRepository.class));
        Plugins.getMemberAccessor().set(ClusterMetaDataManagerPersistService.class.getDeclaredField("metaDataPersistFacade"), metaDataManagerPersistService, metaDataPersistFacade);
        Plugins.getMemberAccessor().set(ClusterMetaDataManagerPersistService.class.getDeclaredField("clusterDatabaseListenerPersistCoordinator"),
                metaDataManagerPersistService, clusterDatabaseListenerPersistCoordinator);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase()).add("foo_db");
        verify(clusterDatabaseListenerPersistCoordinator).persist("foo_db", ClusterDatabaseListenerCoordinatorType.CREATE);
        verify(clusterDatabaseListenerPersistCoordinator).delete("foo_db");
    }
    
    @Test
    void assertDropDatabase() {
        metaDataManagerPersistService.dropDatabase(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())));
        verify(clusterDatabaseListenerPersistCoordinator).persist("foo_db", ClusterDatabaseListenerCoordinatorType.DROP);
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase()).drop("foo_db");
        verify(clusterDatabaseListenerPersistCoordinator).delete("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())), "foo_schema");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenameSchema() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(schema);
        metaDataManagerPersistService.renameSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())), "foo_schema",
                "bar_schema");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).renameSchema(any(), any(), eq("foo_schema"), eq("bar_schema"));
    }
    
    @Test
    void assertDropSchema() {
        metaDataManagerPersistService.dropSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                Collections.singleton("foo_schema"));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertAlterSingleRuleConfiguration() {
        SingleRule singleRule = mock(SingleRule.class);
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        when(singleRule.getConfiguration()).thenReturn(singleRuleConfig);
        when(metaDataPersistFacade.getDatabaseRuleService().persist("foo_db", Collections.singleton(singleRuleConfig))).thenReturn(Collections.emptyList());
        metaDataManagerPersistService.alterSingleRuleConfiguration(
                new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                new RuleMetaData(Collections.singleton(singleRule)));
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist("foo_db", Collections.singleton(singleRuleConfig));
    }
    
    @Test
    void assertAlterNullRuleConfiguration() {
        metaDataManagerPersistService.alterRuleConfiguration(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                null);
        verify(metaDataPersistFacade.getDatabaseRuleService(), never()).persist(eq("foo_db"), any());
    }
    
    @Test
    void assertAlterRuleConfiguration() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        metaDataManagerPersistService.alterRuleConfiguration(database, ruleConfig);
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist("foo_db", Collections.singleton(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabase(eq("foo_db"), any(), any());
    }
    
    @Test
    void assertRemoveNullRuleConfigurationItem() {
        metaDataManagerPersistService.removeRuleConfigurationItem(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                null);
        verify(metaDataPersistFacade.getDatabaseRuleService(), never()).delete(eq("foo_db"), anyCollection());
    }
    
    @Test
    void assertRemoveRuleConfigurationItem() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singleton("ds_0.t_order"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        SingleRule singleRule = mock(SingleRule.class);
        when(singleRule.getConfiguration()).thenReturn(new SingleRuleConfiguration());
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(singleRule)));
        metaDataManagerPersistService.removeRuleConfigurationItem(database, ruleConfig);
        verify(metaDataPersistFacade.getDatabaseRuleService()).delete("foo_db", Collections.singleton(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistAlteredTables(eq("foo_db"), any(), argThat(actual -> 1 == actual.size() && actual.contains("t_order")));
    }
    
    @Test
    void assertRemoveRuleConfiguration() {
        metaDataManagerPersistService.removeRuleConfiguration(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                mock(RuleConfiguration.class), "fixtureRule");
        verify(metaDataPersistFacade.getDatabaseRuleService()).delete("foo_db", "fixtureRule");
    }
    
    @Test
    void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataPersistFacade.getGlobalRuleService()).persist(Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertAlterProperties() {
        Properties props = new Properties();
        metaDataManagerPersistService.alterProperties(props);
        verify(metaDataPersistFacade.getPropsService()).persist(props);
    }
    
    @Test
    void assertCreateTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        metaDataManagerPersistService.createTable(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())), "foo_schema",
                table);
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Collections.singleton(table));
    }
    
    @Test
    void assertDropTables() {
        metaDataManagerPersistService.dropTables(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(),
                Collections.emptyList(), new ConfigurationProperties(new Properties())), "foo_schema", Collections.singleton("foo_tbl"));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getTable()).drop("foo_db", "foo_schema", "foo_tbl");
    }
}
