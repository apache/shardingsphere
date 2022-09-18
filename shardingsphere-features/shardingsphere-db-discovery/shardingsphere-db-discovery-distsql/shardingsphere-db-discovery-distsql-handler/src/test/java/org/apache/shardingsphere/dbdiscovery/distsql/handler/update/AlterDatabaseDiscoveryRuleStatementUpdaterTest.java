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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterDatabaseDiscoveryRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResource resource;
    
    private final AlterDatabaseDiscoveryRuleStatementUpdater updater = new AlterDatabaseDiscoveryRuleStatementUpdater();
    
    @Before
    public void before() {
        when(database.getResource()).thenReturn(resource);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.emptyList()), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredDatabaseDiscoveryRule() throws DistSQLException {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "", "");
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singletonList(segment)),
                new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() throws DistSQLException {
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("ds0"));
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "readwrite_ds_mgr", "readwrite_ds_heartbeat");
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "TEST");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)),
                Collections.singletonMap("readwrite_ds_mgr", null), Collections.singletonMap("readwrite_ds_heartbeat", null));
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singletonList(segment)), ruleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() throws DistSQLException {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("INVALID_TYPE", new Properties());
        DatabaseDiscoveryDefinitionSegment segment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "TEST");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)),
                Collections.singletonMap("readwrite_ds_mgr", null), Collections.singletonMap("readwrite_ds_heartbeat", null));
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), ruleConfig);
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryTypeName() throws DistSQLException {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "not_exist_discovery_type_name", "");
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "TEST");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)),
                Collections.singletonMap("readwrite_ds_mgr", null), Collections.singletonMap("readwrite_ds_heartbeat", null));
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), ruleConfig);
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryHeartbeatName() throws DistSQLException {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment(
                "readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), "discovery_type_name", "not_exist_heartbeat_name");
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "TEST");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)),
                Collections.singletonMap("readwrite_ds_mgr", null), Collections.singletonMap("readwrite_ds_heartbeat", null));
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), ruleConfig);
    }
    
    @Test
    public void assertBuild() {
        DatabaseDiscoveryConstructionSegment constructionSegment = new DatabaseDiscoveryConstructionSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"),
                "discovery_type_name", "heartbeat_name");
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_2", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration =
                (DatabaseDiscoveryRuleConfiguration) updater.buildToBeAlteredRuleConfiguration(new AlterDatabaseDiscoveryRuleStatement(Arrays.asList(constructionSegment, definitionSegment)));
        assertThat(databaseDiscoveryRuleConfiguration.getDataSources().size(), is(2));
        assertTrue(databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_2")));
        assertTrue(databaseDiscoveryRuleConfiguration.getDiscoveryTypes().containsKey("readwrite_ds_2_MySQL.MGR"));
        assertTrue(databaseDiscoveryRuleConfiguration.getDiscoveryHeartbeats().containsKey("readwrite_ds_2_heartbeat"));
    }
    
    @Test
    public void assertUpdate() {
        DatabaseDiscoveryConstructionSegment constructionSegment = new DatabaseDiscoveryConstructionSegment(
                "readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), "readwrite_ds_2_mgr", "heartbeat_name");
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_2", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig =
                (DatabaseDiscoveryRuleConfiguration) updater.buildToBeAlteredRuleConfiguration(new AlterDatabaseDiscoveryRuleStatement(Arrays.asList(constructionSegment, definitionSegment)));
        DatabaseDiscoveryDataSourceRuleConfiguration constructionRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds_1", Collections.emptyList(), "", "");
        DatabaseDiscoveryDataSourceRuleConfiguration definitionRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds_2", Collections.emptyList(), "", "");
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Arrays.asList(constructionRuleConfig, definitionRuleConfig)),
                new HashMap<>(Collections.singletonMap("discovery_type_name", null)), new HashMap<>(Collections.singletonMap("heartbeat_name", null)));
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
