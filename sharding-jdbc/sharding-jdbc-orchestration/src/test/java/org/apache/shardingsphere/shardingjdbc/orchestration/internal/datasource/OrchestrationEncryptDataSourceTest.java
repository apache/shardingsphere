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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.encrypt.api.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.orchestration.core.common.CenterType;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.EncryptRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlEncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.underlying.common.database.DefaultSchema;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrchestrationEncryptDataSourceTest {
    
    private OrchestrationEncryptDataSource encryptDataSource;
    
    @Before
    public void setUp() throws Exception {
        encryptDataSource = new OrchestrationEncryptDataSource(getEncryptDatasource(), getOrchestrationConfiguration());
    }
    
    private EncryptDataSource getEncryptDatasource() throws URISyntaxException {
        File yamlFile = new File(OrchestrationEncryptDataSource.class.getResource("/yaml/unit/encrypt.yaml").toURI());
        return (EncryptDataSource) YamlEncryptDataSourceFactory.createDataSource(yamlFile);
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>();
        instanceConfigurationMap.put("test_encrypt_registry_name", getRegistryCenterConfiguration());
        instanceConfigurationMap.put("test_encrypt_config_name", getConfigCenterConfiguration());
        instanceConfigurationMap.put("test_encrypt_metadata_name", getMetaDataCenterConfiguration());
        return new OrchestrationConfiguration(instanceConfigurationMap);
    }
    
    private CenterConfiguration getRegistryCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("SecondTestRegistryCenter");
        result.setOrchestrationType(CenterType.REGISTRY_CENTER.getValue());
        result.setNamespace("test_encrypt_registry");
        result.setServerLists("localhost:3181");
        return result;
    }
    
    private CenterConfiguration getConfigCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("SecondTestConfigCenter");
        result.setOrchestrationType(CenterType.CONFIG_CENTER.getValue());
        result.setNamespace("test_encrypt_config");
        result.setServerLists("localhost:3181");
        return result;
    }
    
    private CenterConfiguration getMetaDataCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("FirstTestMetaDataCenter");
        result.setOrchestrationType(CenterType.METADATA_CENTER.getValue());
        result.setNamespace("test_encrypt_metadata");
        result.setServerLists("localhost:3181");
        return result;
    }

    @Test
    public void assertInitializeOrchestrationEncryptDataSource() throws SQLException {
        DataSource dataSource = new OrchestrationEncryptDataSource(getOrchestrationConfiguration());
        assertThat(dataSource.getConnection(), instanceOf(EncryptConnection.class));
    }
    
    @Test
    public void assertRenewRule() {
        encryptDataSource.renew(getEncryptRuleChangedEvent());
        EncryptRule encryptRule = (EncryptRule) encryptDataSource.getDataSource().getRuntimeContext().getRules().iterator().next();
        assertThat(encryptRule.getEncryptTableNames().size(), is(1));
        assertThat(encryptRule.getEncryptTableNames().iterator().next(), is("t_order_item"));
        assertThat(encryptRule.getCipherColumn("t_order_item", "item_id"), is("cipher_item_id"));
        assertThat(encryptRule.findPlainColumn("t_order_item", "item_id"), is(Optional.of("plain_item_id")));
        Map<String, EncryptorRuleConfiguration> encryptorRuleConfigurations = encryptRule.getRuleConfiguration().getEncryptors();
        assertThat(encryptorRuleConfigurations.size(), is(1));
        assertTrue(encryptorRuleConfigurations.containsKey("order_encryptor"));
        EncryptorRuleConfiguration encryptorRuleConfig = encryptorRuleConfigurations.get("order_encryptor");
        assertThat(encryptorRuleConfig.getType(), is("md5"));
        assertThat(encryptorRuleConfig.getProperties().stringPropertyNames().size(), is(0));
    }
    
    private EncryptRuleChangedEvent getEncryptRuleChangedEvent() {
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration();
        encryptRuleConfig.getEncryptors().put("order_encryptor", new EncryptorRuleConfiguration("md5", new Properties()));
        encryptRuleConfig.getTables().put("t_order_item", 
                new EncryptTableRuleConfiguration(Collections.singletonMap("item_id", new EncryptColumnRuleConfiguration("plain_item_id", "cipher_item_id", "", "order_encryptor"))));
        return new EncryptRuleChangedEvent(DefaultSchema.LOGIC_NAME, encryptRuleConfig);
    }
    
    @Test
    public void assertRenewDataSource() throws SQLException {
        assertThat(encryptDataSource.getConnection().getMetaData().getURL(), is("jdbc:h2:mem:ds_encrypt"));
        encryptDataSource.renew(new DataSourceChangedEvent(DefaultSchema.LOGIC_NAME, getDataSourceConfigurations()));
        assertThat(encryptDataSource.getConnection().getMetaData().getURL(), is("jdbc:h2:mem:test"));
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return Collections.singletonMap("ds_test", DataSourceConfiguration.getDataSourceConfiguration(dataSource));
    }
    
    @Test
    public void assertRenewDataSourceWithError() {
        try {
            encryptDataSource.renew(new DataSourceChangedEvent(DefaultSchema.LOGIC_NAME, Collections.emptyMap()));
        } catch (IllegalStateException ex) {
            assertThat(ex.getMessage(), is("There should be only one datasource for encrypt, but now has 0 datasource(s)"));
        }
    }
    
    @Test
    public void assertRenewProperties() {
        encryptDataSource.renew(getPropertiesChangedEvent());
        assertThat(encryptDataSource.getDataSource().getRuntimeContext().getProperties().getProps().getProperty("sql.show"), is("true"));
    }
    
    private PropertiesChangedEvent getPropertiesChangedEvent() {
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        return new PropertiesChangedEvent(properties);
    }
}
