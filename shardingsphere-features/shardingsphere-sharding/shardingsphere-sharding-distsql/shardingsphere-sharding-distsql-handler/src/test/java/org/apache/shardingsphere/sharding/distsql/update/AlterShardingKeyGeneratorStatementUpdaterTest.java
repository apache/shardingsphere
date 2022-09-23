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
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingKeyGeneratorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingKeyGeneratorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingKeyGeneratorStatementUpdaterTest {
    
    private final AlterShardingKeyGeneratorStatementUpdater updater = new AlterShardingKeyGeneratorStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("input_key_generator_name", new AlgorithmSegment("snowflake", createProperties()));
        updater.checkSQLStatement(database, new AlterShardingKeyGeneratorStatement(Arrays.asList(keyGeneratorSegment, keyGeneratorSegment)), null);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertExecuteWithNotExist() throws DistSQLException {
        Properties props = createProperties();
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("not_exist_key_generator_name", new AlgorithmSegment("snowflake", props));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getKeyGenerators().put("exist_key_generator_name", new AlgorithmConfiguration("hash_mod", props));
        updater.checkSQLStatement(database, new AlterShardingKeyGeneratorStatement(Collections.singletonList(keyGeneratorSegment)), ruleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() throws DistSQLException {
        Properties props = createProperties();
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("exist_key_generator_name", new AlgorithmSegment("INVALID_TYPE", props));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getKeyGenerators().put("exist_key_generator_name", new AlgorithmConfiguration("UUID", props));
        updater.checkSQLStatement(database, new AlterShardingKeyGeneratorStatement(Collections.singletonList(keyGeneratorSegment)), ruleConfig);
    }
    
    @Test
    public void assertUpdate() {
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("exist_key_generator_name", new AlgorithmSegment("snowflake", createProperties()));
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getKeyGenerators().put("exist_key_generator_name", new AlgorithmConfiguration("uuid", createProperties()));
        AlterShardingKeyGeneratorStatement statement = new AlterShardingKeyGeneratorStatement(Collections.singletonList(keyGeneratorSegment));
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfiguration);
        assertThat(currentRuleConfig.getKeyGenerators().get("exist_key_generator_name").getType(), is("uuid"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("key", "value");
        return result;
    }
}
