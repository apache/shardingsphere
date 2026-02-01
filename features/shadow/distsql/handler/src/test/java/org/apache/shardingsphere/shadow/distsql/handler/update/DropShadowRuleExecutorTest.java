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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DropShadowRuleExecutorTest {
    
    private final DropShadowRuleExecutor executor = new DropShadowRuleExecutor();
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertCheckWithRuleNotExisted() {
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(new ShadowRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class,
                () -> executor.checkBeforeUpdate(createSQLStatement("notExistedRuleName")));
    }
    
    @Test
    void assertCheckWithIfExists() {
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(new ShadowRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(createSQLStatement(true, "notExistedRuleName"));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithUnusedAlgorithms() {
        DropShadowRuleStatement sqlStatement = createSQLStatement("shadow_group");
        ShadowRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        ShadowRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(toBeDroppedRuleConfig.getDataSources().size(), is(1));
        assertThat(toBeDroppedRuleConfig.getTables().size(), is(1));
        assertThat(toBeDroppedRuleConfig.getShadowAlgorithms().size(), is(1));
    }
    
    @Test
    void assertUpdateMultipleCurrentRuleConfigurationWithInUsedAlgorithms() {
        DropShadowRuleStatement sqlStatement = createSQLStatement("shadow_group");
        ShadowRuleConfiguration ruleConfig = createMultipleCurrentRuleConfiguration();
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
        executor.checkBeforeUpdate(sqlStatement);
        ShadowRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(toBeDroppedRuleConfig.getDataSources().size(), is(1));
        assertThat(toBeDroppedRuleConfig.getTables().size(), is(1));
        assertThat(toBeDroppedRuleConfig.getShadowAlgorithms().size(), is(1));
    }
    
    private DropShadowRuleStatement createSQLStatement(final String... ruleName) {
        DropShadowRuleStatement result = new DropShadowRuleStatement(false, Arrays.asList(ruleName));
        result.buildAttributes();
        return result;
    }
    
    private DropShadowRuleStatement createSQLStatement(final boolean ifExists, final String... ruleName) {
        DropShadowRuleStatement result = new DropShadowRuleStatement(ifExists, Arrays.asList(ruleName));
        result.buildAttributes();
        return result;
    }
    
    private ShadowRuleConfiguration createCurrentRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(createShadowDataSourceConfiguration("shadow_group"));
        result.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("shadow_group")), Collections.emptyList()));
        result.getShadowAlgorithms().put("t_order_algorithm", new AlgorithmConfiguration("SHADOW", new Properties()));
        return result;
    }
    
    private ShadowRuleConfiguration createMultipleCurrentRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(createShadowDataSourceConfiguration("shadow_group"));
        result.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("shadow_group")), Collections.emptyList()));
        result.getShadowAlgorithms().put("t_order_algorithm_inUsed", new AlgorithmConfiguration("SHADOW", new Properties()));
        result.getShadowAlgorithms().put("t_order_algorithm_unused", new AlgorithmConfiguration("SHADOW", new Properties()));
        result.setDefaultShadowAlgorithmName("t_order_algorithm_inUsed");
        return result;
    }
    
    private ShadowDataSourceConfiguration createShadowDataSourceConfiguration(final String ruleName) {
        return new ShadowDataSourceConfiguration(ruleName, "production", "shadow");
    }
}
