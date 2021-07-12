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
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
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

public final class DropDatabaseDiscoveryRuleStatementUpdaterTest {
    
    private final DropDatabaseDiscoveryRuleStatementUpdater updater = new DropDatabaseDiscoveryRuleStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), null, mock(ShardingSphereResource.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedRules() throws RuleDefinitionViolationException {
        updater.checkSQLStatement("foo", createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap()), mock(ShardingSphereResource.class));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = createCurrentRuleConfiguration();
        assertTrue(updater.updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfiguration));
        assertTrue(databaseDiscoveryRuleConfiguration.getDiscoveryTypes().isEmpty());
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
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "pr_ds_MGR");
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(1, 1);
        discoveryTypes.put("pr_ds_MGR", new ShardingSphereAlgorithmConfiguration("pr_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), discoveryTypes);
    }
    
    private DatabaseDiscoveryRuleConfiguration createMultipleCurrentRuleConfigurations() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "pr_ds_MGR");
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(1, 1);
        discoveryTypes.put("pr_ds_MGR", new ShardingSphereAlgorithmConfiguration("pr_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Arrays.asList(dataSourceRuleConfig, 
                new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group_another", Collections.emptyList(), "pr_ds_MGR"))), discoveryTypes);
    }
}
