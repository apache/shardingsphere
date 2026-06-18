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

package org.apache.shardingsphere.shadow.distsql.handler.supporter;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShadowRuleStatementSupporterTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getRuleNamesByConfigurationArguments")
    void assertGetRuleNamesByConfiguration(final String name, final ShadowRuleConfiguration ruleConfig, final List<String> expected) {
        assertThat(ShadowRuleStatementSupporter.getRuleNames(ruleConfig), is(expected));
    }
    
    @Test
    void assertGetRuleNamesBySegmentsWhenEmpty() {
        assertThat(ShadowRuleStatementSupporter.getRuleNames(Collections.emptyList()), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetRuleNamesBySegmentsWhenPresent() {
        Collection<ShadowRuleSegment> segments = Arrays.asList(
                new ShadowRuleSegment("ds_0", "source_0", "shadow_0", Collections.emptyMap()), new ShadowRuleSegment("ds_1", "source_1", "shadow_1", Collections.emptyMap()));
        assertThat(ShadowRuleStatementSupporter.getRuleNames(segments), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    void assertGetStorageUnitNamesWhenEmpty() {
        assertThat(ShadowRuleStatementSupporter.getStorageUnitNames(Collections.emptyList()), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetStorageUnitNamesWhenPresent() {
        Collection<ShadowRuleSegment> segments = Arrays.asList(
                new ShadowRuleSegment("ds_0", "source_0", "shadow_0", Collections.emptyMap()), new ShadowRuleSegment("ds_1", null, "shadow_1", Collections.emptyMap()));
        assertThat(ShadowRuleStatementSupporter.getStorageUnitNames(segments), is(Arrays.asList("source_0", "shadow_0", "shadow_1")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAlgorithmNamesByConfigurationArguments")
    void assertGetAlgorithmNamesByConfiguration(final String name, final ShadowRuleConfiguration ruleConfig, final List<String> expected) {
        assertThat(ShadowRuleStatementSupporter.getAlgorithmNames(ruleConfig), is(expected));
    }
    
    @Test
    void assertGetAlgorithmNamesBySegmentsWhenEmpty() {
        assertThat(ShadowRuleStatementSupporter.getAlgorithmNames(Collections.emptyList()), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetAlgorithmNamesBySegmentsWhenPresent() {
        Collection<ShadowAlgorithmSegment> segments = Arrays.asList(
                new ShadowAlgorithmSegment("algorithm_0", new AlgorithmSegment("SQL_HINT", new Properties())),
                new ShadowAlgorithmSegment("algorithm_1", new AlgorithmSegment("SQL_HINT", new Properties())));
        assertThat(ShadowRuleStatementSupporter.getAlgorithmNames(
                Collections.singleton(new ShadowRuleSegment("ds_0", "source_0", "shadow_0", Collections.singletonMap("t_order", segments)))), is(Arrays.asList("algorithm_0", "algorithm_1")));
    }
    
    @Test
    void assertMergeConfiguration() {
        ShadowTableConfiguration existingConfig = new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("ds_0")), new ArrayList<>(Collections.singleton("algorithm_0")));
        ShadowTableConfiguration newConfig = new ShadowTableConfiguration(new LinkedList<>(Collections.singleton("ds_1")), new LinkedList<>(Collections.singleton("algorithm_1")));
        ShadowTableConfiguration actual = ShadowRuleStatementSupporter.mergeConfiguration(existingConfig, newConfig);
        assertThat(actual.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
        assertThat(actual.getShadowAlgorithmNames(), is(Arrays.asList("algorithm_0", "algorithm_1")));
    }
    
    private static Stream<Arguments> getRuleNamesByConfigurationArguments() {
        return Stream.of(
                Arguments.of("null configuration", null, Collections.emptyList()),
                Arguments.of("single datasource", createRuleConfiguration(Collections.singleton("ds_0"), Collections.emptyList()), Collections.singletonList("ds_0")),
                Arguments.of("multiple datasources", createRuleConfiguration(Arrays.asList("ds_0", "ds_1"), Collections.emptyList()), Arrays.asList("ds_0", "ds_1")));
    }
    
    private static Stream<Arguments> getAlgorithmNamesByConfigurationArguments() {
        return Stream.of(
                Arguments.of("null configuration", null, Collections.emptyList()),
                Arguments.of("single algorithm", createRuleConfiguration(Collections.emptyList(), Collections.singleton("algorithm_0")), Collections.singletonList("algorithm_0")),
                Arguments.of("multiple algorithms", createRuleConfiguration(Collections.emptyList(), Arrays.asList("algorithm_0", "algorithm_1")), Arrays.asList("algorithm_0", "algorithm_1")));
    }
    
    private static ShadowRuleConfiguration createRuleConfiguration(final Collection<String> dataSourceNames, final Collection<String> algorithmNames) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        for (String each : dataSourceNames) {
            result.getDataSources().add(new ShadowDataSourceConfiguration(each, each + "_source", each + "_shadow"));
        }
        for (String each : algorithmNames) {
            result.getShadowAlgorithms().put(each, new AlgorithmConfiguration("SQL_HINT", new Properties()));
        }
        return result;
    }
}
