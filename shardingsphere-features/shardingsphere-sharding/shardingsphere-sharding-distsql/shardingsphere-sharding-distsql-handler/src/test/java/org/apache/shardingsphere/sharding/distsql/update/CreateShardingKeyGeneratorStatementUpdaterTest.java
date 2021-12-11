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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateKeyGeneratorException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingKeyGeneratorStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingKeyGeneratorStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingKeyGeneratorStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private CreateShardingKeyGeneratorStatementUpdater updater;
    
    @Before
    public void before() {
        updater = new CreateShardingKeyGeneratorStatementUpdater();
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateKeyGeneratorException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = buildShardingKeyGeneratorSegment();
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(keyGeneratorSegment, keyGeneratorSegment), null);
    }
    
    @Test(expected = DuplicateKeyGeneratorException.class)
    public void assertExecuteWithExist() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = buildShardingKeyGeneratorSegment();
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getKeyGenerators().put("uuid_key_generator", new ShardingSphereAlgorithmConfiguration("uuid", buildProps()));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(keyGeneratorSegment), shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithoutRuleConfiguration() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = buildShardingKeyGeneratorSegment();
        ShardingRuleConfiguration shardingRuleConfiguration = null;
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(keyGeneratorSegment), shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() throws DistSQLException {
        ShardingKeyGeneratorSegment keyGeneratorSegment = buildShardingKeyGeneratorSegment();
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getKeyGenerators().put("snowflake_key_generator", new ShardingSphereAlgorithmConfiguration("INVALID_ALGORITHM", buildProps()));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(keyGeneratorSegment), shardingRuleConfiguration);
    }
    
    private CreateShardingKeyGeneratorStatement createSQLStatement(final ShardingKeyGeneratorSegment... ruleSegments) {
        return new CreateShardingKeyGeneratorStatement(Arrays.asList(ruleSegments));
    }
    
    private ShardingKeyGeneratorSegment buildShardingKeyGeneratorSegment() {
        return new ShardingKeyGeneratorSegment("uuid_key_generator", new AlgorithmSegment("uuid", buildProps()));
    }
    
    private Properties buildProps() {
        Properties props = new Properties();
        props.put("worker-id", "123");
        return props;
    }
}
