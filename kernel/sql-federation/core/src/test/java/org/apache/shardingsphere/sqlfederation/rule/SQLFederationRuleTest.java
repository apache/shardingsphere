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

import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderDuplicatedException;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderNotFoundException;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SQLFederationRuleTest {
    
    @Test
    void assertConstructDisabled() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(4, 64L));
        SQLFederationRule actual = new SQLFederationRule(ruleConfig, Collections.emptyList());
        assertThat(actual.getConfiguration(), sameInstance(ruleConfig));
        assertNull(actual.getProvider());
        assertThat(actual.getOrder(), is(SQLFederationOrder.ORDER));
    }
    
    @Test
    void assertRefreshDisabled() {
        SQLFederationRule rule = new SQLFederationRule(new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(4, 64L)), Collections.emptyList());
        rule.refresh(Collections.emptyList(), GlobalRuleChangedType.DATABASE_CHANGED);
        assertNull(rule.getProvider());
    }
    
    @Test
    void assertDefaultProviderMissing() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L));
        SQLFederationProviderNotFoundException actual = assertThrows(SQLFederationProviderNotFoundException.class, () -> new SQLFederationRule(ruleConfig, Collections.emptyList()));
        assertThat(actual.getMessage(), is("SQL_FEDERATION-00001: SQL Federation provider 'CALCITE' is not installed."));
    }
    
    @Test
    void assertUnknownProviderMissing() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L), "UNKNOWN");
        SQLFederationProviderNotFoundException actual = assertThrows(SQLFederationProviderNotFoundException.class, () -> new SQLFederationRule(ruleConfig, Collections.emptyList()));
        assertThat(actual.getMessage(), is("SQL_FEDERATION-00001: SQL Federation provider 'UNKNOWN' is not installed."));
    }
    
    @Test
    void assertDuplicateProviderTypeFails() {
        SQLFederationProvider first = mock(SQLFederationProvider.class);
        SQLFederationProvider second = mock(SQLFederationProvider.class);
        when(first.getType()).thenReturn("CALCITE");
        when(second.getType()).thenReturn("calcite");
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Arrays.asList(first, second));
            SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L));
            assertThrows(SQLFederationProviderDuplicatedException.class, () -> new SQLFederationRule(config, Collections.emptyList()));
        }
    }
}
