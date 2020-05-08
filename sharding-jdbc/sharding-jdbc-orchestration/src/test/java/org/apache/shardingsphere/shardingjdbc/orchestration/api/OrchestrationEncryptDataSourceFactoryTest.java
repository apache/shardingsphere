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

package org.apache.shardingsphere.shardingjdbc.orchestration.api;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.CenterType;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationEncryptDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class OrchestrationEncryptDataSourceFactoryTest {
    
    @Before
    public void setUp() throws SQLException {
        OrchestrationEncryptDataSourceFactory.createDataSource(getDataSource(), getEncryptRuleConfiguration(), new Properties(), getOrchestrationConfiguration());
    }
    
    @Test
    public void assertCreateDataSourceWithDataSource() throws SQLException {
        DataSource dataSource = OrchestrationEncryptDataSourceFactory.createDataSource(getDataSource(), getEncryptRuleConfiguration(), new Properties(), getOrchestrationConfiguration());
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceWithoutDataSource() throws SQLException {
        DataSource dataSource = OrchestrationEncryptDataSourceFactory.createDataSource(getOrchestrationConfiguration());
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceWithEmptyRule() throws SQLException {
        DataSource dataSource = OrchestrationEncryptDataSourceFactory.createDataSource(null, null, null, getOrchestrationConfiguration());
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    private DataSource getDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:ds_encrypt;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration encryptorRuleConfig = new EncryptorRuleConfiguration("aes", properties);
        return new EncryptRuleConfiguration(ImmutableMap.of("order_encryptor", encryptorRuleConfig), Collections.emptyMap());
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<>();
        instanceConfigurationMap.put("test_encrypt_registry_name", getRegistryCenterConfiguration());
        instanceConfigurationMap.put("test_encrypt_config_name", getConfigCenterConfiguration());
        instanceConfigurationMap.put("test_encrypt_metadata_name", getMetaDataCenterConfiguration());
        return new OrchestrationConfiguration(instanceConfigurationMap);
    }
    
    private CenterConfiguration getRegistryCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("FirstTestRegistryCenter");
        result.setOrchestrationType(CenterType.REGISTRY_CENTER.getValue());
        result.setNamespace("test_encrypt_registry");
        result.setServerLists("localhost:3181");
        return result;
    }
    
    private CenterConfiguration getConfigCenterConfiguration() {
        CenterConfiguration result = new CenterConfiguration("FirstTestConfigCenter");
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
}
