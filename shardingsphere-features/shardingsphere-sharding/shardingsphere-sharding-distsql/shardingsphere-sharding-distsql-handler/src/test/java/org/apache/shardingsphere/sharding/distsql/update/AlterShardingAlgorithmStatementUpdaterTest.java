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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingAlgorithmStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAlgorithmStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingAlgorithmStatementUpdaterTest {
    
    private final AlterShardingAlgorithmStatementUpdater updater = new AlterShardingAlgorithmStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        Properties props = new Properties();
        props.put("input_key", "input_value");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("input_algorithm_name", new AlgorithmSegment("input_algorithm_name", props));
        updater.checkSQLStatement(database, createSQLStatement(algorithmSegment, algorithmSegment), null);
    }
    
    @Test(expected = RequiredAlgorithmMissedException.class)
    public void assertExecuteWithNotExist() throws DistSQLException {
        Properties props = new Properties();
        props.put("input_key", "input_value");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("not_exist_algorithm_name", new AlgorithmSegment("input_algorithm_name", props));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getShardingAlgorithms().put("exist_algorithm_name", new AlgorithmConfiguration("hash_mod", props));
        updater.checkSQLStatement(database, createSQLStatement(algorithmSegment), shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() throws DistSQLException {
        Properties props = new Properties();
        props.put("input_key", "input_value");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("exist_algorithm_name", new AlgorithmSegment("input_algorithm_name", props));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getShardingAlgorithms().put("exist_algorithm_name", new AlgorithmConfiguration("invalid_algorithm", props));
        updater.checkSQLStatement(database, createSQLStatement(algorithmSegment), ruleConfig);
    }
    
    @Test
    public void assertUpdate() {
        Properties props = new Properties();
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("exist_algorithm_name", new AlgorithmSegment("mod", props));
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getShardingAlgorithms().put("exist_algorithm_name", new AlgorithmConfiguration("hash_mod", props));
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(createSQLStatement(algorithmSegment));
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfiguration);
        assertThat(currentRuleConfig.getShardingAlgorithms().get("exist_algorithm_name").getType(), is("mod"));
    }
    
    private AlterShardingAlgorithmStatement createSQLStatement(final ShardingAlgorithmSegment... ruleSegments) {
        return new AlterShardingAlgorithmStatement(Arrays.asList(ruleSegments));
    }
}
