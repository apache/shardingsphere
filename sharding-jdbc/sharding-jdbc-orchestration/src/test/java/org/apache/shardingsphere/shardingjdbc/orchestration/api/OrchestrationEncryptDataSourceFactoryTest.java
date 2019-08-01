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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptorRuleConfiguration;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationEncryptDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class OrchestrationEncryptDataSourceFactoryTest {
    
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
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration encryptorRuleConfig = new EncryptorRuleConfiguration("aes", properties);
        result.getEncryptors().put("order_encryptor", encryptorRuleConfig);
        return result;
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        RegistryCenterConfiguration registryCenterConfiguration = new RegistryCenterConfiguration("TestRegistryCenter");
        registryCenterConfiguration.setNamespace("test_ms");
        registryCenterConfiguration.setServerLists("localhost:3181");
        return new OrchestrationConfiguration("test", registryCenterConfiguration, true);
    }
}
