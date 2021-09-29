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

package org.apache.shardingsphere.shadow.checker;

import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlgorithmProvidedShadowRuleConfigurationCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertValidCheck() {
        AlgorithmProvidedShadowRuleConfiguration config = createValidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singletonList(config)).get(config);
        assertThat(checker, instanceOf(AlgorithmProvidedShadowRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createValidConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = mock(AlgorithmProvidedShadowRuleConfiguration.class);
        when(result.getColumn()).thenReturn("id");
        when(result.getSourceDataSourceNames()).thenReturn(Collections.singletonList("ds0"));
        when(result.getShadowDataSourceNames()).thenReturn(Collections.singletonList("shadow0"));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertInvalidCheck() {
        AlgorithmProvidedShadowRuleConfiguration config = createInvalidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singletonList(config)).get(config);
        assertThat(checker, instanceOf(AlgorithmProvidedShadowRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createInvalidConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = mock(AlgorithmProvidedShadowRuleConfiguration.class);
        when(result.getColumn()).thenReturn("");
        when(result.getSourceDataSourceNames()).thenReturn(Collections.emptyList());
        when(result.getShadowDataSourceNames()).thenReturn(Collections.emptyList());
        return result;
    }
}
