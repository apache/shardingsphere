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

package org.apache.shardingsphere.sqlfederation.config;

import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SQLFederationRuleConfigurationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final SQLFederationRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Minimum cache values", new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(1, 1L))),
                Arguments.of("Enabled SQL federation", new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(2, 2L))),
                Arguments.of("All queries use SQL federation", new SQLFederationRuleConfiguration(true, true, new SQLFederationCacheOption(16, 1024L))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final SQLFederationRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Null execution plan cache", new SQLFederationRuleConfiguration(false, false, null)),
                Arguments.of("Zero initial capacity", new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(0, 1L))),
                Arguments.of("Negative initial capacity", new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(-1, 1L))),
                Arguments.of("Zero maximum size", new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(1, 0L))),
                Arguments.of("Negative maximum size", new SQLFederationRuleConfiguration(false, false, new SQLFederationCacheOption(1, -1L))));
    }
}
