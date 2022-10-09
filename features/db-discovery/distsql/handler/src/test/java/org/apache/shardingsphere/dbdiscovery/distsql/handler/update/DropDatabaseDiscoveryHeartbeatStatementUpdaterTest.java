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
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class DropDatabaseDiscoveryHeartbeatStatementUpdaterTest {
    
    private final DropDatabaseDiscoveryHeartbeatStatementUpdater updater = new DropDatabaseDiscoveryHeartbeatStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentHeartbeat() {
        updater.checkSQLStatement(database, createSQLStatement(), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedHeartbeat() {
        updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RuleInUsedException.class)
    public void assertCheckSQLStatementWithInUsed() {
        DatabaseDiscoveryDataSourceRuleConfiguration ruleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("name", Collections.emptyList(), "heartbeat_name", "");
        updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.singletonList(ruleConfig),
                Collections.singletonMap("heartbeat_name", null), Collections.emptyMap()));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig);
        assertFalse(ruleConfig.getDiscoveryHeartbeats().containsKey("heartbeat_name"));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithIfExists() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryHeartbeatStatement dropDatabaseDiscoveryHeartbeatStatement = createSQLStatementWithIfExists();
        updater.checkSQLStatement(database, dropDatabaseDiscoveryHeartbeatStatement, ruleConfig);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryHeartbeatStatement, ruleConfig));
        assertTrue(ruleConfig.getDiscoveryHeartbeats().containsKey("heartbeat_name"));
        assertThat(ruleConfig.getDiscoveryHeartbeats().size(), is(2));
    }
    
    private DropDatabaseDiscoveryHeartbeatStatement createSQLStatement() {
        return new DropDatabaseDiscoveryHeartbeatStatement(false, Collections.singleton("heartbeat_name"));
    }
    
    private DropDatabaseDiscoveryHeartbeatStatement createSQLStatementWithIfExists() {
        return new DropDatabaseDiscoveryHeartbeatStatement(true, Collections.singleton("heartbeat_name_0"));
    }
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeat = new HashMap<>(2, 1);
        discoveryHeartbeat.put("heartbeat_name", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        discoveryHeartbeat.put("other", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), discoveryHeartbeat, Collections.emptyMap());
    }
}
