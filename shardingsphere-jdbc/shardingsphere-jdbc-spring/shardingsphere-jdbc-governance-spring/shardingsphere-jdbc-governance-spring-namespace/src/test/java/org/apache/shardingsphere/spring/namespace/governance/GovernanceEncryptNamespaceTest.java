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

import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.spring.namespace.governance.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.governance.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/encryptGovernance.xml")
public final class GovernanceEncryptNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertEncryptDataSourceType() {
        assertNotNull(applicationContext.getBean("encryptDataSourceGovernance", GovernanceShardingSphereDataSource.class));
        assertEncryptRule(getEncryptRuleConfiguration());
    }
    
    private AlgorithmProvidedEncryptRuleConfiguration getEncryptRuleConfiguration() {
        GovernanceShardingSphereDataSource governanceDataSource = (GovernanceShardingSphereDataSource) applicationContext.getBean("encryptDataSourceGovernance");
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(governanceDataSource, "schemaContexts");
        return (AlgorithmProvidedEncryptRuleConfiguration) schemaContexts.getDefaultSchemaContext().getSchema().getConfigurations().iterator().next();
    }
    
    private void assertEncryptRule(final AlgorithmProvidedEncryptRuleConfiguration configuration) {
        assertThat(configuration.getEncryptors().size(), is(2));
        assertThat(configuration.getTables().size(), is(1));
        EncryptTableRuleConfiguration encryptTableRuleConfiguration = configuration.getTables().iterator().next();
        Iterator<EncryptColumnRuleConfiguration> encryptColumnRuleConfigs = encryptTableRuleConfiguration.getColumns().iterator();
        EncryptColumnRuleConfiguration userIdColumnRuleConfiguration = encryptColumnRuleConfigs.next();
        EncryptColumnRuleConfiguration orderIdColumnRuleConfiguration = encryptColumnRuleConfigs.next();
        assertThat(userIdColumnRuleConfiguration.getCipherColumn(), is("user_encrypt"));
        assertThat(orderIdColumnRuleConfiguration.getPlainColumn(), is("order_decrypt"));
        Map<String, EncryptAlgorithm> encryptAlgorithms = configuration.getEncryptors();
        assertThat(encryptAlgorithms.size(), is(2));
        assertThat(encryptAlgorithms.get("aes_encryptor").getType(), is("AES"));
        assertThat(encryptAlgorithms.get("aes_encryptor").getProps().getProperty("aes-key-value"), is("123456"));
        assertThat(encryptAlgorithms.get("md5_encryptor").getType(), is("MD5"));
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("encryptDataSourceGovernance").getValue(ConfigurationPropertyKey.SQL_SHOW);
        boolean queryWithCipherColumn = getProperties("encryptDataSourceGovernance").getValue(ConfigurationPropertyKey.QUERY_WITH_CIPHER_COLUMN);
        assertTrue(showSQL);
        assertFalse(queryWithCipherColumn);
    }
    
    private ConfigurationProperties getProperties(final String encryptDatasourceName) {
        GovernanceShardingSphereDataSource governanceDataSource = applicationContext.getBean(encryptDatasourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(governanceDataSource, "schemaContexts");
        return schemaContexts.getProps();
    }
}
