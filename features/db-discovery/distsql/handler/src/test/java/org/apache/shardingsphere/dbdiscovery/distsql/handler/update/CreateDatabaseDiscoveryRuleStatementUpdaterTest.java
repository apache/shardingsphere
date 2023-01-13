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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
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
        Properties props = new Properties();
        DatabaseDiscoveryDefinitionSegment databaseDiscoveryDefinitionSegment =
                new DatabaseDiscoveryDefinitionSegment("readwrite_ds", Collections.emptyList(), new AlgorithmSegment("MySQL.MGR", props), props);
        updater.checkSQLStatement(database, createSQLStatement(false, Collections.singletonList(databaseDiscoveryDefinitionSegment)),
                new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertCheckSQLStatementWithoutExistedResources() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.singleton("ds_read_0"));
        Properties props = new Properties();
        DatabaseDiscoveryDefinitionSegment segment =
                new DatabaseDiscoveryDefinitionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), new AlgorithmSegment("MySQL.MGR", props), props);
        updater.checkSQLStatement(database, createSQLStatement(false, Collections.singleton(segment)), null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("INVALID_TYPE", new Properties());
        DatabaseDiscoveryDefinitionSegment segment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        updater.checkSQLStatement(database, createSQLStatement(false, Collections.singleton(segment)), null);
    }
    
    @Test
    public void assertBuild() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration ruleConfig =
                updater.buildToBeCreatedRuleConfiguration(null, createSQLStatement(false, Collections.singletonList(definitionSegment)));
        assertThat(ruleConfig.getDataSources().size(), is(1));
        assertTrue(ruleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(ruleConfig.getDiscoveryTypes().containsKey("readwrite_ds_1_mysql_mgr"));
        assertTrue(ruleConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_1_heartbeat"));
    }
    
    @Test
    public void assertUpdate() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegment = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentConfig,
                createSQLStatement(false, Collections.singletonList(definitionSegment)));
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertThat(currentConfig.getDataSources().size(), is(1));
        assertTrue(currentConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(currentConfig.getDiscoveryTypes().containsKey("readwrite_ds_1_mysql_mgr"));
        assertTrue(currentConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_1_heartbeat"));
    }
    
    @Test
    public void assertUpdateWithIfNotExists() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegmentDS1 = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_1",
                Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryDefinitionSegment definitionSegmentDS2 = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_2",
                Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        Collection<AbstractDatabaseDiscoverySegment> currentSegments = new LinkedList<>();
        currentSegments.add(definitionSegmentDS1);
        currentSegments.add(definitionSegmentDS2);
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentConfig, createSQLStatement(false, currentSegments));
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        definitionSegmentDS1 = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1", "ds_read_3"), algorithmSegment, new Properties());
        definitionSegmentDS2 = new DatabaseDiscoveryDefinitionSegment("readwrite_ds_2", Arrays.asList("ds_read_0", "ds_read_1", "ds_read_3"), algorithmSegment, new Properties());
        Collection<AbstractDatabaseDiscoverySegment> toBeCreatedSegments = new LinkedList<>();
        toBeCreatedSegments.add(definitionSegmentDS1);
        toBeCreatedSegments.add(definitionSegmentDS2);
        CreateDatabaseDiscoveryRuleStatement statement = createSQLStatement(true, toBeCreatedSegments);
        updater.checkSQLStatement(database, statement, currentConfig);
        toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentConfig, statement);
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertThat(currentConfig.getDataSources().size(), is(2));
        assertTrue(currentConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(currentConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_2")));
        assertTrue(currentConfig.getDiscoveryTypes().containsKey("readwrite_ds_1_mysql_mgr"));
        assertTrue(currentConfig.getDiscoveryTypes().containsKey("readwrite_ds_2_mysql_mgr"));
        assertTrue(currentConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_1_heartbeat"));
        assertTrue(currentConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_2_heartbeat"));
        Collection<String> dataSources = new LinkedList<>();
        currentConfig.getDataSources().forEach(each -> dataSources.addAll(each.getDataSourceNames()));
        assertThat(dataSources.size(), is(4));
        assertTrue(dataSources.contains("ds_read_0"));
        assertTrue(dataSources.contains("ds_read_1"));
        assertFalse(dataSources.contains("ds_read_3"));
    }
    
    private CreateDatabaseDiscoveryRuleStatement createSQLStatement(final boolean ifNotExists, final Collection<AbstractDatabaseDiscoverySegment> segments) {
        return new CreateDatabaseDiscoveryRuleStatement(ifNotExists, segments);
    }
}
