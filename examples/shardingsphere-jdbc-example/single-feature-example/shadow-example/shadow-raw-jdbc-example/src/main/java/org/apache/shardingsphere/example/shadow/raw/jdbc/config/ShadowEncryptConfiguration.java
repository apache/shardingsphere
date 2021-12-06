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

package org.apache.shardingsphere.example.shadow.raw.jdbc.config;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class ShadowEncryptConfiguration extends BaseShadowConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        Collection<RuleConfiguration> ruleConfigurations = createRuleConfiguration();
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigurations, createShardingSphereProps());
    }
    
    private Collection<RuleConfiguration> createRuleConfiguration() {
        Collection<RuleConfiguration> result = new LinkedList<>();
        result.add(new EncryptRuleConfiguration(getEncryptTableRuleConfigurations(), getEncryptAlgorithmConfigurations()));
        result.add(createShadowRuleConfiguration());
        return result;
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setEnable(true);
        result.setShadowAlgorithms(createShadowAlgorithmConfigurations());
        result.setDataSources(createShadowDataSources());
        result.setTables(createShadowTables());
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createShadowDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "shadow-ds"));
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createShadowTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_user", new ShadowTableConfiguration(createDataSourceNames(), createShadowAlgorithmNames()));
        return result;
    }
    
    private Collection<String> createDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        result.add("shadow-data-source");
        return result;
    }
    
    private Collection<EncryptTableRuleConfiguration> getEncryptTableRuleConfigurations() {
        Collection<EncryptTableRuleConfiguration> result = new LinkedList<>();
        Collection<EncryptColumnRuleConfiguration> columns = new LinkedList<>();
        columns.add(new EncryptColumnRuleConfiguration("user_name", "user_name", "", "user_name_plain", "name_encryptor"));
        columns.add(new EncryptColumnRuleConfiguration("pwd", "pwd", "assisted_query_pwd", "", "pwd_encryptor"));
        result.add(new EncryptTableRuleConfiguration("t_user", columns, null));
        return result;
    }
    
    private Map<String, ShardingSphereAlgorithmConfiguration> getEncryptAlgorithmConfigurations() {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new LinkedHashMap<>(2, 1);
        Properties props = new Properties();
        props.setProperty("aes-key-value", "123456");
        result.put("name_encryptor", new ShardingSphereAlgorithmConfiguration("AES", props));
        result.put("pwd_encryptor", new ShardingSphereAlgorithmConfiguration("assistedTest", null));
        return result;
    }
}
