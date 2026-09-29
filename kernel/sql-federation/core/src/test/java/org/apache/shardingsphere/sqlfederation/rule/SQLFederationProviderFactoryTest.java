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
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderDuplicatedException;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLFederationProviderFactoryTest {
    
    @Test
    void assertExplicitProviderMissing() {
        SQLFederationProvider defaultProvider = mock(SQLFederationProvider.class);
        when(defaultProvider.getType()).thenReturn("CALCITE");
        when(defaultProvider.isDefault()).thenReturn(true);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(defaultProvider));
            ServiceProviderNotFoundException actual = assertThrows(ServiceProviderNotFoundException.class, () -> SQLFederationProviderFactory.getProvider("UNKNOWN"));
            assertThat(actual.getMessage(), is("SPI-00001: No implementation class load from SPI 'org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider' with type 'UNKNOWN'."));
        }
    }
    
    @Test
    void assertDefaultProviderMissing() {
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.emptyList());
            ServiceProviderNotFoundException actual = assertThrows(ServiceProviderNotFoundException.class, () -> SQLFederationProviderFactory.getProvider(null));
            assertThat(actual.getMessage(), is("SPI-00001: No implementation class load from SPI 'org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider' with type 'CALCITE'."));
        }
    }
    
    @Test
    void assertDuplicateProviderType() {
        SQLFederationProvider first = mock(SQLFederationProvider.class);
        SQLFederationProvider second = mock(SQLFederationProvider.class);
        when(first.getType()).thenReturn("CALCITE");
        when(second.getType()).thenReturn("calcite");
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Arrays.asList(first, second));
            SQLFederationProviderDuplicatedException actual = assertThrows(SQLFederationProviderDuplicatedException.class, () -> SQLFederationProviderFactory.getProvider(null));
            assertThat(actual.getMessage(), is("SQL_FEDERATION-00002: Multiple SQL Federation providers have type 'calcite'."));
        }
    }
    
    @Test
    void assertValidProviderWithoutRuleInitialization() {
        SQLFederationProvider expected = mock(SQLFederationProvider.class);
        when(expected.getType()).thenReturn("CALCITE");
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(expected));
            SQLFederationProvider actual = SQLFederationProviderFactory.getProvider("calcite");
            assertThat(actual, sameInstance(expected));
            verify(expected, never()).initialize(any(), any());
        }
    }
}
