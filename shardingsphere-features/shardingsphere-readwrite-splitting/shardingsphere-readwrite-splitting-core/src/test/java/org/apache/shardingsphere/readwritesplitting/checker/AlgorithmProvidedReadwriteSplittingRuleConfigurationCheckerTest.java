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

package org.apache.shardingsphere.readwritesplitting.checker;

import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlgorithmProvidedReadwriteSplittingRuleConfigurationCheckerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertValidCheck() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration config = createValidConfiguration();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(AlgorithmProvidedReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config);
    }
    
    private AlgorithmProvidedReadwriteSplittingRuleConfiguration createValidConfiguration() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration result = mock(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getType()).thenReturn("Dynamic");
        when(dataSourceConfig.getProps()).thenReturn(getProperties());
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceConfig));
        return result;
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("auto-aware-data-source-name", "ds0");
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertInValidCheck() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration config = createInvalidConfiguration();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(AlgorithmProvidedReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config);
    }
    
    private AlgorithmProvidedReadwriteSplittingRuleConfiguration createInvalidConfiguration() {
        AlgorithmProvidedReadwriteSplittingRuleConfiguration result = mock(AlgorithmProvidedReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getType()).thenReturn("Dynamic");
        when(dataSourceConfig.getProps()).thenReturn(new Properties());
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceConfig));
        return result;
    }
}
