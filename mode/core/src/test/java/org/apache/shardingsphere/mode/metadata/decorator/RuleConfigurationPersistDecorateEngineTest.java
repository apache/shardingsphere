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

package org.apache.shardingsphere.mode.metadata.decorator;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.spi.RuleConfigurationPersistDecorator;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
class RuleConfigurationPersistDecorateEngineTest {
    
    private RuleConfigurationPersistDecorateEngine decorateEngine;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComputeNodeInstanceContext computeNodeInstanceContext;
    
    @BeforeEach
    void setUp() {
        decorateEngine = new RuleConfigurationPersistDecorateEngine(computeNodeInstanceContext);
    }
    
    @Test
    void assertDecorateIfNotClusterMode() {
        Collection<RuleConfiguration> ruleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        assertThat(decorateEngine.decorate(ruleConfigs), is(ruleConfigs));
    }
    
    @Test
    void assertDecorateIfClusterMode() {
        when(computeNodeInstanceContext.getModeConfiguration().isCluster()).thenReturn(true);
        when(TypedSPILoader.findService(eq(RuleConfigurationPersistDecorator.class), any())).thenReturn(Optional.empty());
        Collection<RuleConfiguration> ruleConfigs = Collections.singletonList(mock(RuleConfiguration.class));
        assertThat(decorateEngine.decorate(ruleConfigs), is(ruleConfigs));
    }
    
    @Test
    void assertRestoreIfNotClusterMode() {
        Collection<RuleConfiguration> ruleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        assertThat(decorateEngine.restore(ruleConfigs), is(ruleConfigs));
    }
    
    @Test
    void assertRestoreIfClusterMode() {
        when(computeNodeInstanceContext.getModeConfiguration().isCluster()).thenReturn(true);
        when(TypedSPILoader.findService(eq(RuleConfigurationPersistDecorator.class), any())).thenReturn(Optional.empty());
        Collection<RuleConfiguration> ruleConfigs = Collections.singletonList(mock(RuleConfiguration.class));
        assertThat(decorateEngine.restore(ruleConfigs), is(ruleConfigs));
    }
    
    @Test
    void assertTryRestore() {
        when(TypedSPILoader.findService(eq(RuleConfigurationPersistDecorator.class), any())).thenReturn(Optional.empty());
        Collection<RuleConfiguration> ruleConfigs = Collections.singletonList(mock(RuleConfiguration.class));
        assertThat(decorateEngine.tryRestore(ruleConfigs), is(ruleConfigs));
    }
}
