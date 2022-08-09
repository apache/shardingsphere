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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Properties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleConfigurationCheckerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertValidCheck() {
        ReadwriteSplittingRuleConfiguration config = createValidConfiguration();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config, Collections.emptyMap(), Collections.singleton(mock(DynamicDataSourceContainedRule.class)));
    }
    
    private ReadwriteSplittingRuleConfiguration createValidConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getName()).thenReturn("readwrite_ds");
        when(dataSourceConfig.getDynamicStrategy()).thenReturn(new DynamicReadwriteSplittingStrategyConfiguration("ds0", "false"));
        when(result.getDataSources()).thenReturn(Collections.singletonList(dataSourceConfig));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertInvalidCheck() {
        ReadwriteSplittingRuleConfiguration config = createInvalidConfiguration();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config, Collections.emptyMap(), Collections.emptyList());
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getName()).thenReturn("readwrite_ds");
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceConfig));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertCheckWhenConfigInvalidWriteDataSource() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceRuleConfiguration> configurations = Arrays.asList(createDataSourceRuleConfig(
                "write_ds_0", Arrays.asList("ds_0", "ds_1")), createDataSourceRuleConfig("write_ds_1", Arrays.asList("ds_2", "ds_3")));
        when(config.getDataSources()).thenReturn(configurations);
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config, mockDataSources(), Collections.emptyList());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertCheckWhenConfigInvalidReadDataSource() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceRuleConfiguration> configurations = Arrays.asList(createDataSourceRuleConfig(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_0")), createDataSourceRuleConfig("write_ds_1", Arrays.asList("read_ds_0", "read_ds_0")));
        when(config.getDataSources()).thenReturn(configurations);
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config, mockDataSources(), Collections.emptyList());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertCheckWeightLoadBalanceInvalidDataSourceName() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceRuleConfiguration> configs = Collections.singletonList(createDataSourceRuleConfig("write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")));
        when(config.getDataSources()).thenReturn(configs);
        Properties props = new Properties();
        props.setProperty("read_ds_2", "1");
        props.setProperty("read_ds_1", "2");
        AlgorithmConfiguration algorithm = new AlgorithmConfiguration("WEIGHT", props);
        when(config.getLoadBalancers()).thenReturn(Collections.singletonMap("weight_ds", algorithm));
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(ReadwriteSplittingRuleConfigurationChecker.class));
        checker.get().check("test", config, mockDataSources(), Collections.emptyList());
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration createDataSourceRuleConfig(final String writeDataSource, final List<String> readDataSources) {
        ReadwriteSplittingDataSourceRuleConfiguration result = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        StaticReadwriteSplittingStrategyConfiguration readwriteSplittingStrategy = mock(StaticReadwriteSplittingStrategyConfiguration.class);
        when(readwriteSplittingStrategy.getWriteDataSourceName()).thenReturn(writeDataSource);
        when(readwriteSplittingStrategy.getReadDataSourceNames()).thenReturn(readDataSources);
        when(result.getStaticStrategy()).thenReturn(readwriteSplittingStrategy);
        when(result.getName()).thenReturn("readwrite_ds");
        when(result.getLoadBalancerName()).thenReturn("weight_ds");
        return result;
    }
    
    private Map<String, DataSource> mockDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("read_ds_0", mock(DataSource.class));
        result.put("read_ds_1", mock(DataSource.class));
        result.put("write_ds_0", mock(DataSource.class));
        result.put("write_ds_1", mock(DataSource.class));
        return result;
    }
}
