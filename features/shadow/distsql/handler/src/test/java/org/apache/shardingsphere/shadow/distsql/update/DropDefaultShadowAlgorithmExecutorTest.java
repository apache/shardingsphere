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

package org.apache.shardingsphere.shadow.distsql.update;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropDefaultShadowAlgorithmExecutor;
import org.apache.shardingsphere.shadow.distsql.statement.DropDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DropDefaultShadowAlgorithmExecutorTest {
    
    private final DropDefaultShadowAlgorithmExecutor executor = new DropDefaultShadowAlgorithmExecutor();
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckWithoutDefaultAlgorithm() {
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        executor.setRule(rule);
        assertThrows(UnregisteredAlgorithmException.class, () -> executor.checkBeforeUpdate(new DropDefaultShadowAlgorithmStatement(false)));
    }
    
    @Test
    void assertCheckWithIfExists() {
        executor.checkBeforeUpdate(new DropDefaultShadowAlgorithmStatement(true));
    }
    
    @Test
    void assertUpdate() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.setDefaultShadowAlgorithmName("default");
        ruleConfig.getShadowAlgorithms().put(ruleConfig.getDefaultShadowAlgorithmName(), mock(AlgorithmConfiguration.class));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(new DropDefaultShadowAlgorithmStatement(true));
        DropDefaultShadowAlgorithmStatement sqlStatement = new DropDefaultShadowAlgorithmStatement(false);
        assertTrue(executor.hasAnyOneToBeDropped(sqlStatement));
        ShadowRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(toBeDroppedRuleConfig.getDefaultShadowAlgorithmName(), is("default"));
        assertThat(toBeDroppedRuleConfig.getShadowAlgorithms().size(), is(1));
    }
}
