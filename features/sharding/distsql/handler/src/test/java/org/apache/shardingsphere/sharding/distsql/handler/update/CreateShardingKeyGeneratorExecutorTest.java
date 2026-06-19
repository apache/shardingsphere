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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateShardingKeyGeneratorExecutorTest {
    
    private final CreateShardingKeyGeneratorExecutor executor = new CreateShardingKeyGeneratorExecutor();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdateWithDuplicateRule() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        executor.setRule(mockRule(currentRuleConfig));
        CreateShardingKeyGeneratorStatement sqlStatement = new CreateShardingKeyGeneratorStatement(false, "snowflake", new AlgorithmSegment("SNOWFLAKE", new Properties()));
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfiguration() {
        executor.setRule(mockRule(new ShardingRuleConfiguration()));
        CreateShardingKeyGeneratorStatement sqlStatement = new CreateShardingKeyGeneratorStatement(false, "snowflake", new AlgorithmSegment("SNOWFLAKE", new Properties()));
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(actual.getKeyGenerators().get("snowflake").getType(), is("SNOWFLAKE"));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfigurationWithIfNotExists() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        executor.setRule(mockRule(currentRuleConfig));
        CreateShardingKeyGeneratorStatement sqlStatement = new CreateShardingKeyGeneratorStatement(true, "snowflake", new AlgorithmSegment("SNOWFLAKE", new Properties()));
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertTrue(actual.getKeyGenerators().isEmpty());
    }
    
    private ShardingRule mockRule(final ShardingRuleConfiguration ruleConfig) {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}
