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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDatabaseDiscoveryRuleStatementUpdaterTest {
    
    private final CreateDatabaseDiscoveryRuleStatementUpdater updater = new CreateDatabaseDiscoveryRuleStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    @Before
    public void before() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateRuleNames() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "test");
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("readwrite_ds", Collections.emptyList(), "", "");
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertCheckSQLStatementWithoutExistedResources() {
        when(resourceMetaData.getNotExistedResources(any())).thenReturn(Collections.singleton("ds_read_0"));
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "", "");
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("INVALID_TYPE", new Properties());
        DatabaseDiscoveryDefinitionSegment segment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), null);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryTypeName() {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "not_exist_discovery_type_name", "");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), ruleConfig);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryHeartbeatName() {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment(
                "readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "discovery_type_name", "not_exist_heartbeat_name");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(),
                Collections.singletonMap("discovery_type_name", null));
        updater.checkSQLStatement(database, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), ruleConfig);
    }
    
    @Test
    public void assertBuild() {
        DatabaseDiscoveryConstructionSegment constructionSegment = new DatabaseDiscoveryConstructionSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"),
                "discovery_type_name", "heartbeat_name");
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_2", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration ruleConfig =
                updater.buildToBeCreatedRuleConfiguration(new CreateDatabaseDiscoveryRuleStatement(Arrays.asList(constructionSegment, definitionSegment)));
        assertThat(ruleConfig.getDataSources().size(), is(2));
        assertTrue(ruleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(ruleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_2")));
        assertTrue(ruleConfig.getDiscoveryTypes().containsKey("readwrite_ds_2_MySQL.MGR"));
        assertTrue(ruleConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_2_heartbeat"));
    }
    
    @Test
    public void assertUpdate() {
        DatabaseDiscoveryConstructionSegment constructionSegment = new DatabaseDiscoveryConstructionSegment(
                "readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), "discovery_type_name", "heartbeat_name");
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_2", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(
                new CreateDatabaseDiscoveryRuleStatement(Arrays.asList(constructionSegment, definitionSegment)));
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertThat(currentConfig.getDataSources().size(), is(2));
        assertTrue(currentConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(currentConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_2")));
        assertTrue(currentConfig.getDiscoveryTypes().containsKey("readwrite_ds_2_MySQL.MGR"));
        assertTrue(currentConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_2_heartbeat"));
    }
}
