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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DropDatabaseDiscoveryRuleStatementUpdaterTest {
    
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final DropDatabaseDiscoveryRuleStatementUpdater updater = new DropDatabaseDiscoveryRuleStatementUpdater();
    
    @Before
    public void init() {
        shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(null, Collections.emptyList()));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedRules() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfiguration);
        assertTrue(databaseDiscoveryRuleConfiguration.getDataSources().isEmpty());
        assertThat(databaseDiscoveryRuleConfiguration.getDiscoveryTypes().size(), is(1));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithIfExists() throws DistSQLException {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryRuleStatement dropDatabaseDiscoveryRuleStatement = createSQLStatementWithIfExists();
        updater.checkSQLStatement(shardingSphereMetaData, dropDatabaseDiscoveryRuleStatement, databaseDiscoveryRuleConfiguration);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryRuleStatement, databaseDiscoveryRuleConfiguration));
        assertThat(databaseDiscoveryRuleConfiguration.getDataSources().size(), is(1));
        assertThat(databaseDiscoveryRuleConfiguration.getDiscoveryTypes().size(), is(1));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithInUsedDiscoveryType() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = createMultipleCurrentRuleConfigurations();
        assertFalse(updater.updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfiguration));
        assertThat(databaseDiscoveryRuleConfiguration.getDiscoveryTypes().size(), is(1));
    }
    
    private DropDatabaseDiscoveryRuleStatement createSQLStatement() {
        return new DropDatabaseDiscoveryRuleStatement(Collections.singleton("ha_group"));
    }
    
    private DropDatabaseDiscoveryRuleStatement createSQLStatementWithIfExists() {
        return new DropDatabaseDiscoveryRuleStatement(Collections.singleton("ha_group_0"), true);
    }
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR");
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(1, 1);
        discoveryTypes.put("readwrite_ds_MGR", new ShardingSphereAlgorithmConfiguration("readwrite_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap(), discoveryTypes);
    }
    
    private DatabaseDiscoveryRuleConfiguration createMultipleCurrentRuleConfigurations() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR");
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(1, 1);
        discoveryTypes.put("readwrite_ds_MGR", new ShardingSphereAlgorithmConfiguration("readwrite_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Arrays.asList(dataSourceRuleConfig,
                new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group_another", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR"))), Collections.emptyMap(), discoveryTypes);
    }
}
