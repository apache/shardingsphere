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

package org.apache.shardingsphere.sqlfederation.config;

import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SQLFederationRuleConfigurationValidatorTest {
    
    @Test
    void assertExplicitProviderExists() {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        when(provider.getType()).thenReturn("CALCITE");
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(provider));
            assertDoesNotThrow(() -> RuleConfigurationValidator.validate(new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L), "CALCITE")));
        }
    }
    
    @Test
    void assertUnknownProviderFailsWithDefaultInstalled() {
        SQLFederationProvider provider = mock(SQLFederationProvider.class);
        when(provider.getType()).thenReturn("NONE");
        when(provider.isDefault()).thenReturn(true);
        try (MockedStatic<ShardingSphereServiceLoader> serviceLoader = mockStatic(ShardingSphereServiceLoader.class)) {
            serviceLoader.when(() -> ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)).thenReturn(Collections.singleton(provider));
            assertThrows(InvalidRuleConfigurationException.class,
                    () -> RuleConfigurationValidator.validate(new SQLFederationRuleConfiguration(false, new SQLFederationCacheOption(4, 64L), "UNKNOWN")));
        }
    }
}
