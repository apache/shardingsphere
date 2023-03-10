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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class AlterDatabaseDiscoveryRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    private final AlterDatabaseDiscoveryRuleStatementUpdater updater = new AlterDatabaseDiscoveryRuleStatementUpdater();
    
    @BeforeEach
    public void before() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
    }
    
    @Test
    public void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.emptyList()), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutToBeAlteredDatabaseDiscoveryRule() {
        Properties props = new Properties();
        DatabaseDiscoveryRuleSegment segment =
                new DatabaseDiscoveryRuleSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), new AlgorithmSegment("MySQL.MGR", props), props);
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singletonList(segment)),
                new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap())));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutExistedResources() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.singleton("ds0"));
        Properties props = new Properties();
        DatabaseDiscoveryRuleSegment segment =
                new DatabaseDiscoveryRuleSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), new AlgorithmSegment("MySQL.MGR", props), props);
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "TEST");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)),
                Collections.singletonMap("readwrite_ds_mgr", null), Collections.singletonMap("readwrite_ds_heartbeat", null));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singletonList(segment)), ruleConfig));
    }
    
    @Test
    public void assertCheckSQLStatementWithDatabaseDiscoveryType() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("INVALID_TYPE", new Properties());
        DatabaseDiscoveryRuleSegment segment = new DatabaseDiscoveryRuleSegment("readwrite_ds", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds", Collections.emptyList(), "ha-heartbeat", "TEST");
        DatabaseDiscoveryRuleConfiguration ruleConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)),
                Collections.singletonMap("readwrite_ds_mgr", null), Collections.singletonMap("readwrite_ds_heartbeat", null));
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> updater.checkSQLStatement(database, new AlterDatabaseDiscoveryRuleStatement(Collections.singleton(segment)), ruleConfig));
    }
    
    @Test
    public void assertBuild() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryRuleSegment definitionSegment = new DatabaseDiscoveryRuleSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig =
                (DatabaseDiscoveryRuleConfiguration) updater.buildToBeAlteredRuleConfiguration(new AlterDatabaseDiscoveryRuleStatement(Collections.singletonList(definitionSegment)));
        assertThat(databaseDiscoveryRuleConfig.getDataSources().size(), is(1));
        assertTrue(databaseDiscoveryRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName)
                .collect(Collectors.toList()).removeAll(Collections.singletonList("readwrite_ds_1")));
        assertTrue(databaseDiscoveryRuleConfig.getDiscoveryTypes().containsKey("readwrite_ds_1_mysql_mgr"));
        assertTrue(databaseDiscoveryRuleConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_1_heartbeat"));
    }
    
    @Test
    public void assertUpdate() {
        AlgorithmSegment algorithmSegment = new AlgorithmSegment("MySQL.MGR", new Properties());
        DatabaseDiscoveryRuleSegment definitionSegment = new DatabaseDiscoveryRuleSegment("readwrite_ds_1", Arrays.asList("ds_read_0", "ds_read_1"), algorithmSegment, new Properties());
        DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig =
                (DatabaseDiscoveryRuleConfiguration) updater.buildToBeAlteredRuleConfiguration(new AlterDatabaseDiscoveryRuleStatement(Collections.singletonList(definitionSegment)));
        DatabaseDiscoveryDataSourceRuleConfiguration definitionRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("readwrite_ds_1", Collections.emptyList(), "", "");
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singletonList(definitionRuleConfig)),
                new HashMap<>(Collections.singletonMap("discovery_type_name", null)), new HashMap<>(Collections.singletonMap("heartbeat_name", null)));
        updater.updateCurrentRuleConfiguration(currentConfig, toBeCreatedRuleConfig);
        assertThat(currentConfig.getDataSources().size(), is(1));
        assertTrue(currentConfig.getDataSources().stream()
                .map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toList()).removeAll(Collections.singleton("readwrite_ds_1")));
        assertTrue(currentConfig.getDiscoveryTypes().containsKey("readwrite_ds_1_mysql_mgr"));
        assertTrue(currentConfig.getDiscoveryHeartbeats().containsKey("readwrite_ds_1_heartbeat"));
    }
}
