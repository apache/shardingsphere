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

package org.apache.shardingsphere.shadow.config;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShadowRuleConfigurationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final ShadowRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        ShadowRuleConfiguration defaultAlgorithmRuleConfig = createRuleConfiguration(
                Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "foo_primary", "foo_shadow")), Collections.emptyMap(),
                Collections.singletonMap("foo_shadow_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        defaultAlgorithmRuleConfig.setDefaultShadowAlgorithmName("foo_shadow_algorithm");
        return Stream.of(
                Arguments.of("Empty configuration", new ShadowRuleConfiguration()),
                Arguments.of("Data source configuration", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "foo_primary", "foo_shadow")),
                        Collections.emptyMap(), Collections.emptyMap())),
                Arguments.of("Complete configuration", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "foo_primary", "foo_shadow")),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(
                                Collections.singleton("foo_ds"), Collections.singleton("foo_shadow_algorithm"))),
                        Collections.singletonMap("foo_shadow_algorithm", new AlgorithmConfiguration("FIXTURE", new Properties())))),
                Arguments.of("Default SQL hint algorithm", defaultAlgorithmRuleConfig));
    }
    
    private static ShadowRuleConfiguration createRuleConfiguration(final Collection<ShadowDataSourceConfiguration> dataSources,
                                                                   final Map<String, ShadowTableConfiguration> tables,
                                                                   final Map<String, AlgorithmConfiguration> shadowAlgorithms) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setDataSources(dataSources);
        result.setTables(tables);
        result.setShadowAlgorithms(shadowAlgorithms);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final ShadowRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        ShadowRuleConfiguration missingDefaultAlgorithm = createRuleConfiguration(Collections.emptyList(), Collections.emptyMap(),
                Collections.singletonMap("foo_shadow_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties())));
        missingDefaultAlgorithm.setDefaultShadowAlgorithmName("bar_shadow_algorithm");
        ShadowRuleConfiguration invalidDefaultAlgorithmType = createRuleConfiguration(
                Collections.emptyList(), Collections.emptyMap(), Collections.singletonMap("foo_shadow_algorithm", algorithmConfig));
        invalidDefaultAlgorithmType.setDefaultShadowAlgorithmName("foo_shadow_algorithm");
        return Stream.of(
                Arguments.of("Null data sources", createRuleConfiguration(null, Collections.emptyMap(), Collections.emptyMap())),
                Arguments.of("Null data source", createRuleConfiguration(Collections.singleton(null), Collections.emptyMap(), Collections.emptyMap())),
                Arguments.of("Blank data source name", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("", "foo_primary", "foo_shadow")),
                        Collections.emptyMap(), Collections.emptyMap())),
                Arguments.of("Blank production data source name", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "", "foo_shadow")),
                        Collections.emptyMap(), Collections.emptyMap())),
                Arguments.of("Blank shadow data source name", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "foo_primary", "")),
                        Collections.emptyMap(), Collections.emptyMap())),
                Arguments.of("Null tables", createRuleConfiguration(Collections.emptyList(), null, Collections.emptyMap())),
                Arguments.of("Blank table name", createRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("", new ShadowTableConfiguration(Collections.emptyList(), Collections.singleton("foo_shadow_algorithm"))), Collections.emptyMap())),
                Arguments.of("Null table", createRuleConfiguration(
                        Collections.emptyList(), Collections.singletonMap("foo_tbl", null), Collections.emptyMap())),
                Arguments.of("Null table data source names", createRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(null, Collections.singleton("foo_shadow_algorithm"))), Collections.emptyMap())),
                Arguments.of("Blank table data source name", createRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.singleton(""), Collections.singleton("foo_shadow_algorithm"))), Collections.emptyMap())),
                Arguments.of("Null shadow algorithm names", createRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.emptyList(), null)), Collections.emptyMap())),
                Arguments.of("Empty shadow algorithm names", createRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.emptyList(), Collections.emptyList())), Collections.emptyMap())),
                Arguments.of("Blank shadow algorithm name", createRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(Collections.emptyList(), Collections.singleton(""))), Collections.emptyMap())),
                Arguments.of("Null shadow algorithms", createRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), null)),
                Arguments.of("Blank shadow algorithm key", createRuleConfiguration(
                        Collections.emptyList(), Collections.emptyMap(), Collections.singletonMap("", algorithmConfig))),
                Arguments.of("Null shadow algorithm", createRuleConfiguration(
                        Collections.emptyList(), Collections.emptyMap(), Collections.singletonMap("foo_shadow_algorithm", null))),
                Arguments.of("Missing shadow algorithm type", createRuleConfiguration(Collections.emptyList(), Collections.emptyMap(),
                        Collections.singletonMap("foo_shadow_algorithm", new AlgorithmConfiguration("MISSING", new Properties())))),
                Arguments.of("Unconfigured table data source", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "foo_primary", "foo_shadow")),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(
                                Collections.singleton("bar_ds"), Collections.singleton("foo_shadow_algorithm"))),
                        Collections.singletonMap("foo_shadow_algorithm", algorithmConfig))),
                Arguments.of("Unconfigured table shadow algorithm", createRuleConfiguration(
                        Collections.singleton(new ShadowDataSourceConfiguration("foo_ds", "foo_primary", "foo_shadow")),
                        Collections.singletonMap("foo_tbl", new ShadowTableConfiguration(
                                Collections.singleton("foo_ds"), Collections.singleton("bar_shadow_algorithm"))),
                        Collections.singletonMap("foo_shadow_algorithm", algorithmConfig))),
                Arguments.of("Unconfigured default shadow algorithm", missingDefaultAlgorithm),
                Arguments.of("Default shadow algorithm without SQL hint type", invalidDefaultAlgorithmType));
    }
    
}
