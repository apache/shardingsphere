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
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.exception.actual.DuplicateReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.MissingRequiredReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.ReadwriteSplittingActualDataSourceNotFoundException;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class ReadwriteSplittingRuleConfigurationCheckerTest {
    
    @Test
    void assertInvalidCheck() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createInvalidRuleConfiguration();
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        assertThrows(MissingRequiredReadwriteSplittingActualDataSourceException.class, () -> checker.check("test", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = mock(ReadwriteSplittingDataSourceGroupRuleConfiguration.class);
        when(dataSourceGroupConfig.getName()).thenReturn("readwrite_ds");
        when(result.getDataSourceGroups()).thenReturn(Collections.singleton(dataSourceGroupConfig));
        return result;
    }
    
    @Test
    void assertCheckWhenConfigInvalidWriteDataSource() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs = Arrays.asList(createDataSourceGroupRuleConfiguration(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")), createDataSourceGroupRuleConfiguration("write_ds_2", Arrays.asList("read_ds_0", "read_ds_1")));
        when(ruleConfig.getDataSourceGroups()).thenReturn(configs);
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        assertThrows(ReadwriteSplittingActualDataSourceNotFoundException.class, () -> checker.check("test", ruleConfig, mockDataSources(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWhenConfigInvalidReadDataSource() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        List<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs = Arrays.asList(createDataSourceGroupRuleConfiguration(
                "write_ds_0", Arrays.asList("read_ds_0", "read_ds_0")), createDataSourceGroupRuleConfiguration("write_ds_1", Arrays.asList("read_ds_0", "read_ds_0")));
        when(ruleConfig.getDataSourceGroups()).thenReturn(configs);
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class, () -> checker.check("test", ruleConfig, mockDataSources(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWeightLoadBalanceInvalidDataSourceName() {
        ReadwriteSplittingRuleConfiguration ruleConfig = mock(ReadwriteSplittingRuleConfiguration.class);
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> configs = Collections.singleton(createDataSourceGroupRuleConfiguration("write_ds_0", Arrays.asList("read_ds_0", "read_ds_1")));
        when(ruleConfig.getDataSourceGroups()).thenReturn(configs);
        AlgorithmConfiguration algorithm = new AlgorithmConfiguration("WEIGHT", PropertiesBuilder.build(new Property("read_ds_2", "1"), new Property("read_ds_1", "2")));
        when(ruleConfig.getLoadBalancers()).thenReturn(Collections.singletonMap("weight_ds", algorithm));
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        assertThrows(AlgorithmInitializationException.class, () -> checker.check("test", ruleConfig, mockDataSources(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWhenConfigOtherRulesDatasource() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createContainsOtherRulesDatasourceConfiguration();
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper().containsKey("otherDatasourceName")).thenReturn(true);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        checker.check("test", ruleConfig, mockDataSources(), Collections.singleton(rule));
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
        Map<String, DataSource> result = new LinkedHashMap<>(4, 1F);
        result.put("read_ds_0", new MockedDataSource());
        result.put("read_ds_1", new MockedDataSource());
        result.put("write_ds_0", new MockedDataSource());
        result.put("write_ds_1", new MockedDataSource());
        return result;
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceGroupConfig), Collections.emptyMap());
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        assertThat(checker.getRequiredDataSourceNames(ruleConfig), is(new LinkedHashSet<>(Arrays.asList("write_ds", "read_ds0", "read_ds1"))));
    }
    
    @Test
    void assertGetRequiredDataSourceNamesWhenEmpty() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_group", null, Collections.emptyList(), "foo_algo");
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceGroupConfig), Collections.emptyMap());
        DatabaseRuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        assertTrue(checker.getRequiredDataSourceNames(ruleConfig).isEmpty());
    }
}
