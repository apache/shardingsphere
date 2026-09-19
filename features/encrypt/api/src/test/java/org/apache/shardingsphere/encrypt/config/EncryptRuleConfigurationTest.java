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

package org.apache.shardingsphere.encrypt.config;

import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.validator.RuleConfigurationValidator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
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

class EncryptRuleConfigurationTest {
    
    @Test
    void assertGetLogicTableNames() {
        Collection<String> actual = new EncryptRuleConfiguration(Arrays.asList(
                new EncryptTableRuleConfiguration("foo_tbl", Collections.emptyList()), new EncryptTableRuleConfiguration("bar_tbl", Collections.emptyList())), null).getLogicTableNames();
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("foo_tbl"));
        assertTrue(actual.contains("bar_tbl"));
        assertTrue(actual.contains("FOO_TBL"));
        assertTrue(actual.contains("BAR_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validRuleConfigurationArguments")
    void assertValidateValidRuleConfiguration(final String name, final EncryptRuleConfiguration ruleConfig) {
        assertDoesNotThrow(() -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> validRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("Empty configuration", new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap())),
                Arguments.of("Table without columns", new EncryptRuleConfiguration(
                        Collections.singleton(new EncryptTableRuleConfiguration("foo_tbl", Collections.emptyList())), Collections.emptyMap())),
                Arguments.of("Complete configuration", createValidRuleConfiguration()));
    }
    
    private static EncryptRuleConfiguration createValidRuleConfiguration() {
        EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration(
                "foo_col", new EncryptColumnItemRuleConfiguration("foo_cipher", "foo_encryptor"));
        columnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("foo_assisted", "foo_encryptor"));
        columnConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("foo_like", "foo_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("foo_tbl", Collections.singleton(columnConfig));
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig),
                Collections.singletonMap("foo_encryptor", new AlgorithmConfiguration("FIXTURE", new Properties())));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final EncryptRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    private static Stream<Arguments> invalidRuleConfigurationArguments() {
        EncryptColumnRuleConfiguration invalidAssistedQueryName = createValidColumnConfiguration();
        invalidAssistedQueryName.setAssistedQuery(new EncryptColumnItemRuleConfiguration("", "foo_encryptor"));
        EncryptColumnRuleConfiguration invalidAssistedQueryEncryptor = createValidColumnConfiguration();
        invalidAssistedQueryEncryptor.setAssistedQuery(new EncryptColumnItemRuleConfiguration("foo_assisted", ""));
        EncryptColumnRuleConfiguration invalidLikeQueryName = createValidColumnConfiguration();
        invalidLikeQueryName.setLikeQuery(new EncryptColumnItemRuleConfiguration("", "foo_encryptor"));
        EncryptColumnRuleConfiguration invalidLikeQueryEncryptor = createValidColumnConfiguration();
        invalidLikeQueryEncryptor.setLikeQuery(new EncryptColumnItemRuleConfiguration("foo_like", ""));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        return Stream.of(
                Arguments.of("Null tables", new EncryptRuleConfiguration(null, Collections.emptyMap())),
                Arguments.of("Null table", new EncryptRuleConfiguration(Collections.singleton(null), Collections.emptyMap())),
                Arguments.of("Null encryptors", new EncryptRuleConfiguration(Collections.emptyList(), null)),
                Arguments.of("Blank encryptor name", new EncryptRuleConfiguration(Collections.emptyList(), Collections.singletonMap("", algorithmConfig))),
                Arguments.of("Null encryptor", new EncryptRuleConfiguration(Collections.emptyList(), Collections.singletonMap("foo_encryptor", null))),
                Arguments.of("Missing encryptor type", new EncryptRuleConfiguration(Collections.emptyList(),
                        Collections.singletonMap("foo_encryptor", new AlgorithmConfiguration("MISSING", new Properties())))),
                Arguments.of("Blank table name", new EncryptRuleConfiguration(
                        Collections.singleton(new EncryptTableRuleConfiguration("", Collections.emptyList())), Collections.emptyMap())),
                Arguments.of("Null columns", new EncryptRuleConfiguration(
                        Collections.singleton(new EncryptTableRuleConfiguration("foo_tbl", null)), Collections.emptyMap())),
                Arguments.of("Null column", new EncryptRuleConfiguration(
                        Collections.singleton(new EncryptTableRuleConfiguration("foo_tbl", Collections.singleton(null))), Collections.emptyMap())),
                Arguments.of("Blank column name", createRuleConfiguration(
                        new EncryptColumnRuleConfiguration("", new EncryptColumnItemRuleConfiguration("foo_cipher", "foo_encryptor")))),
                Arguments.of("Null cipher", createRuleConfiguration(new EncryptColumnRuleConfiguration("foo_col", null))),
                Arguments.of("Blank cipher name", createRuleConfiguration(
                        new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("", "foo_encryptor")))),
                Arguments.of("Blank cipher encryptor", createRuleConfiguration(
                        new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("foo_cipher", "")))),
                Arguments.of("Blank assisted query name", createRuleConfiguration(invalidAssistedQueryName)),
                Arguments.of("Blank assisted query encryptor", createRuleConfiguration(invalidAssistedQueryEncryptor)),
                Arguments.of("Blank like query name", createRuleConfiguration(invalidLikeQueryName)),
                Arguments.of("Blank like query encryptor", createRuleConfiguration(invalidLikeQueryEncryptor)));
    }
    
    private static EncryptColumnRuleConfiguration createValidColumnConfiguration() {
        return new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("foo_cipher", "foo_encryptor"));
    }
    
    private static EncryptRuleConfiguration createRuleConfiguration(final EncryptColumnRuleConfiguration columnConfig) {
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("foo_tbl", Collections.singleton(columnConfig));
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), Collections.emptyMap());
    }
}
