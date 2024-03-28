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

package org.apache.shardingsphere.encrypt.distsql.handler.provider;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptRuleConfigurationToDistSQLConverterTest {
    
    @Test
    void assertConvertWithEmptyTables() {
        EncryptRuleConfiguration encryptRuleConfiguration = mock(EncryptRuleConfiguration.class);
        when(encryptRuleConfiguration.getTables()).thenReturn(Collections.emptyList());
        EncryptRuleConfigurationToDistSQLConverter encryptRuleConfigurationToDistSQLConverter = new EncryptRuleConfigurationToDistSQLConverter();
        assertThat(encryptRuleConfigurationToDistSQLConverter.convert(encryptRuleConfiguration), is(""));
    }
    
    @Test
    void assertConvert() {
        EncryptRuleConfiguration encryptRuleConfiguration = getEncryptRuleConfiguration();
        EncryptRuleConfigurationToDistSQLConverter encryptRuleConfigurationToDistSQLConverter = new EncryptRuleConfigurationToDistSQLConverter();
        assertThat(encryptRuleConfigurationToDistSQLConverter.convert(encryptRuleConfiguration),
                is("CREATE ENCRYPT RULE t_encrypt (" + System.lineSeparator() + "COLUMNS(" + System.lineSeparator()
                        + "(NAME=user_id, CIPHER=user_cipher, ASSISTED_QUERY_COLUMN=user_assisted, LIKE_QUERY_COLUMN=user_like, ENCRYPT_ALGORITHM(TYPE(NAME='md5')), "
                        + "ASSISTED_QUERY_ALGORITHM(), LIKE_QUERY_ALGORITHM())," + System.lineSeparator()
                        + "(NAME=pwd, CIPHER=pwd_cipher, ASSISTED_QUERY_COLUMN=pwd_assisted, LIKE_QUERY_COLUMN=pwd_like, ENCRYPT_ALGORITHM(TYPE(NAME='md5')), "
                        + "ASSISTED_QUERY_ALGORITHM(), LIKE_QUERY_ALGORITHM())" + System.lineSeparator() + "))," + System.lineSeparator() + " t_encrypt_another (" + System.lineSeparator() + "COLUMNS("
                        + System.lineSeparator() + "(NAME=user_id, CIPHER=user_cipher, ASSISTED_QUERY_COLUMN=user_assisted, LIKE_QUERY_COLUMN=user_like, ENCRYPT_ALGORITHM(TYPE(NAME='md5')), "
                        + "ASSISTED_QUERY_ALGORITHM(), LIKE_QUERY_ALGORITHM())" + System.lineSeparator() + "));"));
    }
    
    @Test
    void assertGetType() {
        EncryptRuleConfigurationToDistSQLConverter encryptRuleConfigurationToDistSQLConverter = new EncryptRuleConfigurationToDistSQLConverter();
        assertThat(encryptRuleConfigurationToDistSQLConverter.getType().getName(), is("org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration"));
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "test"));
        encryptColumnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_assisted", "foo_assist_query_encryptor"));
        encryptColumnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_like", "foo_like_encryptor"));
        EncryptColumnRuleConfiguration encryptColumnRuleConfig2 = new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", "test"));
        encryptColumnRuleConfig2.setAssistedQuery(new EncryptColumnItemRuleConfiguration("pwd_assisted", "foo_assist_query_encryptor"));
        encryptColumnRuleConfig2.setLikeQuery(new EncryptColumnItemRuleConfiguration("pwd_like", "foo_like_encryptor"));
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", new LinkedList<>(Arrays.asList(encryptColumnRuleConfig, encryptColumnRuleConfig2)));
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new EncryptRuleConfiguration(
                new LinkedList<>(Arrays.asList(encryptTableRuleConfig, new EncryptTableRuleConfiguration("t_encrypt_another", Collections.singleton(encryptColumnRuleConfig)))),
                Collections.singletonMap("test", shardingSphereAlgorithmConfig));
    }
}
