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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.converter;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;

public final class ReadwriteSplittingRuleStatementConverterTest {

    @Test
    public void assertEmptyRuleSegmentConvertResult() {
        ReadwriteSplittingRuleConfiguration actualEmptyRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.emptyList());
        assertThat(actualEmptyRuleSegmentConvertResult.getDataSources(), org.hamcrest.Matchers.empty());
        assertThat(actualEmptyRuleSegmentConvertResult.getLoadBalancers().size(), org.hamcrest.Matchers.is(0));
    }

    @Test
    public void assertSingleRuleSegmentConvertResult() {
        Properties properties1 = new Properties();
        properties1.setProperty("ping", "pong");
        ReadwriteSplittingRuleSegment expectedSingleReadwriteSplittingRuleSegment = createReadwriteSplittingRuleSegment("name1", "write_ds_01",
                Arrays.asList("read_ds_01", "read_ds_02"), "lb01", properties1);
        ReadwriteSplittingRuleConfiguration actualSingleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.singleton(expectedSingleReadwriteSplittingRuleSegment));
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> actualSingleRuleSegmentConvertResultDataSources = actualSingleRuleSegmentConvertResult.getDataSources();
        Map<String, ShardingSphereAlgorithmConfiguration> actualSingleRuleSegmentConvertResultLoadBalancers = actualSingleRuleSegmentConvertResult.getLoadBalancers();
        assertThat(actualSingleRuleSegmentConvertResultDataSources.size(), org.hamcrest.Matchers.is(1));
        assertThat(actualSingleRuleSegmentConvertResultLoadBalancers.size(), org.hamcrest.Matchers.is(1));
        Object[] dataSources = actualSingleRuleSegmentConvertResultDataSources.toArray();
        ReadwriteSplittingDataSourceRuleConfiguration actualRuleConfiguration = (ReadwriteSplittingDataSourceRuleConfiguration) dataSources[0];
        assertThat(actualRuleConfiguration.getName(), org.hamcrest.Matchers.is(expectedSingleReadwriteSplittingRuleSegment.getName()));
        String expectedLoadBalancerName = String.format("%s_%s", expectedSingleReadwriteSplittingRuleSegment.getName(), expectedSingleReadwriteSplittingRuleSegment.getLoadBalancer());
        assertThat(actualRuleConfiguration.getLoadBalancerName(), org.hamcrest.Matchers.is(expectedLoadBalancerName));
        assertThat(actualRuleConfiguration.getWriteDataSourceName(), org.hamcrest.Matchers.is(expectedSingleReadwriteSplittingRuleSegment.getWriteDataSource()));
        assertThat(actualRuleConfiguration.getReadDataSourceNames(),
                org.hamcrest.Matchers.equalTo(expectedSingleReadwriteSplittingRuleSegment.getReadDataSources() == null
                        ? Collections.emptyList()
                        : expectedSingleReadwriteSplittingRuleSegment.getReadDataSources()));
        String actualLoadBalancerName = actualSingleRuleSegmentConvertResultLoadBalancers.keySet().toArray()[0].toString();
        assertThat(actualLoadBalancerName, org.hamcrest.Matchers.is(expectedLoadBalancerName));
        ShardingSphereAlgorithmConfiguration actualSphereAlgorithmConfiguration = actualSingleRuleSegmentConvertResultLoadBalancers.get(actualLoadBalancerName);
        assertThat(actualSphereAlgorithmConfiguration.getType(), org.hamcrest.Matchers.containsString(expectedSingleReadwriteSplittingRuleSegment.getLoadBalancer()));
        assertThat(actualSphereAlgorithmConfiguration.getProps(), org.hamcrest.Matchers.is(expectedSingleReadwriteSplittingRuleSegment.getProps()));
    }

    @Test
    public void assertMultipleReadwriteSplittingRuleSegments() {
        Properties properties2 = new Properties();
        properties2.setProperty("ping1", "pong1");
        Properties properties3 = new Properties();
        properties3.setProperty("ping2", "pong2");
        Properties properties4 = new Properties();
        properties4.setProperty("ping3", "pong3");
        properties4.setProperty("ping4", "pong4");
        List<ReadwriteSplittingRuleSegment> expectedMultipleReadwriteSplittingRuleSegments = Arrays.asList(
                createReadwriteSplittingRuleSegment("name2", "write_ds_02", Arrays.asList("read_ds_02", "read_ds_03"), "lb02", properties2),
                createReadwriteSplittingRuleSegment("name3", "write_ds_03", Arrays.asList("read_ds_04", "read_ds_05"), "lb03", properties3),
                createReadwriteSplittingRuleSegment("name4", "autoAwareResource", "lb04", properties4));
        ReadwriteSplittingRuleConfiguration actualMultipleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter.convert(expectedMultipleReadwriteSplittingRuleSegments);
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> actualMultipleRuleSegmentConvertResultDataSources = actualMultipleRuleSegmentConvertResult.getDataSources();
        Map<String, ShardingSphereAlgorithmConfiguration> actualMultipleRuleSegmentConvertResultLoadBalancers = actualMultipleRuleSegmentConvertResult.getLoadBalancers();
        assertThat(actualMultipleRuleSegmentConvertResultDataSources.size(), org.hamcrest.Matchers.is(expectedMultipleReadwriteSplittingRuleSegments.size()));
        assertThat(actualMultipleRuleSegmentConvertResultLoadBalancers.size(), org.hamcrest.Matchers.is(expectedMultipleReadwriteSplittingRuleSegments.size()));
        ReadwriteSplittingDataSourceRuleConfiguration[] actualRuleConfigurations = new ReadwriteSplittingDataSourceRuleConfiguration[expectedMultipleReadwriteSplittingRuleSegments.size()];
        actualMultipleRuleSegmentConvertResultDataSources.toArray(actualRuleConfigurations);
        Stream.iterate(0, i -> i + 1)
                .limit(expectedMultipleReadwriteSplittingRuleSegments.size())
                .forEach(i -> {
                    ReadwriteSplittingRuleSegment expectedReadwriteSplittingRuleSegment = expectedMultipleReadwriteSplittingRuleSegments.get(i);
                    ReadwriteSplittingDataSourceRuleConfiguration actualRuleConfiguration = actualRuleConfigurations[i];
                    assertThat(actualRuleConfiguration.getName(), org.hamcrest.Matchers.is(expectedReadwriteSplittingRuleSegment.getName()));
                    String expectedLoadBalancerName = String.format("%s_%s", expectedReadwriteSplittingRuleSegment.getName(), expectedReadwriteSplittingRuleSegment.getLoadBalancer());
                    assertThat(actualRuleConfiguration.getLoadBalancerName(), org.hamcrest.Matchers.is(expectedLoadBalancerName));
                    assertThat(actualRuleConfiguration.getWriteDataSourceName(), org.hamcrest.Matchers.is(expectedReadwriteSplittingRuleSegment.getWriteDataSource()));
                    assertThat(actualRuleConfiguration.getReadDataSourceNames(),
                            org.hamcrest.Matchers.equalTo(expectedReadwriteSplittingRuleSegment.getReadDataSources() == null
                                    ? Collections.emptyList()
                                    : expectedReadwriteSplittingRuleSegment.getReadDataSources()));
                    assertThat(actualMultipleRuleSegmentConvertResultLoadBalancers, org.hamcrest.Matchers.hasKey(expectedLoadBalancerName));
                    ShardingSphereAlgorithmConfiguration actualSphereAlgorithmConfiguration = actualMultipleRuleSegmentConvertResultLoadBalancers.get(actualRuleConfiguration.getLoadBalancerName());
                    assertThat(actualSphereAlgorithmConfiguration.getType(), org.hamcrest.Matchers.containsString(expectedReadwriteSplittingRuleSegment.getLoadBalancer()));
                    assertThat(actualSphereAlgorithmConfiguration.getProps(), org.hamcrest.Matchers.is(expectedReadwriteSplittingRuleSegment.getProps()));
                });
    }

    private ReadwriteSplittingRuleSegment createReadwriteSplittingRuleSegment(final String name,
                                                                              final String writeDataSource,
                                                                              final List<String> readDataSourceList,
                                                                              final String loadBalancerTypeName,
                                                                              final Properties properties) {
        return new ReadwriteSplittingRuleSegment(name, writeDataSource, readDataSourceList, loadBalancerTypeName, properties);
    }

    private ReadwriteSplittingRuleSegment createReadwriteSplittingRuleSegment(final String name,
                                                                              final String autoAwareResource,
                                                                              final String loadBalancer,
                                                                              final Properties properties) {
        return new ReadwriteSplittingRuleSegment(name, autoAwareResource, loadBalancer, properties);
    }
}
