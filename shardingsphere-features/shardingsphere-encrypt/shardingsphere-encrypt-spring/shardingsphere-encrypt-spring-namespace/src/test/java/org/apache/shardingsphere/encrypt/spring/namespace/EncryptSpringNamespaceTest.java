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

package org.apache.shardingsphere.encrypt.spring.namespace;

import org.apache.shardingsphere.encrypt.algorithm.AESEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.algorithm.MD5EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/spring/encrypt-application-context.xml")
public final class EncryptSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertEncryptRuleConfiguration() {
        AlgorithmProvidedEncryptRuleConfiguration config = applicationContext.getBean("encryptRule", AlgorithmProvidedEncryptRuleConfiguration.class);
        assertEncryptors(config.getEncryptors());
        assertThat(config.getTables().size(), is(1));
        assertEncryptTable(config.getTables().iterator().next());
    }
    
    private void assertEncryptors(final Map<String, EncryptAlgorithm> encryptors) {
        assertThat(encryptors.size(), is(2));
        assertThat(encryptors.get("aes_encryptor"), instanceOf(AESEncryptAlgorithm.class));
        assertThat(encryptors.get("aes_encryptor").getProps().getProperty("aes.key.value"), is("123456"));
        assertThat(encryptors.get("md5_encryptor"), instanceOf(MD5EncryptAlgorithm.class));
    }
    
    private void assertEncryptTable(final EncryptTableRuleConfiguration tableRuleConfig) {
        assertThat(tableRuleConfig.getName(), is("t_order"));
        assertThat(tableRuleConfig.getColumns().size(), is(2));
        Iterator<EncryptColumnRuleConfiguration> columnRuleConfigs = tableRuleConfig.getColumns().iterator();
        assertEncryptColumn1(columnRuleConfigs.next());
        assertEncryptColumn2(columnRuleConfigs.next());
    }
    
    private void assertEncryptColumn1(final EncryptColumnRuleConfiguration columnRuleConfig) {
        assertThat(columnRuleConfig.getLogicColumn(), is("user_id"));
        assertThat(columnRuleConfig.getCipherColumn(), is("user_encrypt"));
        assertThat(columnRuleConfig.getAssistedQueryColumn(), is("user_assisted"));
        assertThat(columnRuleConfig.getPlainColumn(), is("user_decrypt"));
        assertThat(columnRuleConfig.getEncryptorName(), is("aes_encryptor"));
    }
    
    private void assertEncryptColumn2(final EncryptColumnRuleConfiguration columnRuleConfig) {
        assertThat(columnRuleConfig.getLogicColumn(), is("order_id"));
        assertThat(columnRuleConfig.getCipherColumn(), is("order_encrypt"));
        assertThat(columnRuleConfig.getAssistedQueryColumn(), is("order_assisted"));
        assertThat(columnRuleConfig.getPlainColumn(), is("order_decrypt"));
        assertThat(columnRuleConfig.getEncryptorName(), is("md5_encryptor"));
    }
}
