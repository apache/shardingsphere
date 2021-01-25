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

package org.apache.shardingsphere.governance.core.config.checker;

import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.replicaquery.algorithm.config.AlgorithmProvidedReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class RuleConfigurationCheckerFactoryTest {
    
    @Test
    public void assertShardingRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(ShardingRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertAlgorithmProvidedShardingRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(AlgorithmProvidedShardingRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertEncryptRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(ReplicaQueryRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertAlgorithmProvidedEncryptRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(AlgorithmProvidedReplicaQueryRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertReplicaQueryRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(EncryptRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertAlgorithmProvidedReplicaQueryRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(AlgorithmProvidedEncryptRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertShadowRuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(ShadowRuleConfiguration.class)).isPresent());
    }
    
    @Test
    public void assertHARuleConfigurationChecker() {
        assertTrue(RuleConfigurationCheckerFactory.newInstance(mock(HARuleConfiguration.class)).isPresent());
    }
}
