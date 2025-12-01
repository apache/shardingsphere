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

package org.apache.shardingsphere.encrypt.yaml;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EncryptRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    EncryptRuleConfigurationYamlIT() {
        super("yaml/encrypt-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static EncryptRuleConfiguration getExpectedRuleConfiguration() {
        return new EncryptRuleConfiguration(getEncryptTableRuleConfigurations(), createAlgorithmConfigurationMap());
    }
    
    private static Collection<EncryptTableRuleConfiguration> getEncryptTableRuleConfigurations() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("username",
                new EncryptColumnItemRuleConfiguration("username_cipher", "aes_encryptor"));
        encryptColumnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("assisted_query_username", "assisted_encryptor"));
        EncryptTableRuleConfiguration tableRuleConfig = new EncryptTableRuleConfiguration("t_user", Collections.singletonList(encryptColumnRuleConfig));
        return Collections.singletonList(tableRuleConfig);
    }
    
    private static Map<String, AlgorithmConfiguration> createAlgorithmConfigurationMap() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("aes_encryptor", new AlgorithmConfiguration("AES", PropertiesBuilder.build(new Property("aes-key-value", "123456abc"), new Property("digest-algorithm-name", "SHA-1"))));
        result.put("assisted_encryptor", new AlgorithmConfiguration("AES", PropertiesBuilder.build(new Property("aes-key-value", "123456abc"), new Property("digest-algorithm-name", "SHA-1"))));
        return result;
    }
    
    @Override
    protected boolean assertYamlConfiguration(final YamlRuleConfiguration actual) {
        assertEncryptRule((YamlEncryptRuleConfiguration) actual);
        return true;
    }
    
    private void assertEncryptRule(final YamlEncryptRuleConfiguration actual) {
        assertTables(actual.getTables());
        assertEncryptAlgorithm(actual.getEncryptors());
    }
    
    private void assertTables(final Map<String, YamlEncryptTableRuleConfiguration> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("t_user").getColumns().size(), is(1));
        assertThat(actual.get("t_user").getColumns().get("username").getCipher().getName(), is("username_cipher"));
        assertThat(actual.get("t_user").getColumns().get("username").getCipher().getEncryptorName(), is("aes_encryptor"));
        assertThat(actual.get("t_user").getColumns().get("username").getAssistedQuery().getName(), is("assisted_query_username"));
        assertThat(actual.get("t_user").getColumns().get("username").getAssistedQuery().getEncryptorName(), is("assisted_encryptor"));
    }
    
    private void assertEncryptAlgorithm(final Map<String, YamlAlgorithmConfiguration> actual) {
        assertThat(actual.size(), is(2));
        assertThat(actual.get("aes_encryptor").getType(), is("AES"));
        assertThat(actual.get("aes_encryptor").getProps().getProperty("aes-key-value"), is("123456abc"));
        assertThat(actual.get("aes_encryptor").getProps().getProperty("digest-algorithm-name"), is("SHA-1"));
        assertThat(actual.get("assisted_encryptor").getType(), is("AES"));
        assertThat(actual.get("assisted_encryptor").getProps().getProperty("aes-key-value"), is("123456abc"));
        assertThat(actual.get("assisted_encryptor").getProps().getProperty("digest-algorithm-name"), is("SHA-1"));
    }
}
