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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingAuditorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAuditorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingAuditorStatementUpdaterTest {
    
    private final AlterShardingAuditorStatementUpdater updater = new AlterShardingAuditorStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicate() {
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("input_auditor_name", new AlgorithmSegment("DML_SHARDING_CONDITIONS", createProperties()));
        updater.checkSQLStatement(database, new AlterShardingAuditorStatement(Arrays.asList(auditorSegment, auditorSegment)), null);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertExecuteWithNotExist() {
        Properties props = createProperties();
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("not_exist_auditor_name", new AlgorithmSegment("DML_SHARDING_CONDITIONS", props));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getAuditors().put("exist_auditor_name", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", props));
        updater.checkSQLStatement(database, new AlterShardingAuditorStatement(Collections.singletonList(auditorSegment)), ruleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() {
        Properties props = createProperties();
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("exist_auditor_name", new AlgorithmSegment("INVALID_TYPE", props));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getAuditors().put("exist_auditor_name", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", props));
        updater.checkSQLStatement(database, new AlterShardingAuditorStatement(Collections.singletonList(auditorSegment)), ruleConfig);
    }
    
    @Test
    public void assertUpdate() {
        ShardingAuditorSegment auditorSegment = new ShardingAuditorSegment("exist_auditor_name", new AlgorithmSegment("DML_SHARDING_CONDITIONS", createProperties()));
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getAuditors().put("exist_auditor_name", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", createProperties()));
        AlterShardingAuditorStatement statement = new AlterShardingAuditorStatement(Collections.singletonList(auditorSegment));
        ShardingRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        assertThat(currentRuleConfig.getAuditors().get("exist_auditor_name").getType(), is("DML_SHARDING_CONDITIONS"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("key", "value");
        return result;
    }
}
