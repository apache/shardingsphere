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

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLFederationRuleTest {
    
    @Test
    void assertDefaultProviderDisablesFederation() {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        when(provider.isDefault()).thenReturn(true);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(provider));
            SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L));
            SQLFederationRule actual = new SQLFederationRule(ruleConfig, Collections.emptyList());
            assertThat(actual.getConfiguration(), sameInstance(ruleConfig));
            assertThat(actual.getProvider(), sameInstance(provider));
            assertFalse(actual.isSqlFederationEnabled());
            assertThat(actual.getOrder(), is(SQLFederationOrder.ORDER));
        }
    }
    
    @Test
    void assertConstructEnabledInitializesProvider() {
        SQLFederationProvider expected = mock(SQLFederationProvider.class);
        when(expected.getType()).thenReturn("CALCITE");
        when(expected.isSQLFederationEnabled()).thenReturn(true);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(expected));
            SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L), "CALCITE");
            SQLFederationRule actual = new SQLFederationRule(ruleConfig, Collections.emptyList());
            assertThat(actual.getProvider(), sameInstance(expected));
            assertTrue(actual.isSqlFederationEnabled());
            verify(expected).initialize(ruleConfig, Collections.emptyList());
        }
    }
    
    @Test
    void assertDefaultProviderMissing() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L));
        ServiceProviderNotFoundException actual = assertThrows(ServiceProviderNotFoundException.class, () -> new SQLFederationRule(ruleConfig, Collections.emptyList()));
        assertThat(actual.getMessage(), is("SPI-00001: No implementation class load from SPI 'org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider' with type 'null'."));
    }
    
    @Test
    void assertUnknownProviderMissing() {
        SQLFederationRuleConfiguration ruleConfig = new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L), "UNKNOWN");
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        when(provider.isDefault()).thenReturn(true);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(provider));
            ServiceProviderNotFoundException actual = assertThrows(ServiceProviderNotFoundException.class, () -> new SQLFederationRule(ruleConfig, Collections.emptyList()));
            assertThat(actual.getMessage(), is("SPI-00001: No implementation class load from SPI 'org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider' with type 'UNKNOWN'."));
        }
    }
    
    @Test
    void assertConstructWithDuplicateProviderTypes() {
        SQLFederationProvider first = mock(SQLFederationProvider.class);
        SQLFederationProvider second = mock(SQLFederationProvider.class);
        when(first.getType()).thenReturn("CALCITE");
        when(second.getType()).thenReturn("calcite");
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Arrays.asList(first, second));
            SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L), "CALCITE");
            SQLFederationRule actual = new SQLFederationRule(config, Collections.emptyList());
            assertThat(actual.getProvider(), sameInstance(first));
            verify(first).initialize(config, Collections.emptyList());
        }
    }
}
