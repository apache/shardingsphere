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

package org.apache.shardingsphere.sqlfederation.rule;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContextFactory;
import org.apache.shardingsphere.sqlfederation.compiler.exception.InvalidExecutionPlanCacheConfigException;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class SQLFederationRuleTest {
    
    @Test
    void assertConstructThrowsWhenInitialCapacityNotPositive() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(0, 1L));
        InvalidExecutionPlanCacheConfigException exception = assertThrows(InvalidExecutionPlanCacheConfigException.class, () -> new SQLFederationRule(ruleConfig, Collections.emptyList()));
        assertThat(exception.getMessage(), is("Invalid execution plan cache config: `initialCapacity`=`0`, the value must be positive."));
    }
    
    @Test
    void assertConstructThrowsWhenMaximumSizeNotPositive() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(1, 0L));
        InvalidExecutionPlanCacheConfigException exception = assertThrows(InvalidExecutionPlanCacheConfigException.class, () -> new SQLFederationRule(ruleConfig, Collections.emptyList()));
        assertThat(exception.getMessage(), is("Invalid execution plan cache config: `maximumSize`=`0`, the value must be positive."));
    }
    
    @Test
    void assertConstructSuccess() {
        CompilerContext initialContext = mock(CompilerContext.class);
        CompilerContext refreshedContext = mock(CompilerContext.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(4, 64L));
        try (MockedStatic<CompilerContextFactory> mockedFactory = mockStatic(CompilerContextFactory.class)) {
            mockedFactory.when(() -> CompilerContextFactory.create(Collections.singleton(database))).thenReturn(initialContext, refreshedContext);
            SQLFederationRule rule = new SQLFederationRule(ruleConfig, Collections.singleton(database));
            assertThat(rule.getConfiguration(), is(ruleConfig));
            assertThat(rule.getCompilerContext(), is(initialContext));
            assertThat(rule.getOrder(), is(SQLFederationOrder.ORDER));
            mockedFactory.verify(() -> CompilerContextFactory.create(Collections.singleton(database)));
        }
    }
    
    @Test
    void assertRefresh() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(4, 64L));
        try (MockedStatic<CompilerContextFactory> mockedFactory = mockStatic(CompilerContextFactory.class)) {
            mockedFactory.when(() -> CompilerContextFactory.create(Collections.singleton(database))).thenReturn(mock(CompilerContext.class), mock(CompilerContext.class));
            SQLFederationRule rule = new SQLFederationRule(ruleConfig, Collections.singleton(database));
            rule.refresh(Collections.singleton(database), GlobalRuleChangedType.DATABASE_CHANGED);
            mockedFactory.verify(() -> CompilerContextFactory.create(Collections.singleton(database)), times(2));
        }
    }
}
