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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
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
public final class DropDatabaseDiscoveryProviderAlgorithmStatementUpdaterTest {
    
    private final DropDatabaseDiscoveryTypeStatementUpdater updater = new DropDatabaseDiscoveryTypeStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentType() {
        updater.checkSQLStatement(database, createSQLStatement(), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedTypes() {
        updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RuleInUsedException.class)
    public void assertCheckSQLStatementWithInUsed() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("name", Collections.emptyList(), "", "type_name");
        updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.singletonList(dataSourceRuleConfig),
                Collections.emptyMap(), Collections.singletonMap("type_name", null)));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig);
        assertFalse(ruleConfig.getDiscoveryTypes().containsKey("type_name"));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithIfExists() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryTypeStatement dropDatabaseDiscoveryRuleStatement = createSQLStatementWithIfExists();
        updater.checkSQLStatement(database, dropDatabaseDiscoveryRuleStatement, ruleConfig);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryRuleStatement, ruleConfig));
        assertTrue(ruleConfig.getDiscoveryTypes().containsKey("type_name"));
        assertThat(ruleConfig.getDiscoveryTypes().size(), is(2));
    }
    
    private DropDatabaseDiscoveryTypeStatement createSQLStatement() {
        return new DropDatabaseDiscoveryTypeStatement(false, Collections.singleton("type_name"));
    }
    
    private DropDatabaseDiscoveryTypeStatement createSQLStatementWithIfExists() {
        return new DropDatabaseDiscoveryTypeStatement(true, Collections.singleton("type_name_0"));
    }
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        Map<String, AlgorithmConfiguration> discoveryTypes = new HashMap<>(2, 1);
        discoveryTypes.put("type_name", new AlgorithmConfiguration("MySQL.MGR", new Properties()));
        discoveryTypes.put("other", new AlgorithmConfiguration("MySQL.MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), discoveryTypes);
    }
}
