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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateDatabaseDiscoveryRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    private final CreateDatabaseDiscoveryRuleStatementUpdater updater = new CreateDatabaseDiscoveryRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateRuleNames() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("pr_ds", Collections.emptyList(), "ha-heartbeat", "test");
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("pr_ds", Collections.emptyList(), "", "");
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() throws DistSQLException {
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("ds_read_0"));
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("pr_ds", Arrays.asList("ds_read_0", "ds_read_1"), "", "");
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() throws DistSQLException {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("INVALID_TYPE", new Properties());
        DatabaseDiscoveryDefinitionSegment segment = new DatabaseDiscoveryDefinitionSegment("pr_ds", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), null);
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryTypeName() throws DistSQLException {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("pr_ds", Arrays.asList("ds_read_0", "ds_read_1"), "not_exist_discovery_type_name", "");
        DatabaseDiscoveryRuleConfiguration configuration = new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), configuration);
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryHeartbeatName() throws DistSQLException {
        DatabaseDiscoveryConstructionSegment segment = new DatabaseDiscoveryConstructionSegment("pr_ds", Arrays.asList("ds_read_0", "ds_read_1"), "discovery_type_name", "not_exist_heartbeat_name");
        DatabaseDiscoveryRuleConfiguration configuration = new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), 
                Collections.singletonMap("discovery_type_name", null));
        updater.checkSQLStatement(shardingSphereMetaData, new CreateDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), configuration);
    }
    
    @Test
    public void assertBuild() {
        DatabaseDiscoveryConstructionSegment constructionSegment = new DatabaseDiscoveryConstructionSegment("pr_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), 
                "discovery_type_name", "heartbeat_name");
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("mgr", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("pr_ds_2", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration 
                = updater.buildToBeCreatedRuleConfiguration(new CreateDatabaseDiscoveryRuleStatement(Arrays.asList(constructionSegment, definitionSegment)));
        assertThat(databaseDiscoveryRuleConfiguration.getDataSources().size(), is(2));
        assertTrue(databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("pr_ds_1")));
        assertTrue(databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("pr_ds_2")));
        assertTrue(databaseDiscoveryRuleConfiguration.getDiscoveryTypes().containsKey("pr_ds_2_mgr"));
        assertTrue(databaseDiscoveryRuleConfiguration.getDiscoveryHeartbeats().containsKey("pr_ds_2_heartbeat"));
    }
    
    @Test
    public void assertUpdate() {
        DatabaseDiscoveryConstructionSegment constructionSegment = new DatabaseDiscoveryConstructionSegment("pr_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), 
                "discovery_type_name", "heartbeat_name");
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("mgr", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("pr_ds_2", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig 
                = updater.buildToBeCreatedRuleConfiguration(new CreateDatabaseDiscoveryRuleStatement(Arrays.asList(constructionSegment, definitionSegment)));
        DatabaseDiscoveryRuleConfiguration currentConfiguration = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        updater.updateCurrentRuleConfiguration(currentConfiguration, toBeCreatedRuleConfig);
        assertThat(currentConfiguration.getDataSources().size(), is(2));
        assertTrue(currentConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("pr_ds_1")));
        assertTrue(currentConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("pr_ds_2")));
        assertTrue(currentConfiguration.getDiscoveryTypes().containsKey("pr_ds_2_mgr"));
        assertTrue(currentConfiguration.getDiscoveryHeartbeats().containsKey("pr_ds_2_heartbeat"));
    }
}
