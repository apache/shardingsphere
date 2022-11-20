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

package org.apache.shardingsphere.encrypt.spring.boot;

import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.algorithm.encrypt.AESEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.algorithm.encrypt.MD5EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.algorithm.like.CharDigestLikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = EncryptSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("encrypt")
public class EncryptSpringBootStarterTest {
    
    @Resource
    private AESEncryptAlgorithm aesEncryptor;
    
    @Resource
    private AlgorithmProvidedEncryptRuleConfiguration encryptRuleConfig;
    
    @Test
    public void assertAesEncryptor() {
        assertThat(aesEncryptor.getProps().getProperty("aes-key-value"), is("123456"));
    }
    
    @Test
    public void assertEncryptRuleConfiguration() {
        assertEncryptors(encryptRuleConfig.getEncryptors());
        assertThat(encryptRuleConfig.getTables().size(), is(1));
        assertEncryptTable(encryptRuleConfig.getTables().iterator().next());
    }
    
    private void assertEncryptors(final Map<String, EncryptAlgorithm<?, ?>> encryptors) {
        assertThat(encryptors.size(), is(3));
        assertThat(encryptors.get("aesEncryptor"), instanceOf(AESEncryptAlgorithm.class));
        assertThat(encryptors.get("aesEncryptor").getProps().getProperty("aes-key-value"), is("123456"));
        assertThat(encryptors.get("md5Encryptor"), instanceOf(MD5EncryptAlgorithm.class));
        assertThat(encryptors.get("likeQueryEncryptor"), instanceOf(CharDigestLikeEncryptAlgorithm.class));
    }
    
    private void assertEncryptTable(final EncryptTableRuleConfiguration tableRuleConfig) {
        assertThat(tableRuleConfig.getName(), is("t_order"));
        assertThat(tableRuleConfig.getColumns().size(), is(2));
        assertFalse(tableRuleConfig.getQueryWithCipherColumn());
        Iterator<EncryptColumnRuleConfiguration> columnRuleConfigs = tableRuleConfig.getColumns().iterator();
        assertEncryptColumn2(columnRuleConfigs.next());
        assertEncryptColumn1(columnRuleConfigs.next());
    }
    
    private void assertEncryptColumn1(final EncryptColumnRuleConfiguration columnRuleConfig) {
        assertThat(columnRuleConfig.getLogicColumn(), is("pwd"));
        assertThat(columnRuleConfig.getCipherColumn(), is("pwd_cipher"));
        assertThat(columnRuleConfig.getEncryptorName(), is("aesEncryptor"));
    }
    
    private void assertEncryptColumn2(final EncryptColumnRuleConfiguration columnRuleConfig) {
        assertThat(columnRuleConfig.getLogicColumn(), is("credit_card"));
        assertThat(columnRuleConfig.getCipherColumn(), is("credit_card_cipher"));
        assertThat(columnRuleConfig.getAssistedQueryColumn(), is("credit_card_assisted_query"));
        assertThat(columnRuleConfig.getLikeQueryColumn(), is("credit_card_like_query"));
        assertThat(columnRuleConfig.getPlainColumn(), is("credit_card_plain"));
        assertThat(columnRuleConfig.getEncryptorName(), is("md5Encryptor"));
        assertThat(columnRuleConfig.getLikeQueryEncryptorName(), is("likeQueryEncryptor"));
    }
}
