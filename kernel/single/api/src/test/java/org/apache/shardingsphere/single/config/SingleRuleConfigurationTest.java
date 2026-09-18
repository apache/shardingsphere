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

package org.apache.shardingsphere.single.config;

import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleRuleConfigurationTest {
    
    @Test
    void assertGetPresentDefaultDataSource() {
        Optional<String> actual = new SingleRuleConfiguration(Collections.emptyList(), "default_db").getDefaultDataSource();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("default_db"));
    }
    
    @Test
    void assertGetAbsentDefaultDataSource() {
        assertFalse(new SingleRuleConfiguration().getDefaultDataSource().isPresent());
    }
    
    @Test
    void assertGetLogicTableNames() {
        Collection<String> actual = new SingleRuleConfiguration(Arrays.asList("foo_db.foo_tbl", "bar_db.bar_tbl"), null).getLogicTableNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("foo_tbl"));
        assertTrue(actual.contains("bar_tbl"));
        assertTrue(actual.contains("FOO_TBL"));
        assertTrue(actual.contains("BAR_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final SingleRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Empty tables", new SingleRuleConfiguration(Collections.emptyList(), null)),
                Arguments.of("Single table", new SingleRuleConfiguration(Collections.singleton("foo_db.foo_tbl"), "foo_db")),
                Arguments.of("Multiple tables", new SingleRuleConfiguration(Arrays.asList("foo_db.foo_tbl", "bar_db.bar_tbl"), null)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final SingleRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Null tables", new SingleRuleConfiguration(null, null)),
                Arguments.of("Null table", new SingleRuleConfiguration(Collections.singleton(null), null)),
                Arguments.of("Empty table", new SingleRuleConfiguration(Collections.singleton(""), null)),
                Arguments.of("Blank table", new SingleRuleConfiguration(Collections.singleton(" "), null)));
    }
}
