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

package org.apache.shardingsphere.example.governance.raw.jdbc.config.local;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.driver.governance.api.GovernanceShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

public final class LocalEncryptConfiguration implements ExampleConfiguration {
    
    private final GovernanceConfiguration governanceConfiguration;
    
    public LocalEncryptConfiguration(final GovernanceConfiguration governanceConfiguration) {
        this.governanceConfiguration = governanceConfiguration;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return GovernanceShardingSphereDataSourceFactory.createDataSource(
                DataSourceUtil.createDataSource("demo_ds"), Collections.singleton(getEncryptRuleConfiguration()), new Properties(), governanceConfiguration);
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        return new EncryptRuleConfiguration(Collections.singleton(createEncryptTableRuleConfiguration()), ImmutableMap.of("status_encryptor", createEncryptAlgorithmConfiguration()));
    }
    
    private EncryptTableRuleConfiguration createEncryptTableRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfiguration = new EncryptColumnRuleConfiguration("status", "status", "", "", "status_encryptor");
        return new EncryptTableRuleConfiguration("t_order", Collections.singleton(encryptColumnRuleConfiguration));
    }
    
    private ShardingSphereAlgorithmConfiguration createEncryptAlgorithmConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes.key.value", "123456");
        return new ShardingSphereAlgorithmConfiguration("AES", props);
    }
}
