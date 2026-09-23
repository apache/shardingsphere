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
import java.util.Map;
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
                Arguments.of("Complete configuration", createValidRuleConfiguration()),
                Arguments.of("Multiple columns without derived column conflicts", createValidMultipleColumnRuleConfiguration()));
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
    
    private static EncryptRuleConfiguration createValidMultipleColumnRuleConfiguration() {
        EncryptColumnRuleConfiguration fooColumnConfig = createValidColumnConfiguration();
        fooColumnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("foo_assisted", "foo_encryptor"));
        EncryptColumnRuleConfiguration barColumnConfig = new EncryptColumnRuleConfiguration("bar_col", new EncryptColumnItemRuleConfiguration("bar_cipher", "foo_encryptor"));
        barColumnConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("bar_like", "foo_encryptor"));
        return createRuleConfiguration(Arrays.asList(fooColumnConfig, barColumnConfig), Collections.singletonMap("foo_encryptor", new AlgorithmConfiguration("FIXTURE", new Properties())));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRuleConfigurationArguments")
    void assertValidateInvalidRuleConfiguration(final String name, final EncryptRuleConfiguration ruleConfig) {
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
    }
    
    @Test
    void assertUnconfiguredEncryptorViolation() {
        EncryptRuleConfiguration ruleConfig = createRuleConfiguration(new EncryptColumnRuleConfiguration(
                "foo_col", new EncryptColumnItemRuleConfiguration("foo_cipher", "bar_encryptor")));
        InvalidRuleConfigurationException actual = assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validate(ruleConfig));
        assertThat(actual.getMessage(), is("Invalid 'EncryptRuleConfiguration' rule, error message is: Property `tables` references unconfigured encryptors `bar_encryptor`."));
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
        EncryptColumnRuleConfiguration missingAssistedQueryEncryptor = createValidColumnConfiguration();
        missingAssistedQueryEncryptor.setAssistedQuery(new EncryptColumnItemRuleConfiguration("foo_assisted", "bar_encryptor"));
        EncryptColumnRuleConfiguration missingLikeQueryEncryptor = createValidColumnConfiguration();
        missingLikeQueryEncryptor.setLikeQuery(new EncryptColumnItemRuleConfiguration("foo_like", "bar_encryptor"));
        EncryptColumnRuleConfiguration assistedQueryColumnNameConflict = createValidColumnConfiguration();
        assistedQueryColumnNameConflict.setAssistedQuery(new EncryptColumnItemRuleConfiguration("foo_col", "foo_encryptor"));
        EncryptColumnRuleConfiguration likeQueryColumnNameConflict = createValidColumnConfiguration();
        likeQueryColumnNameConflict.setLikeQuery(new EncryptColumnItemRuleConfiguration("foo_col", "foo_encryptor"));
        EncryptColumnRuleConfiguration caseInsensitiveColumnNameConflict = createValidColumnConfiguration();
        caseInsensitiveColumnNameConflict.setAssistedQuery(new EncryptColumnItemRuleConfiguration("FOO_COL", "foo_encryptor"));
        EncryptColumnRuleConfiguration crossColumnAssistedQueryNameConflict = createValidColumnConfiguration();
        crossColumnAssistedQueryNameConflict.setAssistedQuery(new EncryptColumnItemRuleConfiguration("bar_col", "foo_encryptor"));
        EncryptColumnRuleConfiguration crossColumnLikeQueryNameConflict = createValidColumnConfiguration();
        crossColumnLikeQueryNameConflict.setLikeQuery(new EncryptColumnItemRuleConfiguration("bar_col", "foo_encryptor"));
        EncryptColumnRuleConfiguration otherLogicColumn = new EncryptColumnRuleConfiguration("bar_col", new EncryptColumnItemRuleConfiguration("bar_cipher", "foo_encryptor"));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("FIXTURE", new Properties());
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("foo_encryptor", algorithmConfig);
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
                Arguments.of("Blank like query encryptor", createRuleConfiguration(invalidLikeQueryEncryptor)),
                Arguments.of("Unconfigured cipher encryptor", createRuleConfiguration(createValidColumnConfiguration())),
                Arguments.of("Unconfigured assisted query encryptor", createRuleConfiguration(missingAssistedQueryEncryptor,
                        Collections.singletonMap("foo_encryptor", algorithmConfig))),
                Arguments.of("Unconfigured like query encryptor", createRuleConfiguration(missingLikeQueryEncryptor,
                        Collections.singletonMap("foo_encryptor", algorithmConfig))),
                Arguments.of("Assisted query column conflicts with its logic column", createRuleConfiguration(assistedQueryColumnNameConflict, encryptors)),
                Arguments.of("Like query column conflicts with its logic column", createRuleConfiguration(likeQueryColumnNameConflict, encryptors)),
                Arguments.of("Derived column conflicts with its logic column case insensitively", createRuleConfiguration(caseInsensitiveColumnNameConflict, encryptors)),
                Arguments.of("Assisted query column conflicts with another logic column", createRuleConfiguration(Arrays.asList(crossColumnAssistedQueryNameConflict, otherLogicColumn), encryptors)),
                Arguments.of("Like query column conflicts with another logic column", createRuleConfiguration(Arrays.asList(crossColumnLikeQueryNameConflict, otherLogicColumn), encryptors)));
    }
    
    @Test
    void assertValidateInvalidRuleItemConfiguration() {
        EncryptColumnRuleConfiguration columnConfig = createValidColumnConfiguration();
        columnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("foo_col", "foo_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("foo_tbl", Collections.singleton(columnConfig));
        EncryptRuleConfiguration ruleConfig = new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        assertThrows(InvalidRuleConfigurationException.class, () -> RuleConfigurationValidator.validateRuleItem(ruleConfig, tableConfig));
    }
    
    private static EncryptColumnRuleConfiguration createValidColumnConfiguration() {
        return new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("foo_cipher", "foo_encryptor"));
    }
    
    private static EncryptRuleConfiguration createRuleConfiguration(final EncryptColumnRuleConfiguration columnConfig) {
        return createRuleConfiguration(columnConfig, Collections.emptyMap());
    }
    
    private static EncryptRuleConfiguration createRuleConfiguration(final EncryptColumnRuleConfiguration columnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        return createRuleConfiguration(Collections.singleton(columnConfig), encryptors);
    }
    
    private static EncryptRuleConfiguration createRuleConfiguration(final Collection<EncryptColumnRuleConfiguration> columnConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("foo_tbl", columnConfigs);
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), encryptors);
    }
}
