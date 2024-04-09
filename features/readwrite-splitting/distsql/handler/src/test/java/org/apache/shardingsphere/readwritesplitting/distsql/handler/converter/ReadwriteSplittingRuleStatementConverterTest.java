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

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingRuleStatementConverterTest {
    
    @Test
    void assertEmptyRuleSegmentConvertResult() {
        ReadwriteSplittingRuleConfiguration actualEmptyRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.emptyList());
        assertTrue(actualEmptyRuleSegmentConvertResult.getDataSources().isEmpty());
        assertTrue(actualEmptyRuleSegmentConvertResult.getLoadBalancers().isEmpty());
    }
    
    @Test
    void assertSingleRuleSegmentConvertResult() {
        ReadwriteSplittingRuleSegment expectedSingleReadwriteSplittingRuleSegment = createReadwriteSplittingRuleSegment("write_ds", Arrays.asList("read_ds_01", "read_ds_02"),
                "static_load_balancer_type", new Properties());
        ReadwriteSplittingRuleConfiguration actualSingleRuleSegmentConvertResult = ReadwriteSplittingRuleStatementConverter
                .convert(Collections.singleton(expectedSingleReadwriteSplittingRuleSegment));
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> actualSingleRuleSegmentConvertResultDataSources = actualSingleRuleSegmentConvertResult.getDataSources();
        Map<String, AlgorithmConfiguration> actualSingleRuleSegmentConvertResultLoadBalancers = actualSingleRuleSegmentConvertResult.getLoadBalancers();
        assertThat(actualSingleRuleSegmentConvertResultDataSources.size(), is(1));
        assertThat(actualSingleRuleSegmentConvertResultLoadBalancers.size(), is(1));
        ReadwriteSplittingDataSourceRuleConfiguration actualRuleConfig = actualSingleRuleSegmentConvertResultDataSources.iterator().next();
        assertThat(actualRuleConfig.getName(), is(expectedSingleReadwriteSplittingRuleSegment.getName()));
        String expectedLoadBalancerName = String.format("%s_%s", expectedSingleReadwriteSplittingRuleSegment.getName(), expectedSingleReadwriteSplittingRuleSegment.getLoadBalancer().getName());
        assertThat(actualRuleConfig.getLoadBalancerName(), is(expectedLoadBalancerName));
        assertThat(actualRuleConfig.getWriteDataSourceName(), is(expectedSingleReadwriteSplittingRuleSegment.getWriteDataSource()));
        assertThat(actualRuleConfig.getReadDataSourceNames(), is(expectedSingleReadwriteSplittingRuleSegment.getReadDataSources()));
        String actualLoadBalancerName = actualSingleRuleSegmentConvertResultLoadBalancers.keySet().iterator().next();
        assertThat(actualLoadBalancerName, is(expectedLoadBalancerName));
        AlgorithmConfiguration actualSphereAlgorithmConfig = actualSingleRuleSegmentConvertResultLoadBalancers.get(actualLoadBalancerName);
        assertThat(actualSphereAlgorithmConfig.getType(), is(expectedSingleReadwriteSplittingRuleSegment.getLoadBalancer().getName()));
        assertThat(actualSphereAlgorithmConfig.getProps(), is(expectedSingleReadwriteSplittingRuleSegment.getLoadBalancer().getProps()));
    }
    
    private ReadwriteSplittingRuleSegment createReadwriteSplittingRuleSegment(final String writeDataSource, final List<String> readDataSources,
                                                                              final String loadBalancerTypeName, final Properties props) {
        return new ReadwriteSplittingRuleSegment("", writeDataSource, readDataSources, new AlgorithmSegment(loadBalancerTypeName, props));
    }
}
