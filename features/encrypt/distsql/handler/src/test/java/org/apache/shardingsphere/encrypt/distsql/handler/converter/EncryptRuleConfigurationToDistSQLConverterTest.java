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

package org.apache.shardingsphere.encrypt.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EncryptRuleConfigurationToDistSQLConverterTest {
    
    @SuppressWarnings("unchecked")
    private final RuleConfigurationToDistSQLConverter<EncryptRuleConfiguration> converter = TypedSPILoader.getService(RuleConfigurationToDistSQLConverter.class, EncryptRuleConfiguration.class);
    
    @Test
    void assertConvertWithEmptyTables() {
        EncryptRuleConfiguration ruleConfig = new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
        assertThat(converter.convert(ruleConfig), is(""));
    }
    
    @Test
    void assertConvert() {
        EncryptRuleConfiguration ruleConfig = createEncryptRuleConfiguration();
        assertThat(converter.convert(ruleConfig),
                is("CREATE ENCRYPT RULE foo_tbl ("
                        + System.lineSeparator()
                        + "COLUMNS("
                        + System.lineSeparator()
                        + "(NAME=foo_col, CIPHER=foo_col_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='md5'))),"
                        + System.lineSeparator()
                        + "(NAME=bar_col, CIPHER=bar_col_cipher, ASSISTED_QUERY_COLUMN=bar_col_assisted, LIKE_QUERY_COLUMN=bar_col_like,"
                        + " ENCRYPT_ALGORITHM(TYPE(NAME='md5')), ASSISTED_QUERY_ALGORITHM(), LIKE_QUERY_ALGORITHM())"
                        + System.lineSeparator()
                        + ")),"
                        + System.lineSeparator()
                        + " t_encrypt_another ("
                        + System.lineSeparator()
                        + "COLUMNS("
                        + System.lineSeparator()
                        + "(NAME=foo_col, CIPHER=foo_col_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='md5')))"
                        + System.lineSeparator()
                        + "));"));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig1 = new EncryptColumnRuleConfiguration("foo_col", new EncryptColumnItemRuleConfiguration("foo_col_cipher", "test"));
        EncryptColumnRuleConfiguration encryptColumnRuleConfig2 = createEncryptColumnRuleConfiguration();
        EncryptTableRuleConfiguration encryptTableRuleConfig = createEncryptTableRuleConfiguration(encryptColumnRuleConfig1, encryptColumnRuleConfig2);
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new EncryptRuleConfiguration(Arrays.asList(encryptTableRuleConfig, new EncryptTableRuleConfiguration("t_encrypt_another", Collections.singleton(encryptColumnRuleConfig1))),
                Collections.singletonMap("test", shardingSphereAlgorithmConfig));
    }
    
    private EncryptTableRuleConfiguration createEncryptTableRuleConfiguration(final EncryptColumnRuleConfiguration encryptColumnRuleConfig1,
                                                                              final EncryptColumnRuleConfiguration encryptColumnRuleConfig2) {
        return new EncryptTableRuleConfiguration("foo_tbl", new LinkedList<>(Arrays.asList(encryptColumnRuleConfig1, encryptColumnRuleConfig2)));
    }
    
    private EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration() {
        EncryptColumnRuleConfiguration result = new EncryptColumnRuleConfiguration("bar_col", new EncryptColumnItemRuleConfiguration("bar_col_cipher", "test"));
        result.setAssistedQuery(new EncryptColumnItemRuleConfiguration("bar_col_assisted", "bar_assist_query_encryptor"));
        result.setLikeQuery(new EncryptColumnItemRuleConfiguration("bar_col_like", "bar_like_encryptor"));
        return result;
    }
}
