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
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationCheckerFactory;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptRuleConfigurationCheckerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertCheckWhenConfigValidConfiguration() {
        EncryptRuleConfiguration config = createValidConfiguration();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(EncryptRuleConfigurationChecker.class));
        checker.get().check("test", config, Collections.emptyMap(), Collections.emptyList());
    }
    
    private EncryptRuleConfiguration createValidConfiguration() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.singletonMap("aes_encryptor", mock(AlgorithmConfiguration.class)));
        Collection<EncryptColumnRuleConfiguration> columns = Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_cipher", null, "user_plain", "aes_encryptor", false));
        when(result.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("t_encrypt", columns, false)));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertCheckWhenConfigInvalidCipherColumn() {
        EncryptRuleConfiguration config = createInvalidCipherColumnConfig();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(EncryptRuleConfigurationChecker.class));
        checker.get().check("test", config, Collections.emptyMap(), Collections.emptyList());
    }
    
    private EncryptRuleConfiguration createInvalidCipherColumnConfig() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.emptyMap());
        Collection<EncryptColumnRuleConfiguration> columns = Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_cipher", null, "user_plain", "aes_encryptor", false));
        when(result.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("t_encrypt", columns, false)));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test(expected = IllegalStateException.class)
    public void assertCheckWhenConfigInvalidAssistColumn() {
        EncryptRuleConfiguration config = createInvalidAssistColumnConfig();
        Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.findInstance(config);
        assertTrue(checker.isPresent());
        assertThat(checker.get(), instanceOf(EncryptRuleConfigurationChecker.class));
        checker.get().check("test", config, Collections.emptyMap(), Collections.emptyList());
    }
    
    private EncryptRuleConfiguration createInvalidAssistColumnConfig() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getEncryptors()).thenReturn(Collections.emptyMap());
        Collection<EncryptColumnRuleConfiguration> columns =
                Collections.singletonList(new EncryptColumnRuleConfiguration("user_id", "user_cipher", "user_assisted", "user_plain", "aes_encryptor", "aes_assisted_encryptor", false, null, null));
        when(result.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("t_encrypt", columns, false)));
        return result;
    }
}
