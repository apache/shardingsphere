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
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropDatabaseDiscoveryHeartbeatStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final DropDatabaseDiscoveryHeartbeatStatementUpdater updater = new DropDatabaseDiscoveryHeartbeatStatementUpdater();
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentHeartbeat() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedHeartbeat() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RuleInUsedException.class)
    public void assertCheckSQLStatementWithInUsed() throws DistSQLException {
        DatabaseDiscoveryDataSourceRuleConfiguration configuration = new DatabaseDiscoveryDataSourceRuleConfiguration("name", Collections.emptyList(), "heartbeat_name", "");
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.singletonList(configuration),
                Collections.singletonMap("heartbeat_name", null), Collections.emptyMap()));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfiguration);
        assertFalse(databaseDiscoveryRuleConfiguration.getDiscoveryHeartbeats().containsKey("heartbeat_name"));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithIfExists() throws DistSQLException {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryHeartbeatStatement dropDatabaseDiscoveryHeartbeatStatement = createSQLStatementWithIfExists();
        updater.checkSQLStatement(shardingSphereMetaData, dropDatabaseDiscoveryHeartbeatStatement, databaseDiscoveryRuleConfiguration);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryHeartbeatStatement, databaseDiscoveryRuleConfiguration));
        assertTrue(databaseDiscoveryRuleConfiguration.getDiscoveryHeartbeats().containsKey("heartbeat_name"));
        assertThat(databaseDiscoveryRuleConfiguration.getDiscoveryHeartbeats().size(), is(2));
    }
    
    private DropDatabaseDiscoveryHeartbeatStatement createSQLStatement() {
        return new DropDatabaseDiscoveryHeartbeatStatement(Collections.singleton("heartbeat_name"));
    }
    
    private DropDatabaseDiscoveryHeartbeatStatement createSQLStatementWithIfExists() {
        return new DropDatabaseDiscoveryHeartbeatStatement(Collections.singleton("heartbeat_name_0"), true);
    }
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeat = new HashMap<>(2, 1);
        discoveryHeartbeat.put("heartbeat_name", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        discoveryHeartbeat.put("other", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), discoveryHeartbeat, Collections.emptyMap());
    }
}
