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

package org.apache.shardingsphere.transaction.config;

import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.config.rule.validator.group.RuleConfigurationTypeValidationGroup;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionRuleConfigurationTest {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validXAConfigurationArguments")
    void assertValidateValidXAConfiguration(final String name, final TransactionRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validXAConfigurationArguments() {
        return Stream.of(
                Arguments.of("Provider type", new TransactionRuleConfiguration("XA", "Atomikos", null)),
                Arguments.of("Case insensitive provider type", new TransactionRuleConfiguration("xa", "atomikos", null)),
                Arguments.of("Default provider", new TransactionRuleConfiguration("XA", null, null)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidXAConfigurationArguments")
    void assertValidateInvalidXAConfiguration(final String name, final TransactionRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidXAConfigurationArguments() {
        return Stream.of(Arguments.of("Missing provider", new TransactionRuleConfiguration("XA", "MISSING", null)));
    }
    
    @Test
    void assertInvalidProviderTypePropertyPath() {
        Collection<ConstraintViolation<TransactionRuleConfiguration>> actual = VALIDATOR.validate(
                new TransactionRuleConfiguration("XA", "MISSING", null), RuleConfigurationTypeValidationGroup.class);
        assertThat(actual.iterator().next().getPropertyPath().toString(), is("providerType"));
    }
}
