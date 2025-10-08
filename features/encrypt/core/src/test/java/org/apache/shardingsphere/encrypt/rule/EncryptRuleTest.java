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

package org.apache.shardingsphere.encrypt.rule;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptTableNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleTest {
    
    @Test
    void assertGetAllTableNames() {
        assertThat(new EncryptRule("foo_db", createEncryptRuleConfiguration()).getAllTableNames(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    void assertFindEncryptTable() {
        assertTrue(new EncryptRule("foo_db", createEncryptRuleConfiguration()).findEncryptTable("t_encrypt").isPresent());
    }
    
    @Test
    void assertGetEncryptTable() {
        assertThat(new EncryptRule("foo_db", createEncryptRuleConfiguration()).getEncryptTable("t_encrypt").getTable(), is("t_encrypt"));
    }
    
    @Test
    void assertGetNotExistedEncryptTable() {
        assertThrows(EncryptTableNotFoundException.class, () -> new EncryptRule("foo_db", createEncryptRuleConfiguration()).getEncryptTable("not_existed_tbl"));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration pwdColumnConfig = createEncryptColumnRuleConfiguration("standard_encryptor", "assisted_encryptor", "like_encryptor");
        EncryptColumnRuleConfiguration creditCardColumnConfig = new EncryptColumnRuleConfiguration("credit_card", new EncryptColumnItemRuleConfiguration("credit_card_cipher", "standard_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Arrays.asList(pwdColumnConfig, creditCardColumnConfig));
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), getEncryptors(new AlgorithmConfiguration("CORE.FIXTURE", new Properties()),
                new AlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties()), new AlgorithmConfiguration("CORE.QUERY_LIKE.FIXTURE", new Properties())));
    }
    
    private Map<String, AlgorithmConfiguration> getEncryptors(final AlgorithmConfiguration standardEncryptConfig, final AlgorithmConfiguration queryAssistedEncryptConfig,
                                                              final AlgorithmConfiguration queryLikeEncryptConfig) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(3, 1F);
        result.put("standard_encryptor", standardEncryptConfig);
        result.put("assisted_encryptor", queryAssistedEncryptConfig);
        result.put("like_encryptor", queryLikeEncryptConfig);
        return result;
    }
    
    @Test
    void assertFindQueryEncryptor() {
        EncryptRule encryptRule = new EncryptRule("foo_db", createEncryptRuleConfiguration());
        assertThat(encryptRule.findQueryEncryptor("t_encrypt", "credit_card"),
                is(Optional.of(encryptRule.getEncryptTable("t_encrypt").getEncryptColumn("credit_card").getCipher().getEncryptor())));
    }
    
    @Test
    void assertNotFindQueryEncryptor() {
        assertFalse(new EncryptRule("foo_db", createEncryptRuleConfiguration()).findQueryEncryptor("t_encrypt", "invalid_col").isPresent());
    }
    
    @SuppressWarnings("unused")
    @ParameterizedTest(name = "Wrong{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertNewEncryptRuleWhenConfigureWrongEncryptorType(final String name, final String encryptorName, final String assistedQueryEncryptorName, final String likeEncryptorName) {
        EncryptColumnRuleConfiguration pwdColumnConfig = createEncryptColumnRuleConfiguration(encryptorName, assistedQueryEncryptorName, likeEncryptorName);
        EncryptColumnRuleConfiguration creditCardColumnConfig = new EncryptColumnRuleConfiguration("credit_card", new EncryptColumnItemRuleConfiguration("credit_card_cipher", "standard_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Arrays.asList(pwdColumnConfig, creditCardColumnConfig));
        EncryptRuleConfiguration ruleConfig = new EncryptRuleConfiguration(Collections.singleton(tableConfig), getEncryptors(new AlgorithmConfiguration("CORE.FIXTURE", new Properties()),
                new AlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties()), new AlgorithmConfiguration("CORE.QUERY_LIKE.FIXTURE", new Properties())));
        assertThrows(MismatchedEncryptAlgorithmTypeException.class, () -> new EncryptRule("foo_db", ruleConfig));
    }
    
    private EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration(final String encryptorName, final String assistedQueryEncryptorName, final String likeEncryptorName) {
        EncryptColumnRuleConfiguration result = new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", encryptorName));
        result.setAssistedQuery(new EncryptColumnItemRuleConfiguration("pwd_assist", assistedQueryEncryptorName));
        result.setLikeQuery(new EncryptColumnItemRuleConfiguration("pwd_like", likeEncryptorName));
        return result;
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("Encryptor", "assisted_encryptor", "assisted_encryptor", "like_encryptor"),
                    Arguments.of("AssistedQueryEncryptor", "standard_encryptor", "like_encryptor", "like_encryptor"),
                    Arguments.of("LikeEncryptor", "standard_encryptor", "assisted_encryptor", "standard_encryptor"));
        }
    }
}
