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

package org.apache.shardingsphere.mask.config;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleConfigurationTest {
    
    @Test
    void assertGetLogicTableNames() {
        Collection<String> actual = new MaskRuleConfiguration(Arrays.asList(
                new MaskTableRuleConfiguration("foo_tbl", Collections.emptyList()), new MaskTableRuleConfiguration("bar_tbl", Collections.emptyList())), null).getLogicTableNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("foo_tbl"));
        assertTrue(actual.contains("bar_tbl"));
        assertTrue(actual.contains("FOO_TBL"));
        assertTrue(actual.contains("BAR_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final MaskRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        MaskColumnRuleConfiguration columnConfig = new MaskColumnRuleConfiguration("foo_col", "foo_mask");
        MaskTableRuleConfiguration tableConfig = new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(columnConfig));
        return Stream.of(
                Arguments.of("Empty configuration", new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap())),
                Arguments.of("Table without columns", new MaskRuleConfiguration(
                        Collections.singleton(new MaskTableRuleConfiguration("foo_tbl", Collections.emptyList())), Collections.emptyMap())),
                Arguments.of("Complete configuration", new MaskRuleConfiguration(Collections.singleton(tableConfig),
                        Collections.singletonMap("foo_mask", new AlgorithmConfiguration("FIXTURE", new Properties())))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final MaskRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        return Stream.of(
                Arguments.of("Null tables", new MaskRuleConfiguration(null, Collections.emptyMap())),
                Arguments.of("Null table", new MaskRuleConfiguration(Collections.singleton(null), Collections.emptyMap())),
                Arguments.of("Null mask algorithms", new MaskRuleConfiguration(Collections.emptyList(), null)),
                Arguments.of("Blank mask algorithm name", new MaskRuleConfiguration(Collections.emptyList(), Collections.singletonMap("", algorithmConfig))),
                Arguments.of("Null mask algorithm", new MaskRuleConfiguration(Collections.emptyList(), Collections.singletonMap("foo_mask", null))),
                Arguments.of("Missing mask algorithm type", new MaskRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_mask", new AlgorithmConfiguration("MISSING", new Properties())))),
                Arguments.of("Blank table name", new MaskRuleConfiguration(
                        Collections.singleton(new MaskTableRuleConfiguration("", Collections.emptyList())), Collections.emptyMap())),
                Arguments.of("Null columns", new MaskRuleConfiguration(
                        Collections.singleton(new MaskTableRuleConfiguration("foo_tbl", null)), Collections.emptyMap())),
                Arguments.of("Null column", new MaskRuleConfiguration(
                        Collections.singleton(new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(null))), Collections.emptyMap())),
                Arguments.of("Blank logic column", createRuleConfiguration(new MaskColumnRuleConfiguration("", "foo_mask"))),
                Arguments.of("Blank column mask algorithm", createRuleConfiguration(new MaskColumnRuleConfiguration("foo_col", ""))));
    }
    
    private static MaskRuleConfiguration createRuleConfiguration(final MaskColumnRuleConfiguration columnConfig) {
        MaskTableRuleConfiguration tableConfig = new MaskTableRuleConfiguration("foo_tbl", Collections.singleton(columnConfig));
        return new MaskRuleConfiguration(Collections.singleton(tableConfig), Collections.emptyMap());
    }
}
