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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.algorithm.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleTest {
    
    @Test
    void assertFindEncryptTable() {
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).findEncryptTable("t_encrypt").isPresent());
    }
    
    @Test
    void assertFindStandardEncryptor() {
        assertTrue(new EncryptRule(createEncryptRuleConfiguration()).findStandardEncryptor("t_encrypt", "pwd").isPresent());
    }
    
    @Test
    void assertNotFindStandardEncryptor() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).findStandardEncryptor("t_encrypt", "other_column").isPresent());
    }
    
    @Test
    void assertGetEncryptValues() {
        List<Object> encryptAssistedQueryValues = new EncryptRule(createEncryptRuleConfiguration())
                .encrypt(DefaultDatabase.LOGIC_NAME, DefaultDatabase.LOGIC_NAME, "t_encrypt", "pwd", Collections.singletonList(null));
        for (Object each : encryptAssistedQueryValues) {
            assertNull(each);
        }
    }
    
    @Test
    void assertGetCipherColumnWhenEncryptColumnExist() {
        assertThat(new EncryptRule(createEncryptRuleConfiguration()).getCipherColumn("t_encrypt", "pwd"), is("pwd_cipher"));
    }
    
    @Test
    void assertGetCipherColumnWhenNoEncryptColumn() {
        // TODO should not throw NPE
        assertThrows(NullPointerException.class, () -> new EncryptRule(createEncryptRuleConfiguration()).getCipherColumn("t_encrypt", "pwd_cipher"));
    }
    
    @Test
    void assertGetLogicAndCipherColumns() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).getLogicAndCipherColumnsMap("t_encrypt").isEmpty());
    }
    
    @Test
    void assertFindAssistedQueryColumn() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).findAssistedQueryColumn("t_encrypt", "pwd_cipher").isPresent());
    }
    
    @Test
    void assertGetEncryptAssistedQueryValues() {
        List<Object> encryptAssistedQueryValues = new EncryptRule(createEncryptRuleConfiguration())
                .getEncryptAssistedQueryValues(DefaultDatabase.LOGIC_NAME, DefaultDatabase.LOGIC_NAME, "t_encrypt", "pwd", Collections.singletonList(null));
        for (Object each : encryptAssistedQueryValues) {
            assertNull(each);
        }
    }
    
    @Test
    void assertGetAssistedQueryColumns() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).getAssistedQueryColumns("t_encrypt").isEmpty());
    }
    
    @Test
    void assertFindLikeQueryColumn() {
        assertFalse(new EncryptRule(createEncryptRuleConfiguration()).findLikeQueryColumn("t_encrypt", "pwd_cipher").isPresent());
    }
    
    @Test
    void assertGetEncryptLikeQueryValues() {
        List<Object> encryptLikeQueryValues = new EncryptRule(createEncryptRuleConfiguration())
                .getEncryptLikeQueryValues(DefaultDatabase.LOGIC_NAME, DefaultDatabase.LOGIC_NAME, "t_encrypt", "pwd", Collections.singletonList(null));
        for (Object actual : encryptLikeQueryValues) {
            assertNull(actual);
        }
    }
    
    @Test
    void assertGetTables() {
        assertThat(new EncryptRule(createEncryptRuleConfiguration()).getTables(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    void assertGetTableWithLowercase() {
        assertThat(new EncryptRule(createEncryptRuleConfigurationWithUpperCaseLogicTable()).getTables(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    void assertTheSameLogicTable() {
        Collection<String> logicTables = new EncryptRule(createEncryptRuleConfiguration()).getTables();
        Collection<String> theSameLogicTables = new EncryptRule(createEncryptRuleConfigurationWithUpperCaseLogicTable()).getTables();
        assertThat(logicTables, is(theSameLogicTables));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration pwdColumnConfig = createEncryptColumnRuleConfiguration("standard_encryptor", "assisted_encryptor", "like_encryptor");
        EncryptColumnRuleConfiguration creditCardColumnConfig = new EncryptColumnRuleConfiguration("credit_card", new EncryptColumnItemRuleConfiguration("credit_card_cipher", "standard_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Arrays.asList(pwdColumnConfig, creditCardColumnConfig));
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), getEncryptors(new AlgorithmConfiguration("CORE.FIXTURE", new Properties()),
                new AlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties()), new AlgorithmConfiguration("CORE.QUERY_LIKE.FIXTURE", new Properties())));
    }
    
    @Test
    void assertAssistedQueryEncryptorNameSpecified() {
        EncryptColumnRuleConfiguration pwdColumnConfig =
                new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", "standard_encryptor"));
        pwdColumnConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("pwd_assist", "assisted_query_test_encryptor"));
        assertTrue(pwdColumnConfig.getAssistedQuery().isPresent());
        assertThat(pwdColumnConfig.getAssistedQuery().get().getEncryptorName(), is("assisted_query_test_encryptor"));
    }
    
    @Test
    void assertLikeQueryEncryptorNameSpecified() {
        EncryptColumnRuleConfiguration pwdColumnConfig =
                new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", "standard_encryptor"));
        pwdColumnConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("pwd_like", "like_query_test_encryptor"));
        assertTrue(pwdColumnConfig.getLikeQuery().isPresent());
        assertThat(pwdColumnConfig.getLikeQuery().get().getEncryptorName(), is("like_query_test_encryptor"));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfigurationWithUpperCaseLogicTable() {
        AlgorithmConfiguration standardEncryptConfig = new AlgorithmConfiguration("CORE.FIXTURE", new Properties());
        AlgorithmConfiguration queryAssistedEncryptConfig = new AlgorithmConfiguration("CORE.QUERY_ASSISTED.FIXTURE", new Properties());
        AlgorithmConfiguration queryLikeEncryptConfig = new AlgorithmConfiguration("CORE.QUERY_LIKE.FIXTURE", new Properties());
        EncryptColumnRuleConfiguration pwdColumnConfig = new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", "standard_encryptor"));
        EncryptColumnRuleConfiguration creditCardColumnConfig = new EncryptColumnRuleConfiguration("credit_card", new EncryptColumnItemRuleConfiguration("credit_card_cipher", "standard_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("T_ENCRYPT", Arrays.asList(pwdColumnConfig, creditCardColumnConfig));
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), getEncryptors(standardEncryptConfig, queryAssistedEncryptConfig, queryLikeEncryptConfig));
    }
    
    private Map<String, AlgorithmConfiguration> getEncryptors(final AlgorithmConfiguration standardEncryptConfig, final AlgorithmConfiguration queryAssistedEncryptConfig,
                                                              final AlgorithmConfiguration queryLikeEncryptConfig) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(2, 1F);
        result.put("standard_encryptor", standardEncryptConfig);
        result.put("assisted_encryptor", queryAssistedEncryptConfig);
        result.put("like_encryptor", queryLikeEncryptConfig);
        return result;
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
        assertThrows(MismatchedEncryptAlgorithmTypeException.class, () -> new EncryptRule(ruleConfig));
    }
    
    private static EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration(final String encryptorName, final String assistedQueryEncryptorName, final String likeEncryptorName) {
        EncryptColumnRuleConfiguration result = new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", encryptorName));
        result.setAssistedQuery(new EncryptColumnItemRuleConfiguration("pwd_assist", assistedQueryEncryptorName));
        result.setLikeQuery(new EncryptColumnItemRuleConfiguration("pwd_like", likeEncryptorName));
        return result;
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("Encryptor", "assisted_encryptor", "assisted_encryptor", "like_encryptor"),
                    Arguments.of("AssistedQueryEncryptor", "standard_encryptor", "like_encryptor", "like_encryptor"),
                    Arguments.of("LikeEncryptor", "standard_encryptor", "assisted_encryptor", "standard_encryptor"));
        }
    }
}
