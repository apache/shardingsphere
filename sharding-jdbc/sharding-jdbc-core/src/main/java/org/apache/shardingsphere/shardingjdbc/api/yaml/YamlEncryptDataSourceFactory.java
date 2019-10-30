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

package org.apache.shardingsphere.shardingjdbc.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlRootEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;

/**
 * Encrypt data source factory for YAML.
 * 
 * @author panjuan 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlEncryptDataSourceFactory {
    
    /**
     * Create encrypt data source.
     *
     * @param yamlFile YAML file for encrypt rule configuration with data sources
     * @return encrypt data source
     */
    @SneakyThrows
    public static DataSource createDataSource(final File yamlFile) {
        YamlRootEncryptRuleConfiguration config = YamlEngine.unmarshal(yamlFile, YamlRootEncryptRuleConfiguration.class);
        return EncryptDataSourceFactory.createDataSource(config.getDataSource(), new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), config.getProps());
    }
    
    /**
     * Create encrypt data source.
     *
     * @param yamlBytes YAML bytes for encrypt rule configuration with data sources
     * @return encrypt data source
     */
    @SneakyThrows
    public static DataSource createDataSource(final byte[] yamlBytes) {
        YamlRootEncryptRuleConfiguration config = YamlEngine.unmarshal(yamlBytes, YamlRootEncryptRuleConfiguration.class);
        return EncryptDataSourceFactory.createDataSource(config.getDataSource(), new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), config.getProps());
    }
    
    /**
     * Create encrypt data source.
     *
     * @param dataSource data source
     * @param yamlFile YAML file for encrypt rule configuration without data sources
     * @return encrypt data source
     */
    @SneakyThrows
    public static DataSource createDataSource(final DataSource dataSource, final File yamlFile) {
        YamlRootEncryptRuleConfiguration config = YamlEngine.unmarshal(yamlFile, YamlRootEncryptRuleConfiguration.class);
        return EncryptDataSourceFactory.createDataSource(dataSource, new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), config.getProps());
    
    }
    
    /**
     * Create encrypt data source.
     *
     * @param dataSource data source
     * @param yamlBytes YAML bytes for encrypt rule configuration without data sources
     * @return encrypt data source
     */
    @SneakyThrows
    public static DataSource createDataSource(final DataSource dataSource, final byte[] yamlBytes) {
        YamlRootEncryptRuleConfiguration config = YamlEngine.unmarshal(yamlBytes, YamlRootEncryptRuleConfiguration.class);
        return EncryptDataSourceFactory.createDataSource(dataSource, new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), config.getProps());
    }
}
