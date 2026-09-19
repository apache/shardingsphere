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
import org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.SPITypeExists;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.RuleConfigurationValidationException;
import org.junit.jupiter.api.Test;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
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
        SQLException actualSQLException = actual.toSQLException();
        assertThat(actualSQLException.getSQLState(), is(XOpenSQLState.CHECK_OPTION_VIOLATION.getValue()));
        assertThat(actualSQLException.getErrorCode(), is(10200));
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
    
    @Test
    void assertValidateRuleConfigurationWithValidationException() {
        RuleConfigurationValidationException actual = assertThrows(RuleConfigurationValidationException.class,
                () -> RuleConfigurationValidator.validate(new MissingSPIClassRuleConfiguration()));
        assertThat(actual.getCause(), isA(ValidationException.class));
        SQLException actualSQLException = actual.toSQLException();
        assertThat(actualSQLException.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actualSQLException.getErrorCode(), is(10205));
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
    
    @Getter
    private static final class MissingSPIClassRuleConfiguration implements RuleConfiguration {
        
        @SPITypeExists(spiClassName = "org.apache.shardingsphere.infra.config.rule.validator.constraint.spi.fixture.MissingSPITypeFixture")
        private final String type = "FIXTURE";
    }
}
