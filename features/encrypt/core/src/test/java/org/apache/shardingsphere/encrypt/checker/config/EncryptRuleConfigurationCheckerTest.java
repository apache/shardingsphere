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

package org.apache.shardingsphere.encrypt.checker.config;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EncryptRuleConfigurationCheckerTest {
    
    @SuppressWarnings("rawtypes")
    private RuleConfigurationChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(EncryptRuleConfiguration.class)).get(EncryptRuleConfiguration.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCheckSuccess() {
        EncryptRuleConfiguration ruleConfig = createValidRuleConfiguration();
        checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList());
    }
    
    private EncryptRuleConfiguration createValidRuleConfiguration() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        Collection<EncryptTableRuleConfiguration> tables = Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig)));
        Map<String, AlgorithmConfiguration> encryptors = Collections.singletonMap("aes_encryptor", new AlgorithmConfiguration("MD5", new Properties()));
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCheckWithInvalidCipherColumn() {
        EncryptRuleConfiguration ruleConfig = createRuleConfigurationWithInvalidCipherColumn();
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private EncryptRuleConfiguration createRuleConfigurationWithInvalidCipherColumn() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        Collection<EncryptTableRuleConfiguration> tables = Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig)));
        Map<String, AlgorithmConfiguration> encryptors = Collections.emptyMap();
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCheckWithInvalidAssistColumn() {
        EncryptRuleConfiguration ruleConfig = createRuleConfigurationWithInvalidAssistColumn();
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private EncryptRuleConfiguration createRuleConfigurationWithInvalidAssistColumn() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        columnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_assisted", "aes_assisted_encryptor"));
        Collection<EncryptTableRuleConfiguration> tables = Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig)));
        Map<String, AlgorithmConfiguration> encryptors = Collections.emptyMap();
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCheckWithInvalidLikeColumn() {
        EncryptRuleConfiguration ruleConfig = createRuleConfigurationWithInvalidLikeColumn();
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private EncryptRuleConfiguration createRuleConfigurationWithInvalidLikeColumn() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        columnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_like", "like_cn_encryptor"));
        Collection<EncryptTableRuleConfiguration> tables = Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnRuleConfig)));
        Map<String, AlgorithmConfiguration> encryptors = Collections.emptyMap();
        return new EncryptRuleConfiguration(tables, encryptors);
    }
}
