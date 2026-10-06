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

package org.apache.shardingsphere.parser.config;

import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SQLParserRuleConfigurationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final SQLParserRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Zero capacities", new SQLParserRuleConfiguration(new CacheOption(0, 0L), new CacheOption(0, 0L))),
                Arguments.of("Positive capacities", new SQLParserRuleConfiguration(new CacheOption(1, 1L), new CacheOption(1, 1L))),
                Arguments.of("Mixed capacities", new SQLParserRuleConfiguration(new CacheOption(0, 1L), new CacheOption(1, 0L))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final SQLParserRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Null parse tree cache", new SQLParserRuleConfiguration(null, new CacheOption(0, 0L))),
                Arguments.of("Null SQL statement cache", new SQLParserRuleConfiguration(new CacheOption(0, 0L), null)),
                Arguments.of("Negative parse tree initial capacity", new SQLParserRuleConfiguration(new CacheOption(-1, 0L), new CacheOption(0, 0L))),
                Arguments.of("Negative parse tree maximum size", new SQLParserRuleConfiguration(new CacheOption(0, -1L), new CacheOption(0, 0L))),
                Arguments.of("Negative SQL statement initial capacity", new SQLParserRuleConfiguration(new CacheOption(0, 0L), new CacheOption(-1, 0L))),
                Arguments.of("Negative SQL statement maximum size", new SQLParserRuleConfiguration(new CacheOption(0, 0L), new CacheOption(0, -1L))));
    }
}
