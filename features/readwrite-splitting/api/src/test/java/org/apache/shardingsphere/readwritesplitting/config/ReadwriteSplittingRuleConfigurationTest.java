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

package org.apache.shardingsphere.readwritesplitting.config;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadwriteSplittingRuleConfigurationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_group", "foo_write", Arrays.asList("foo_read_0", "foo_read_1"), TransactionalReadQueryStrategy.PRIMARY, "foo_load_balancer");
        return Stream.of(
                Arguments.of("Empty configuration", new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap())),
                Arguments.of("Group without load balancer", new ReadwriteSplittingRuleConfiguration(Collections.singleton(
                        new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                                "foo_group", "foo_write", Collections.singletonList("foo_read"), null)),
                        Collections.emptyMap())),
                Arguments.of("Group with empty load balancer name", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "foo_group", "foo_write", Collections.singletonList("foo_read"), ""))),
                Arguments.of("Complete configuration", new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceGroupConfig),
                        Collections.singletonMap("foo_load_balancer", new AlgorithmConfiguration("FIXTURE", new Properties())))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    @Test
    void assertUnconfiguredLoadBalancerViolation() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_group", "foo_write", Collections.singletonList("foo_read"), "bar_load_balancer"));
        InvalidRuleConfigurationException actual = assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
        assertThat(actual.getMessage(),
                is("Invalid 'ReadwriteSplittingRuleConfiguration' rule, error message is: Property `dataSourceGroups` references unconfigured loadBalancers `bar_load_balancer`."));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        AlgorithmConfiguration loadBalancer = new AlgorithmConfiguration("FIXTURE", new Properties());
        return Stream.of(
                Arguments.of("Null data source groups", new ReadwriteSplittingRuleConfiguration(null, Collections.emptyMap())),
                Arguments.of("Null data source group", new ReadwriteSplittingRuleConfiguration(Collections.singleton(null), Collections.emptyMap())),
                Arguments.of("Null load balancers", new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), null)),
                Arguments.of("Blank load balancer name", new ReadwriteSplittingRuleConfiguration(
                        Collections.emptyList(), Collections.singletonMap("", loadBalancer))),
                Arguments.of("Null load balancer", new ReadwriteSplittingRuleConfiguration(
                        Collections.emptyList(), Collections.singletonMap("foo_load_balancer", null))),
                Arguments.of("Missing load balancer type", new ReadwriteSplittingRuleConfiguration(
                        Collections.emptyList(), Collections.singletonMap("foo_load_balancer", new AlgorithmConfiguration("MISSING", new Properties())))),
                Arguments.of("Unconfigured group load balancer", new ReadwriteSplittingRuleConfiguration(Collections.singleton(
                        new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                                "foo_group", "foo_write", Collections.singletonList("foo_read"), TransactionalReadQueryStrategy.PRIMARY, "bar_load_balancer")),
                        Collections.singletonMap("foo_load_balancer", loadBalancer))),
                Arguments.of("Blank group name", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "", "foo_write", Collections.singletonList("foo_read"), TransactionalReadQueryStrategy.PRIMARY, null))),
                Arguments.of("Blank write data source", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "foo_group", "", Collections.singletonList("foo_read"), TransactionalReadQueryStrategy.PRIMARY, null))),
                Arguments.of("Null read data sources", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "foo_group", "foo_write", null, TransactionalReadQueryStrategy.PRIMARY, null))),
                Arguments.of("Empty read data sources", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "foo_group", "foo_write", Collections.emptyList(), TransactionalReadQueryStrategy.PRIMARY, null))),
                Arguments.of("Blank read data source", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "foo_group", "foo_write", Collections.singletonList(""), TransactionalReadQueryStrategy.PRIMARY, null))),
                Arguments.of("Null transactional read query strategy", createRuleConfiguration(new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                        "foo_group", "foo_write", Collections.singletonList("foo_read"), null, null))));
    }
    
    private static ReadwriteSplittingRuleConfiguration createRuleConfiguration(final ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig) {
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceGroupConfig), Collections.emptyMap());
    }
}
