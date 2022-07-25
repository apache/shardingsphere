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

package org.apache.shardingsphere.mode;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.PropertiesPersistService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataContextsFactoryTest {

 @Rule
 public ExpectedException expectedException = ExpectedException.none();
 @Mock
 private InstanceContext mockInstanceContext;
 @Mock
 private ComputeNodeInstance mockComputeNodeInstance;
 @Mock
 private JDBCInstanceMetaData mockJDBCInstanceMetadata;
 @Mock
 private Map<String, DatabaseConfiguration> mockDatabaseConfigs;
 @Mock
 private MetaDataPersistService mockMetadataPersistService;
 @Mock
 private Map<String, DataSource> mockEffectiveDataSources;
 @Mock
 private DatabaseRulePersistService mockDatabaseRulePersistService;
 @Mock
 private Collection<RuleConfiguration> mockDatabaseRuleConfigs;
 @Mock
 private GlobalRulePersistService mockGlobalRulePersistService;
 @Mock
 private PropertiesPersistService mockPropertiesPersistService;
 @Mock
 private Properties mockProperties;
 @Mock
 private Map<String, ShardingSphereDatabase> mockDatabases;
 @Mock
 private ProxyInstanceMetaData mockProxyInstanceMetadata;
 @Mock
 private DatabaseMetaDataPersistService mockDatabaseMetaDataPersistService;
 private Set<String> mockDatabaseNames;
 private Collection<ShardingSphereRule> mockShardingSphereRules;

 @Before
 public void setup() {
  mockDatabaseNames = new HashSet<>();
  mockDatabaseNames.add("testdb");
  mockShardingSphereRules = new HashSet<>();

  Mockito.when(mockInstanceContext.getInstance()).thenReturn(mockComputeNodeInstance);
  Mockito.when(mockMetadataPersistService.getEffectiveDataSources(Mockito.anyString(), Mockito.anyMap())).thenReturn(mockEffectiveDataSources);
  Mockito.when(mockMetadataPersistService.getDatabaseRulePersistService()).thenReturn(mockDatabaseRulePersistService);
  Mockito.when(mockDatabaseRulePersistService.load(Mockito.anyString())).thenReturn(mockDatabaseRuleConfigs);
  Mockito.when(mockGlobalRulePersistService.load()).thenReturn(mockDatabaseRuleConfigs);
  Mockito.when(mockMetadataPersistService.getPropsService()).thenReturn(mockPropertiesPersistService);
  Mockito.when(mockPropertiesPersistService.load()).thenReturn(mockProperties);
  Mockito.when(mockProperties.getOrDefault(Mockito.any(), Mockito.any())).thenReturn("123");
 }

 @Test
 public void createFactorySuccessfullyTestWithJDBCInstanceMetadata() {

  try (
          MockedStatic<ShardingSphereDatabasesFactory> mockedShardingSphereDatabasesFactory = mockStatic(ShardingSphereDatabasesFactory.class);
          MockedStatic<GlobalRulesBuilder> mockedGlobalRulesBuilder = mockStatic(GlobalRulesBuilder.class);
          MockedConstruction<ShardingSphereRuleMetaData> ignored = mockConstruction(ShardingSphereRuleMetaData.class)) {

   Mockito.when(mockComputeNodeInstance.getMetaData()).thenReturn(mockJDBCInstanceMetadata);
   Mockito.when(mockDatabaseConfigs.keySet()).thenReturn(mockDatabaseNames);
   Mockito.when(mockMetadataPersistService.getGlobalRuleService()).thenReturn(mockGlobalRulePersistService);
   mockedShardingSphereDatabasesFactory.when(() -> ShardingSphereDatabasesFactory
                   .create(Mockito.anyMap(), Mockito.any(ConfigurationProperties.class), Mockito.any(InstanceContext.class)))
           .thenReturn(mockDatabases);
   mockedGlobalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(Mockito.anyCollection(), Mockito.anyMap(), Mockito.any(InstanceContext.class))).thenReturn(mockShardingSphereRules);

   MetaDataContexts actualResponse = MetaDataContextsFactory.create(mockMetadataPersistService, mockDatabaseConfigs, mockInstanceContext);
   Assert.assertNotNull(actualResponse);
  } catch (SQLException e) {
   Assert.fail();
  }
 }

 @Test
 public void createFactorySuccessfullyTestWithoutJDBCInstanceMetadata() {

  try (
          MockedStatic<ShardingSphereDatabasesFactory> mockedShardingSphereDatabasesFactory = mockStatic(ShardingSphereDatabasesFactory.class);
          MockedStatic<GlobalRulesBuilder> mockedGlobalRulesBuilder = mockStatic(GlobalRulesBuilder.class);
          MockedConstruction<ShardingSphereRuleMetaData> mockedConstruction = mockConstruction(ShardingSphereRuleMetaData.class)) {

   Mockito.when(mockComputeNodeInstance.getMetaData()).thenReturn(mockProxyInstanceMetadata);
   Mockito.when(mockMetadataPersistService.getDatabaseMetaDataService()).thenReturn(mockDatabaseMetaDataPersistService);
   Mockito.when(mockDatabaseMetaDataPersistService.loadAllDatabaseNames()).thenReturn(mockDatabaseNames);
   Mockito.when(mockMetadataPersistService.getGlobalRuleService()).thenReturn(mockGlobalRulePersistService);
   mockedShardingSphereDatabasesFactory.when(() -> ShardingSphereDatabasesFactory
                   .create(Mockito.anyMap(), Mockito.any(ConfigurationProperties.class), Mockito.any(InstanceContext.class)))
           .thenReturn(mockDatabases);
   mockedGlobalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(Mockito.anyCollection(), Mockito.anyMap(), Mockito.any(InstanceContext.class))).thenReturn(mockShardingSphereRules);

   MetaDataContexts actualResponse = MetaDataContextsFactory.create(mockMetadataPersistService, mockDatabaseConfigs, mockInstanceContext);
   Assert.assertNotNull(actualResponse);
  } catch (SQLException e) {
   Assert.fail();
  }
 }

 @Test
 public void createFactoryFailureWithNoSqlParserRuleInstance() {

  expectedException.expect(IllegalStateException.class);
  expectedException.expectMessage("Rule `SQLParserRule` should have and only have one instance.");

  try (
          MockedStatic<ShardingSphereDatabasesFactory> mockedShardingSphereDatabasesFactory = mockStatic(ShardingSphereDatabasesFactory.class);
          MockedStatic<GlobalRulesBuilder> mockedGlobalRulesBuilder = mockStatic(GlobalRulesBuilder.class)) {
   Mockito.when(mockComputeNodeInstance.getMetaData()).thenReturn(mockJDBCInstanceMetadata);
   Mockito.when(mockDatabaseConfigs.keySet()).thenReturn(mockDatabaseNames);
   Mockito.when(mockMetadataPersistService.getGlobalRuleService())
           .thenReturn(mockGlobalRulePersistService);
   mockedShardingSphereDatabasesFactory.when(() -> ShardingSphereDatabasesFactory
                   .create(Mockito.anyMap(), Mockito.any(ConfigurationProperties.class), Mockito.any(InstanceContext.class)))
           .thenReturn(mockDatabases);
   mockedGlobalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(Mockito.anyCollection(), Mockito.anyMap(), Mockito.any(InstanceContext.class))).thenReturn(mockShardingSphereRules);

   MetaDataContexts actualResponse = MetaDataContextsFactory.create(mockMetadataPersistService, mockDatabaseConfigs, mockInstanceContext);
   Assert.assertNotNull(actualResponse);
  } catch (SQLException e) {
   Assert.fail();
  }
 }
}
