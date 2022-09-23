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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryHeartbeatSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class AlterDatabaseDiscoveryHeartbeatStatementUpdaterTest {
    
    private final AlterDatabaseDiscoveryHeartbeatStatementUpdater updater = new AlterDatabaseDiscoveryHeartbeatStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateHeartbeatNames() throws DistSQLException {
        DatabaseDiscoveryHeartbeatSegment segment1 = new DatabaseDiscoveryHeartbeatSegment("heartbeat", createProperties("key", "value"));
        DatabaseDiscoveryHeartbeatSegment segment2 = new DatabaseDiscoveryHeartbeatSegment("heartbeat", createProperties("key", "value"));
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryHeartbeatStatement(Arrays.asList(segment1, segment2)),
                new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithNotExistDiscoveryHeartbeatName() throws DistSQLException {
        DatabaseDiscoveryHeartbeatSegment segment = new DatabaseDiscoveryHeartbeatSegment("heartbeat", createProperties("key", "value"));
        DatabaseDiscoveryRuleConfiguration config = new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.singletonMap("heartbeat1", null), Collections.emptyMap());
        updater.checkSQLStatement(database, new AlterDatabaseDiscoveryHeartbeatStatement(Collections.singleton(segment)), config);
    }
    
    @Test
    public void assertUpdate() {
        DatabaseDiscoveryHeartbeatSegment segment1 = new DatabaseDiscoveryHeartbeatSegment("heartbeat_1", createProperties("key_1", "value_1"));
        DatabaseDiscoveryHeartbeatSegment segment2 = new DatabaseDiscoveryHeartbeatSegment("heartbeat_2", createProperties("key_2", "value_2"));
        DatabaseDiscoveryRuleConfiguration ruleConfig = updater.buildToBeAlteredRuleConfiguration(new AlterDatabaseDiscoveryHeartbeatStatement(Arrays.asList(segment1, segment2)));
        DatabaseDiscoveryRuleConfiguration currentConfig = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        updater.updateCurrentRuleConfiguration(currentConfig, ruleConfig);
        assertThat(currentConfig.getDiscoveryHeartbeats().size(), is(2));
        assertThat(currentConfig.getDiscoveryHeartbeats().get("heartbeat_1").getProps(), is(createProperties("key_1", "value_1")));
        assertThat(currentConfig.getDiscoveryHeartbeats().get("heartbeat_2").getProps(), is(createProperties("key_2", "value_2")));
    }
    
    private Properties createProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
}
