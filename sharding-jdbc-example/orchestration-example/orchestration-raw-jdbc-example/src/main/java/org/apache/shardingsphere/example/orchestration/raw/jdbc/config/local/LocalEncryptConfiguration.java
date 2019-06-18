/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.example.common.DataSourceUtil;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationEncryptDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public class LocalEncryptConfiguration implements ExampleConfiguration {
    
    private final RegistryCenterConfiguration registryCenterConfig;
    
    public LocalEncryptConfiguration(final RegistryCenterConfiguration registryCenterConfig) {
        this.registryCenterConfig = registryCenterConfig;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return OrchestrationEncryptDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), getEncryptRuleConfiguration(), new Properties(), getOrchestrationConfiguration());
        
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        return new OrchestrationConfiguration("orchestration-encrypt-data-source", registryCenterConfig, true);
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration encryptorRuleConfig = new EncryptorRuleConfiguration("aes", "t_order.status", properties);
        result.getEncryptorRuleConfigs().put("order_encryptor", encryptorRuleConfig);
        return result;
    }
}
