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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring;

import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringEncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.util.EmbedTestingServer;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/encryptOrchestration.xml")
public class OrchestrationEncryptNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertEncryptDataSourceType() {
        assertNotNull(applicationContext.getBean("encryptDataSourceOrchestration", OrchestrationSpringEncryptDataSource.class));
        assertEncryptRule(getEncryptRule());
    }
    
    @SneakyThrows
    private EncryptRule getEncryptRule() {
        OrchestrationSpringEncryptDataSource encryptDataSource = (OrchestrationSpringEncryptDataSource) applicationContext.getBean("encryptDataSourceOrchestration");
        EncryptDataSource dataSource = (EncryptDataSource) FieldValueUtil.getFieldValue(encryptDataSource, "dataSource", true);
        return (EncryptRule) dataSource.getRuntimeContext().getRules().iterator().next();
    }
    
    private void assertEncryptRule(final EncryptRule encryptRule) {
        assertNotNull(encryptRule.getRuleConfiguration());
        EncryptRuleConfiguration ruleConfiguration = encryptRule.getRuleConfiguration();
        assertThat(ruleConfiguration.getEncryptors().size(), is(2));
        assertThat(ruleConfiguration.getTables().size(), is(1));
        assertThat(ruleConfiguration.getTables().get("t_order").getColumns().get("user_id").getCipherColumn(), is("user_encrypt"));
        assertThat(ruleConfiguration.getTables().get("t_order").getColumns().get("order_id").getPlainColumn(), is("order_decrypt"));
        EncryptorRuleConfiguration encryptorRule = ruleConfiguration.getEncryptors().get("encryptor_md5");
        assertNotNull(encryptorRule);
        assertThat(encryptorRule.getType(), is("MD5"));
        encryptorRule = ruleConfiguration.getEncryptors().get("encryptor_aes");
        assertThat(encryptorRule.getType(), is("AES"));
        assertThat(encryptorRule.getProperties().getProperty("aes.key.value"), is("123456"));
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("encryptDataSourceOrchestration").getValue(ConfigurationPropertyKey.SQL_SHOW);
        boolean queryWithCipherColumn = getProperties("encryptDataSourceOrchestration").getValue(ConfigurationPropertyKey.QUERY_WITH_CIPHER_COLUMN);
        assertTrue(showSQL);
        assertFalse(queryWithCipherColumn);
    }
    
    private ConfigurationProperties getProperties(final String encryptDatasourceName) {
        OrchestrationSpringEncryptDataSource encryptDataSource = applicationContext.getBean(encryptDatasourceName, OrchestrationSpringEncryptDataSource.class);
        EncryptDataSource dataSource = (EncryptDataSource) FieldValueUtil.getFieldValue(encryptDataSource, "dataSource", true);
        return dataSource.getRuntimeContext().getProperties();
    }
}
