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
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleConfigurationCheckerTest {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertValidCheck() {
        ReadwriteSplittingRuleConfiguration config = createValidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singleton(config)).get(config);
        assertThat(checker, instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private ReadwriteSplittingRuleConfiguration createValidConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getType()).thenReturn("Dynamic");
        when(dataSourceConfig.getProps()).thenReturn(getProperties());
        when(result.getDataSources()).thenReturn(Collections.singletonList(dataSourceConfig));
        return result;
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("auto-aware-data-source-name", "ds0");
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertInvalidCheck() {
        ReadwriteSplittingRuleConfiguration config = createInvalidConfiguration();
        RuleConfigurationChecker checker = OrderedSPIRegistry.getRegisteredServices(RuleConfigurationChecker.class, Collections.singleton(config)).get(config);
        assertThat(checker, instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.check("test", config);
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getType()).thenReturn("Dynamic");
        when(dataSourceConfig.getProps()).thenReturn(new Properties());
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceConfig));
        return result;
    }
}
