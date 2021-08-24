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

package org.apache.shardingsphere.spring.namespace.governance;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.spring.namespace.governance.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.governance.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/encrypt-cluster.xml")
public final class ClusterEncryptNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertEncryptDataSourceType() {
        assertNotNull(applicationContext.getBean("encryptDataSourceGovernance", ShardingSphereDataSource.class));
        assertEncryptRule(getEncryptRuleConfiguration());
    }
    
    private AlgorithmProvidedEncryptRuleConfiguration getEncryptRuleConfiguration() {
        ShardingSphereDataSource governanceDataSource = (ShardingSphereDataSource) applicationContext.getBean("encryptDataSourceGovernance");
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(governanceDataSource, "contextManager");
        return (AlgorithmProvidedEncryptRuleConfiguration) contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getConfigurations().iterator().next();
    }
    
    private void assertEncryptRule(final AlgorithmProvidedEncryptRuleConfiguration config) {
        assertThat(config.getEncryptors().size(), is(2));
        assertThat(config.getTables().size(), is(1));
        EncryptTableRuleConfiguration encryptTableRuleConfig = config.getTables().iterator().next();
        Iterator<EncryptColumnRuleConfiguration> encryptColumnRuleConfigs = encryptTableRuleConfig.getColumns().iterator();
        EncryptColumnRuleConfiguration userIdColumnRuleConfig = encryptColumnRuleConfigs.next();
        EncryptColumnRuleConfiguration orderIdColumnRuleConfig = encryptColumnRuleConfigs.next();
        assertThat(userIdColumnRuleConfig.getCipherColumn(), is("user_encrypt"));
        assertThat(orderIdColumnRuleConfig.getPlainColumn(), is("order_decrypt"));
        Map<String, EncryptAlgorithm> encryptAlgorithms = config.getEncryptors();
        assertThat(encryptAlgorithms.size(), is(2));
        assertThat(encryptAlgorithms.get("aes_encryptor").getType(), is("AES"));
        assertThat(encryptAlgorithms.get("aes_encryptor").getProps().getProperty("aes-key-value"), is("123456"));
        assertThat(encryptAlgorithms.get("md5_encryptor").getType(), is("MD5"));
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("encryptDataSourceGovernance").getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ConfigurationProperties getProperties(final String encryptDatasourceName) {
        ShardingSphereDataSource governanceDataSource = applicationContext.getBean(encryptDatasourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(governanceDataSource, "contextManager");
        return contextManager.getMetaDataContexts().getProps();
    }
}
