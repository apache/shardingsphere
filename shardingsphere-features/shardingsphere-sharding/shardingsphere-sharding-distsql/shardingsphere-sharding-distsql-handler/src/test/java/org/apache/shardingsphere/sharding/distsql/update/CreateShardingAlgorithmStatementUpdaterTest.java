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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingAlgorithmStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAlgorithmStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingAlgorithmStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private CreateShardingAlgorithmStatementUpdater updater = new CreateShardingAlgorithmStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getName()).thenReturn("test");
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicate() throws DistSQLException {
        Properties properties = new Properties();
        properties.put("inputKey", "inputValue");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("inputAlgorithmName", new AlgorithmSegment("inputAlgorithmName", properties));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(algorithmSegment, algorithmSegment), null);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithExist() throws DistSQLException {
        Properties properties = new Properties();
        properties.put("inputKey", "inputValue");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("existAlgorithmName", new AlgorithmSegment("inputAlgorithmName", properties));
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getShardingAlgorithms().put("existAlgorithmName", new ShardingSphereAlgorithmConfiguration("hash_mod", properties));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(algorithmSegment), shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithoutRuleConfiguration() throws DistSQLException {
        Properties properties = new Properties();
        properties.put("inputKey", "inputValue");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("inputAlgorithmName", new AlgorithmSegment("inputAlgorithmName", properties));
        ShardingRuleConfiguration shardingRuleConfiguration = null;
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(algorithmSegment), shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithm() throws DistSQLException {
        Properties properties = new Properties();
        properties.put("inputKey", "inputValue");
        ShardingAlgorithmSegment algorithmSegment = new ShardingAlgorithmSegment("inputAlgorithmName", new AlgorithmSegment("inputAlgorithmName", properties));
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getShardingAlgorithms().put("existAlgorithmName", new ShardingSphereAlgorithmConfiguration("InvalidAlgorithm", properties));
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(algorithmSegment), shardingRuleConfiguration);
    }
    
    private CreateShardingAlgorithmStatement createSQLStatement(final ShardingAlgorithmSegment... ruleSegments) {
        return new CreateShardingAlgorithmStatement(Arrays.asList(ruleSegments));
    }
}
