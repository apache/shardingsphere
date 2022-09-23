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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateAlgorithmException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingKeyGeneratorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingKeyGeneratorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingKeyGeneratorStatementUpdaterTest {
    
    private final CreateShardingKeyGeneratorStatementUpdater updater = new CreateShardingKeyGeneratorStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateAlgorithmException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("uuid_key_generator", new AlgorithmSegment("uuid", new Properties()));
        updater.checkSQLStatement(database, createSQLStatement(keyGeneratorSegment, keyGeneratorSegment), null);
    }
    
    @Test(expected = DuplicateAlgorithmException.class)
    public void assertExecuteWithExist() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("uuid_key_generator", new AlgorithmSegment("uuid", new Properties()));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getKeyGenerators().put("uuid_key_generator", new AlgorithmConfiguration("uuid", new Properties()));
        updater.checkSQLStatement(database, createSQLStatement(keyGeneratorSegment), ruleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = new ShardingKeyGeneratorSegment("invalid_key_generator", new AlgorithmSegment("INVALID_ALGORITHM", new Properties()));
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        updater.checkSQLStatement(database, createSQLStatement(keyGeneratorSegment), ruleConfig);
    }
    
    private CreateShardingKeyGeneratorStatement createSQLStatement(final ShardingKeyGeneratorSegment... ruleSegments) {
        return new CreateShardingKeyGeneratorStatement(Arrays.asList(ruleSegments));
    }
}
