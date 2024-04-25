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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.exception.actual.ReadwriteSplittingActualDataSourceNotFoundException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.DuplicateReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.MissingRequiredReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
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
        assertThrows(MissingRequiredReadwriteSplittingActualDataSourceException.class, () -> checker.check("test", config, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = mock(ReadwriteSplittingDataSourceGroupRuleConfiguration.class);
        when(dataSourceGroupConfig.getName()).thenReturn("readwrite_ds");
        when(result.getDataSourceGroups()).thenReturn(Collections.singleton(dataSourceGroupConfig));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidWriteDataSource() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs = Arrays.asList(createDataSourceGroupRuleConfiguration(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")), createDataSourceGroupRuleConfiguration("write_ds_2", Arrays.asList("read_ds_0", "read_ds_1")));
        when(config.getDataSourceGroups()).thenReturn(configs);
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(ReadwriteSplittingActualDataSourceNotFoundException.class, () -> checker.check("test", config, mockDataSources(), Collections.emptyList()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidReadDataSource() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs = Arrays.asList(createDataSourceGroupRuleConfiguration(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_0")), createDataSourceGroupRuleConfiguration("write_ds_1", Arrays.asList("read_ds_0", "read_ds_0")));
        when(config.getDataSourceGroups()).thenReturn(configs);
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class, () -> checker.check("test", config, mockDataSources(), Collections.emptyList()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWeightLoadBalanceInvalidDataSourceName() {
        ReadwriteSplittingRuleConfiguration config = mock(ReadwriteSplittingRuleConfiguration.class);
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs = Collections.singleton(createDataSourceGroupRuleConfiguration("write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")));
        when(config.getDataSourceGroups()).thenReturn(configs);
        AlgorithmConfiguration algorithm = new AlgorithmConfiguration("WEIGHT", PropertiesBuilder.build(new Property("read_ds_2", "1"), new Property("read_ds_1", "2")));
        when(config.getLoadBalancers()).thenReturn(Collections.singletonMap("weight_ds", algorithm));
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(AlgorithmInitializationException.class, () -> checker.check("test", config, mockDataSources(), Collections.emptyList()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigOtherRulesDatasource() {
        ReadwriteSplittingRuleConfiguration config = createContainsOtherRulesDatasourceConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper().containsKey("otherDatasourceName")).thenReturn(true);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        checker.check("test", config, mockDataSources(), Collections.singleton(rule));
    }
    
    private ReadwriteSplittingRuleConfiguration createContainsOtherRulesDatasourceConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = mock(ReadwriteSplittingDataSourceGroupRuleConfiguration.class);
        when(dataSourceGroupConfig.getName()).thenReturn("readwrite_ds");
        when(dataSourceGroupConfig.getWriteDataSourceName()).thenReturn("otherDatasourceName");
        when(dataSourceGroupConfig.getReadDataSourceNames()).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        when(result.getDataSourceGroups()).thenReturn(Collections.singleton(dataSourceGroupConfig));
        return result;
    }
    
    private ReadwriteSplittingDataSourceGroupRuleConfiguration createDataSourceGroupRuleConfiguration(final String writeDataSource, final List<String> readDataSources) {
        ReadwriteSplittingDataSourceGroupRuleConfiguration result = mock(ReadwriteSplittingDataSourceGroupRuleConfiguration.class);
        when(result.getName()).thenReturn("readwrite_ds");
        when(result.getWriteDataSourceName()).thenReturn(writeDataSource);
        when(result.getReadDataSourceNames()).thenReturn(readDataSources);
        when(result.getLoadBalancerName()).thenReturn("weight_ds");
        return result;
    }
    
    private Map<String, DataSource> mockDataSources() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("read_ds_0", new MockedDataSource());
        result.put("read_ds_1", new MockedDataSource());
        result.put("write_ds_0", new MockedDataSource());
        result.put("write_ds_1", new MockedDataSource());
        return result;
    }
}
