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
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.exception.checker.DataSourceNameExistedException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.DuplicateDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.InvalidWeightLoadBalancerConfigurationException;
import org.apache.shardingsphere.readwritesplitting.exception.checker.MissingRequiredWriteDataSourceNameException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingRuleConfigurationCheckerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertInvalidCheck() {
        ReadwriteSplittingRuleConfiguration config = createInvalidConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(MissingRequiredWriteDataSourceNameException.class, () -> checker.check("test", config, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getName()).thenReturn("readwrite_ds");
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceConfig));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidWriteDataSource() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceRuleConfiguration> configurations = Arrays.asList(createDataSourceRuleConfig(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")), createDataSourceRuleConfig("write_ds_2", Arrays.asList("read_ds_0", "read_ds_1")));
        when(config.getDataSources()).thenReturn(configurations);
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(DataSourceNameExistedException.class, () -> checker.check("test", config, mockDataSources(), Collections.emptyList()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidReadDataSource() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceRuleConfiguration> configurations = Arrays.asList(createDataSourceRuleConfig(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_0")), createDataSourceRuleConfig("write_ds_1", Arrays.asList("read_ds_0", "read_ds_0")));
        when(config.getDataSources()).thenReturn(configurations);
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(DuplicateDataSourceException.class, () -> checker.check("test", config, mockDataSources(), Collections.emptyList()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWeightLoadBalanceInvalidDataSourceName() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceRuleConfiguration> configs = Collections.singletonList(createDataSourceRuleConfig("write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")));
        when(config.getDataSources()).thenReturn(configs);
        AlgorithmConfiguration algorithm = new AlgorithmConfiguration("WEIGHT", PropertiesBuilder.build(new Property("read_ds_2", "1"), new Property("read_ds_1", "2")));
        when(config.getLoadBalancers()).thenReturn(Collections.singletonMap("weight_ds", algorithm));
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(InvalidWeightLoadBalancerConfigurationException.class, () -> checker.check("test", config, mockDataSources(), Collections.emptyList()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigOtherRulesDatasource() {
        ReadwriteSplittingRuleConfiguration config = createContainsOtherRulesDatasourceConfig();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class, RETURNS_DEEP_STUBS);
        when(dataSourceContainedRule.getDataSourceMapper().containsKey("otherDatasourceName")).thenReturn(true);
        checker.check("test", config, mockDataSources(), Collections.singleton(dataSourceContainedRule));
    }
    
    private ReadwriteSplittingRuleConfiguration createContainsOtherRulesDatasourceConfig() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(dataSourceConfig.getName()).thenReturn("readwrite_ds");
        when(dataSourceConfig.getWriteDataSourceName()).thenReturn("otherDatasourceName");
        when(dataSourceConfig.getReadDataSourceNames()).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        when(result.getDataSources()).thenReturn(Collections.singletonList(dataSourceConfig));
        return result;
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration createDataSourceRuleConfig(final String writeDataSource, final List<String> readDataSources) {
        ReadwriteSplittingDataSourceRuleConfiguration result = mock(ReadwriteSplittingDataSourceRuleConfiguration.class);
        when(result.getName()).thenReturn("readwrite_ds");
        when(result.getWriteDataSourceName()).thenReturn(writeDataSource);
        when(result.getReadDataSourceNames()).thenReturn(readDataSources);
        when(result.getLoadBalancerName()).thenReturn("weight_ds");
        return result;
    }
    
    private Map<String, DataSource> mockDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("read_ds_0", mock(DataSource.class));
        result.put("read_ds_1", mock(DataSource.class));
        result.put("write_ds_0", mock(DataSource.class));
        result.put("write_ds_1", mock(DataSource.class));
        return result;
    }
}
