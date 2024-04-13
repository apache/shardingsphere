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

package org.apache.shardingsphere.encrypt.checker;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptRuleConfigurationCheckerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigValidConfiguration() {
        EncryptRuleConfiguration ruleConfig = createValidConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        checker.check("test", ruleConfig, Collections.emptyMap(), Collections.emptyList());
    }
    
    private EncryptRuleConfiguration createValidConfiguration() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.singletonMap("aes_encryptor", new AlgorithmConfiguration("MD5", new Properties())));
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        Collection<EncryptColumnRuleConfiguration> columns = Collections.singleton(columnRuleConfig);
        when(result.getTables()).thenReturn(Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", columns)));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidCipherColumn() {
        EncryptRuleConfiguration config = createInvalidCipherColumnConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("test", config, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private EncryptRuleConfiguration createInvalidCipherColumnConfiguration() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.emptyMap());
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        Collection<EncryptColumnRuleConfiguration> columns = Collections.singleton(columnRuleConfig);
        when(result.getTables()).thenReturn(Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", columns)));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidAssistColumn() {
        EncryptRuleConfiguration config = createInvalidAssistColumnConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("test", config, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private EncryptRuleConfiguration createInvalidAssistColumnConfiguration() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.emptyMap());
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        Collection<EncryptColumnRuleConfiguration> columns = Collections.singleton(columnRuleConfig);
        columnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_assisted", "aes_assisted_encryptor"));
        when(result.getTables()).thenReturn(Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", columns)));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertCheckWhenConfigInvalidLikeColumn() {
        EncryptRuleConfiguration config = createInvalidLikeColumnConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(UnregisteredAlgorithmException.class, () -> checker.check("test", config, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private EncryptRuleConfiguration createInvalidLikeColumnConfiguration() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.emptyMap());
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "aes_encryptor"));
        Collection<EncryptColumnRuleConfiguration> columns = Collections.singleton(columnRuleConfig);
        columnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_like", "like_cn_encryptor"));
        when(result.getTables()).thenReturn(Collections.singleton(new EncryptTableRuleConfiguration("t_encrypt", columns)));
        return result;
    }
}
