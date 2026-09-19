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

package org.apache.shardingsphere.authority.config;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorityRuleConfigurationTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final AuthorityRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        AlgorithmConfiguration privilegeProvider = createAlgorithmConfiguration("FIXTURE");
        UserConfiguration userConfig = new UserConfiguration("foo_user", null, "%", null, false);
        return Stream.of(
                Arguments.of("Empty users and authenticators", new AuthorityRuleConfiguration(
                        Collections.emptyList(), privilegeProvider, Collections.emptyMap(), null)),
                Arguments.of("User with nullable optional values", new AuthorityRuleConfiguration(
                        Collections.singleton(userConfig), privilegeProvider, Collections.emptyMap(), null)),
                Arguments.of("User with null hostname", createRuleConfiguration(new UserConfiguration("foo_user", null, null, null, false))),
                Arguments.of("User with empty hostname", createRuleConfiguration(new UserConfiguration("foo_user", null, "", null, false))),
                Arguments.of("Complete configuration", new AuthorityRuleConfiguration(Collections.singleton(
                        new UserConfiguration("foo_admin", "foo_password", "localhost", "foo_authenticator", true)), privilegeProvider,
                        Collections.singletonMap("foo_authenticator", createAlgorithmConfiguration("MD5")), "foo_authenticator")));
    }
    
    private static AlgorithmConfiguration createAlgorithmConfiguration(final String type) {
        return new AlgorithmConfiguration(type, new Properties());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final AuthorityRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        AlgorithmConfiguration privilegeProvider = createAlgorithmConfiguration("FIXTURE");
        AlgorithmConfiguration authenticator = createAlgorithmConfiguration("MD5");
        return Stream.of(
                Arguments.of("Null users", new AuthorityRuleConfiguration(null, privilegeProvider, Collections.emptyMap(), null)),
                Arguments.of("Null user", new AuthorityRuleConfiguration(Collections.singleton(null), privilegeProvider, Collections.emptyMap(), null)),
                Arguments.of("Null username", createRuleConfiguration(new UserConfiguration(null, null, "%", null, false))),
                Arguments.of("Empty username", createRuleConfiguration(new UserConfiguration("", null, "%", null, false))),
                Arguments.of("Blank username", createRuleConfiguration(new UserConfiguration(" ", null, "%", null, false))),
                Arguments.of("Null privilege provider", new AuthorityRuleConfiguration(Collections.emptyList(), null, Collections.emptyMap(), null)),
                Arguments.of("Missing privilege provider", new AuthorityRuleConfiguration(
                        Collections.emptyList(), createAlgorithmConfiguration("MISSING"), Collections.emptyMap(), null)),
                Arguments.of("Null authenticators", new AuthorityRuleConfiguration(Collections.emptyList(), privilegeProvider, null, null)),
                Arguments.of("Null authenticator name", new AuthorityRuleConfiguration(
                        Collections.emptyList(), privilegeProvider, Collections.singletonMap(null, authenticator), null)),
                Arguments.of("Empty authenticator name", new AuthorityRuleConfiguration(
                        Collections.emptyList(), privilegeProvider, Collections.singletonMap("", authenticator), null)),
                Arguments.of("Blank authenticator name", new AuthorityRuleConfiguration(
                        Collections.emptyList(), privilegeProvider, Collections.singletonMap(" ", authenticator), null)),
                Arguments.of("Null authenticator", new AuthorityRuleConfiguration(
                        Collections.emptyList(), privilegeProvider, Collections.singletonMap("foo_authenticator", null), null)));
    }
    
    private static AuthorityRuleConfiguration createRuleConfiguration(final UserConfiguration userConfig) {
        return new AuthorityRuleConfiguration(Collections.singleton(userConfig), createAlgorithmConfiguration("FIXTURE"), Collections.emptyMap(), null);
    }
}
