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

package org.apache.shardingsphere.infra.config.rule.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuleConfigurationValidatorTest {
    
    @Test
    void assertValidateRuleConfiguration() {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(new FixtureRuleConfiguration("fixture")));
    }
    
    @Test
    void assertValidateRuleConfigurationWithViolation() {
        InvalidRuleConfigurationException actual = assertThrows(InvalidRuleConfigurationException.class,
                () -> RuleConfigurationValidator.validate(new FixtureRuleConfiguration("")));
        assertThat(actual.getMessage(), containsString("Invalid 'FixtureRuleConfiguration' rule"));
        assertThat(actual.getMessage(), containsString("Property `nested.name`"));
    }
    
    @Test
    void assertValidateRuleConfigurationsWithViolation() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> RuleConfigurationValidator.validate(Arrays.asList(new FixtureRuleConfiguration("fixture"), new FixtureRuleConfiguration(""))));
    }
    
    @Test
    void assertValidateRuleItemWithViolation() {
        InvalidRuleConfigurationException actual = assertThrows(InvalidRuleConfigurationException.class,
                () -> RuleConfigurationValidator.validateRuleItem(new FixtureRuleConfiguration("fixture"), new NestedConfiguration("")));
        assertThat(actual.getMessage(), containsString("Invalid 'FixtureRuleConfiguration' rule"));
        assertThat(actual.getMessage(), containsString("Property `name`"));
    }
    
    @Getter
    private static final class FixtureRuleConfiguration implements RuleConfiguration {
        
        @Valid
        private final NestedConfiguration nested;
        
        private FixtureRuleConfiguration(final String name) {
            nested = new NestedConfiguration(name);
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class NestedConfiguration {
        
        @NotBlank
        private final String name;
    }
}
