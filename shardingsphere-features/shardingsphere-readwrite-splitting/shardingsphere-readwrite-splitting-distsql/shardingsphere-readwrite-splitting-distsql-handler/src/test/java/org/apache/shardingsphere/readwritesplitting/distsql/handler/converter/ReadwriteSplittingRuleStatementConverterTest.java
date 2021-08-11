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
    public void convertTest() {
        ReadwriteSplittingRuleConfiguration emptyRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.emptyList());
        assertEmptyRuleSegmentConvertResult(emptyRuleSegmentConvertResult);
        Properties properties1 = new Properties();
        properties1.setProperty("ping", "pong");
        ReadwriteSplittingRuleSegment singleReadwriteSplittingRuleSegment = createReadwriteSplittingRuleSegment("name1", "write_ds_01",
                Arrays.asList("read_ds_01", "read_ds_02"), "lb01", properties1);
        ReadwriteSplittingRuleConfiguration singleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.singleton(singleReadwriteSplittingRuleSegment));
        assertSingleRuleSegmentConvertResult(singleReadwriteSplittingRuleSegment, singleRuleSegmentConvertResult);
        Properties properties2 = new Properties();
        properties2.setProperty("ping1", "pong1");
        Properties properties3 = new Properties();
        properties3.setProperty("ping2", "pong2");
        Properties properties4 = new Properties();
        properties4.setProperty("ping3", "pong3");
        properties4.setProperty("ping4", "pong4");
        List<ReadwriteSplittingRuleSegment> multipleReadwriteSplittingRuleSegments = Arrays.asList(
                createReadwriteSplittingRuleSegment("name2", "write_ds_02", Arrays.asList("read_ds_02", "read_ds_03"), "lb02", properties2),
                createReadwriteSplittingRuleSegment("name3", "write_ds_03", Arrays.asList("read_ds_04", "read_ds_05"), "lb03", properties3),
                createReadwriteSplittingRuleSegment("name4", "autoAwareResource", "lb04", properties4));
        ReadwriteSplittingRuleConfiguration multipleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter.convert(multipleReadwriteSplittingRuleSegments);
        assertMultipleReadwriteSplittingRuleSegments(multipleReadwriteSplittingRuleSegments, multipleRuleSegmentConvertResult);
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

    private void assertEmptyRuleSegmentConvertResult(final ReadwriteSplittingRuleConfiguration emptyRuleSegmentConvertResult) {
        assertThat(emptyRuleSegmentConvertResult.getDataSources(), org.hamcrest.Matchers.empty());
        assertThat(emptyRuleSegmentConvertResult.getLoadBalancers().size(), org.hamcrest.Matchers.is(0));
    }

    private void assertSingleRuleSegmentConvertResult(final ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment,
                                                      final ReadwriteSplittingRuleConfiguration singleRuleSegmentConvertResult) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> singleRuleSegmentConvertResultDataSources = singleRuleSegmentConvertResult.getDataSources();
        Map<String, ShardingSphereAlgorithmConfiguration> singleRuleSegmentConvertResultLoadBalancers = singleRuleSegmentConvertResult.getLoadBalancers();
        assertThat(singleRuleSegmentConvertResultDataSources.size(), org.hamcrest.Matchers.is(1));
        assertThat(singleRuleSegmentConvertResultLoadBalancers.size(), org.hamcrest.Matchers.is(1));
        Object[] dataSources = singleRuleSegmentConvertResultDataSources.toArray();
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfiguration = (ReadwriteSplittingDataSourceRuleConfiguration) dataSources[0];
        assertThat(readwriteSplittingRuleSegment.getName(), org.hamcrest.Matchers.is(ruleConfiguration.getName()));
        assertThat(ruleConfiguration.getLoadBalancerName(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
        assertThat(readwriteSplittingRuleSegment.getWriteDataSource(), org.hamcrest.Matchers.is(ruleConfiguration.getWriteDataSourceName()));
        assertThat(readwriteSplittingRuleSegment.getReadDataSources() == null ? Collections.emptyList() : readwriteSplittingRuleSegment.getReadDataSources(),
                org.hamcrest.Matchers.equalTo(ruleConfiguration.getReadDataSourceNames()));
        String loadBalancerName = singleRuleSegmentConvertResultLoadBalancers.keySet().toArray()[0].toString();
        assertThat(loadBalancerName, org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
        ShardingSphereAlgorithmConfiguration sphereAlgorithmConfiguration = (ShardingSphereAlgorithmConfiguration) singleRuleSegmentConvertResultLoadBalancers.values().toArray()[0];
        assertThat(sphereAlgorithmConfiguration.getType(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
        assertThat(sphereAlgorithmConfiguration.getProps(), org.hamcrest.Matchers.is(readwriteSplittingRuleSegment.getProps()));
    }

    private void assertMultipleReadwriteSplittingRuleSegments(final List<ReadwriteSplittingRuleSegment> readwriteSplittingRuleSegmentList,
                                                              final ReadwriteSplittingRuleConfiguration multipleReadwriteSplittingRuleSegments) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> multipleRuleSegmentConvertResultDataSources = multipleReadwriteSplittingRuleSegments.getDataSources();
        Map<String, ShardingSphereAlgorithmConfiguration> multipleRuleSegmentConvertResultLoadBalancers = multipleReadwriteSplittingRuleSegments.getLoadBalancers();
        assertThat(multipleRuleSegmentConvertResultDataSources.size(), org.hamcrest.Matchers.is(readwriteSplittingRuleSegmentList.size()));
        assertThat(multipleRuleSegmentConvertResultLoadBalancers.size(), org.hamcrest.Matchers.is(readwriteSplittingRuleSegmentList.size()));
        ReadwriteSplittingDataSourceRuleConfiguration[] ruleConfigurations = new ReadwriteSplittingDataSourceRuleConfiguration[readwriteSplittingRuleSegmentList.size()];
        multipleRuleSegmentConvertResultDataSources.toArray(ruleConfigurations);
        Stream.iterate(0, i -> i + 1)
                .limit(readwriteSplittingRuleSegmentList.size())
                .forEach(i -> {
                    ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment = readwriteSplittingRuleSegmentList.get(i);
                    ReadwriteSplittingDataSourceRuleConfiguration ruleConfiguration = ruleConfigurations[i];
                    assertThat(readwriteSplittingRuleSegment.getName(), org.hamcrest.Matchers.is(ruleConfiguration.getName()));
                    assertThat(ruleConfiguration.getLoadBalancerName(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
                    assertThat(readwriteSplittingRuleSegment.getWriteDataSource(), org.hamcrest.Matchers.is(ruleConfiguration.getWriteDataSourceName()));
                    assertThat(readwriteSplittingRuleSegment.getReadDataSources() == null ? Collections.emptyList() : readwriteSplittingRuleSegment.getReadDataSources(),
                            org.hamcrest.Matchers.equalTo(ruleConfiguration.getReadDataSourceNames()));
                    assertThat(multipleRuleSegmentConvertResultLoadBalancers, org.hamcrest.Matchers.hasKey(ruleConfiguration.getLoadBalancerName()));
                    ShardingSphereAlgorithmConfiguration sphereAlgorithmConfiguration = multipleRuleSegmentConvertResultLoadBalancers.get(ruleConfiguration.getLoadBalancerName());
                    assertThat(sphereAlgorithmConfiguration.getType(), org.hamcrest.Matchers.containsString(readwriteSplittingRuleSegment.getLoadBalancer()));
                    assertThat(sphereAlgorithmConfiguration.getProps(), org.hamcrest.Matchers.is(readwriteSplittingRuleSegment.getProps()));
                });
    }
}
