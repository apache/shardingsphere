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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UnusedAlgorithmFinderTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("findUnusedShadowAlgorithmArguments")
    void assertFindUnusedShadowAlgorithm(final String name, final ShadowRuleConfiguration ruleConfig, final Collection<String> expected) {
        assertThat(new LinkedHashSet<>(UnusedAlgorithmFinder.findUnusedShadowAlgorithm(ruleConfig)), is(new LinkedHashSet<>(expected)));
    }
    
    private static Stream<Arguments> findUnusedShadowAlgorithmArguments() {
        return Stream.of(
                Arguments.of("table references one algorithm", createRuleConfiguration(Collections.singleton("algorithm_used"), null, Arrays.asList("algorithm_used", "algorithm_unused")),
                        Collections.singleton("algorithm_unused")),
                Arguments.of("table and default algorithm are both in use",
                        createRuleConfiguration(Collections.singleton("algorithm_used"), "algorithm_default", Arrays.asList("algorithm_used", "algorithm_default", "algorithm_unused")),
                        Collections.singleton("algorithm_unused")),
                Arguments.of("default algorithm only", createRuleConfiguration(Collections.emptyList(), "algorithm_default", Arrays.asList("algorithm_default", "algorithm_unused")),
                        Collections.singleton("algorithm_unused")));
    }
    
    private static ShadowRuleConfiguration createRuleConfiguration(final Collection<String> tableAlgorithmNames, final String defaultAlgorithmName, final Collection<String> algorithmNames) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.singletonList("ds_0"), tableAlgorithmNames));
        result.setDefaultShadowAlgorithmName(defaultAlgorithmName);
        for (String each : algorithmNames) {
            result.getShadowAlgorithms().put(each, new AlgorithmConfiguration("SQL_HINT", new Properties()));
        }
        return result;
    }
}
