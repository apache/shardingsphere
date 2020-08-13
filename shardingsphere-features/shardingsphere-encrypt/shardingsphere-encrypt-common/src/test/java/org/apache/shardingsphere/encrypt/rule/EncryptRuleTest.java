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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.fixture.TestEncryptAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptRuleTest {
    
    private final String table = "t_encrypt";
    
    private final String pwdColumn = "pwd";
    
    private final String creditCardColumn = "credit_card";
    
    private EncryptRuleConfiguration encryptRuleConfig;
    
    @Before
    public void setUp() {
        encryptRuleConfig = createEncryptRuleConfiguration();
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("QUERY_ASSISTED_TEST", new Properties());
        EncryptColumnRuleConfiguration pwdColumnConfig = new EncryptColumnRuleConfiguration(pwdColumn, "pwd_cipher", "", "pwd_plain", "test_encryptor");
        EncryptColumnRuleConfiguration creditCardColumnConfig = new EncryptColumnRuleConfiguration(creditCardColumn, "credit_card_cipher", "", "credit_card_plain", "test_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(table, Arrays.asList(pwdColumnConfig, creditCardColumnConfig));
        return new EncryptRuleConfiguration(Collections.singleton(tableConfig), ImmutableMap.of("test_encryptor", encryptAlgorithmConfig));
    }
    
    @Test
    public void assertNewInstanceWithAlgorithmProvidedEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration("encrypt_column", "encrypt_cipher", "", "", "test_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(table, Collections.singletonList(encryptColumnConfig));
        AlgorithmProvidedEncryptRuleConfiguration ruleConfig = new AlgorithmProvidedEncryptRuleConfiguration(
                Collections.singleton(tableConfig), ImmutableMap.of("test_encryptor", new TestEncryptAlgorithm()));
        EncryptRule actual = new EncryptRule(ruleConfig);
        assertThat(actual.getEncryptTableNames(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewInstanceWithInvalidConfiguration() {
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("TEST", new Properties());
        EncryptColumnRuleConfiguration encryptColumnConfig = new EncryptColumnRuleConfiguration("encrypt_column", "encrypt_cipher", "", "", "test_encryptor");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(table, Collections.singletonList(encryptColumnConfig));
        EncryptRuleConfiguration ruleConfig = new EncryptRuleConfiguration(Collections.singleton(tableConfig), ImmutableMap.of("invalid_encryptor", encryptAlgorithmConfig));
        new EncryptRule(ruleConfig);
    }
    
    @Test
    public void assertGetEncryptAssistedQueryValues() {
        List<Object> encryptAssistedQueryValues = new EncryptRule(encryptRuleConfig).getEncryptAssistedQueryValues(table, pwdColumn, Collections.singletonList(null));
        for (final Object value : encryptAssistedQueryValues) {
            assertNull(value);
        }
    }
    
    @Test
    public void assertGetEncryptValues() {
        List<Object> encryptAssistedQueryValues = new EncryptRule(encryptRuleConfig).getEncryptValues(table, pwdColumn, Collections.singletonList(null));
        for (final Object value : encryptAssistedQueryValues) {
            assertNull(value);
        }
    }
    
    @Test
    public void assertFindEncryptTable() {
        assertTrue(new EncryptRule(encryptRuleConfig).findEncryptTable(table).isPresent());
    }
    
    @Test
    public void assertGetLogicColumnOfCipher() {
        assertThat(new EncryptRule(encryptRuleConfig).getLogicColumnOfCipher(table, "pwd_cipher"), is(pwdColumn));
    }
    
    @Test
    public void assertFindPlainColumn() {
        assertTrue(new EncryptRule(encryptRuleConfig).findPlainColumn(table, pwdColumn).isPresent());
        assertTrue(new EncryptRule(encryptRuleConfig).findPlainColumn(table, creditCardColumn.toLowerCase()).isPresent());
        assertFalse(new EncryptRule(encryptRuleConfig).findPlainColumn(table, "notExistLogicColumn").isPresent());
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGetCipherColumnWhenNoEncryptColumn() {
        new EncryptRule(encryptRuleConfig).getCipherColumn(table, "pwd_cipher");
    }
    
    @Test
    public void assertGetCipherColumnWhenEncryptColumnExist() {
        assertThat(new EncryptRule(encryptRuleConfig).getCipherColumn(table, pwdColumn), is("pwd_cipher"));
    }
    
    @Test
    public void assertIsCipherColumn() {
        assertTrue(new EncryptRule(encryptRuleConfig).isCipherColumn(table, "pwd_cipher"));
    }
    
    @Test
    public void assertFindAssistedQueryColumn() {
        assertFalse(new EncryptRule(encryptRuleConfig).findAssistedQueryColumn(table, "pwd_cipher").isPresent());
    }
    
    @Test
    public void assertGetAssistedQueryColumns() {
        assertTrue(new EncryptRule(encryptRuleConfig).getAssistedQueryColumns(table).isEmpty());
    }
    
    @Test
    public void assertGetAssistedQueryAndPlainColumns() {
        assertFalse(new EncryptRule(encryptRuleConfig).getAssistedQueryAndPlainColumns(table).isEmpty());
    }
    
    @Test
    public void assertGetLogicAndCipherColumns() {
        assertFalse(new EncryptRule(encryptRuleConfig).getLogicAndCipherColumns(table).isEmpty());
    }
    
    @Test
    public void assertFindEncryptor() {
        assertTrue(new EncryptRule(encryptRuleConfig).findEncryptor(table, pwdColumn).isPresent());
    }
    
    @Test
    public void assertNotFindEncryptor() {
        assertFalse(new EncryptRule(encryptRuleConfig).findEncryptor(table, "other_column").isPresent());
    }
    
    @Test
    public void assertGetEncryptTableNames() {
        assertFalse(new EncryptRule(encryptRuleConfig).getEncryptTableNames().isEmpty());
    }
}
