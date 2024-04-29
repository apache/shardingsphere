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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.update.DropShadowAlgorithmExecutor;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropShadowAlgorithmExecutorTest {
    
    private final DropShadowAlgorithmExecutor executor = new DropShadowAlgorithmExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertExecuteWithIfExists() {
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement(true, "ruleSegment");
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(new ShadowRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
    }
    
    @Test
    void assertUpdate() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getShadowAlgorithms().put("shadow_algorithm", new AlgorithmConfiguration("type", null));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        DropShadowAlgorithmStatement sqlStatement = createSQLStatement("shadow_algorithm");
        executor.checkBeforeUpdate(sqlStatement);
        ShadowRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(toBeDroppedRuleConfig.getShadowAlgorithms().size(), is(1));
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final String... ruleName) {
        return new DropShadowAlgorithmStatement(false, Arrays.asList(ruleName));
    }
    
    private DropShadowAlgorithmStatement createSQLStatement(final boolean ifExists, final String... ruleName) {
        return new DropShadowAlgorithmStatement(ifExists, Arrays.asList(ruleName));
    }
}
