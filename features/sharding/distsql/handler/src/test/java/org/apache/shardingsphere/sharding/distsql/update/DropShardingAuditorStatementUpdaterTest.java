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

package org.apache.shardingsphere.sharding.distsql.update;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.DropShardingAuditorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAuditorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingAuditorStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final DropShardingAuditorStatementUpdater updater = new DropShardingAuditorStatementUpdater();
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertExecuteWithNotExist() {
        updater.checkSQLStatement(database, createSQLStatement("sharding_key_required_auditor"), new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertExecuteWithNotExistWithIfExists() {
        DropShardingAuditorStatement sqlStatement = new DropShardingAuditorStatement(true, Collections.singletonList("sharding_key_required_auditor"));
        updater.checkSQLStatement(database, sqlStatement, new ShardingRuleConfiguration());
    }
    
    @Test
    public void assertDropSpecifiedAuditor() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        updater.updateCurrentRuleConfiguration(createSQLStatement("sharding_key_required_auditor"), currentRuleConfig);
        assertTrue(currentRuleConfig.getAuditors().isEmpty());
    }
    
    @Test(expected = AlgorithmInUsedException.class)
    public void assertExecuteWithUsed() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", null));
        currentRuleConfig.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        updater.checkSQLStatement(database, createSQLStatement("sharding_key_required_auditor"), currentRuleConfig);
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table", null);
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("sharding_key_required_auditor"), true));
        return result;
    }
    
    private DropShardingAuditorStatement createSQLStatement(final String... auditorNames) {
        return new DropShardingAuditorStatement(false, Arrays.asList(auditorNames));
    }
}
