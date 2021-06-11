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

package org.apache.shardingsphere.readwritesplitting.rule.checker;

import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlgorithmProvidedReadwriteSplittingRuleConfigurationCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    @Test
    public void assertCheckPass() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig = mock(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration ds0 = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(ds0.getAutoAwareDataSourceName()).thenReturn("ds0");
        when(ruleConfig.getDataSources()).thenReturn(Collections.singleton(ds0));
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(ruleConfig), RuleConfigurationChecker.class).get(ruleConfig);
        assertNotNull(checker);
        assertThat(checker, instanceOf(AlgorithmProvidedReadwriteSplittingRuleConfigurationChecker.class));
        checker.check("test", ruleConfig);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCheckNoPass() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration ruleConfig = mock(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration ds0 = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(ds0.getAutoAwareDataSourceName()).thenReturn("");
        when(ds0.getWriteDataSourceName()).thenReturn("");
        when(ruleConfig.getDataSources()).thenReturn(Collections.singleton(ds0));
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(Collections.singleton(ruleConfig), RuleConfigurationChecker.class).get(ruleConfig);
        assertNotNull(checker);
        assertThat(checker, instanceOf(AlgorithmProvidedReadwriteSplittingRuleConfigurationChecker.class));
        checker.check("test", ruleConfig);
    }
}
