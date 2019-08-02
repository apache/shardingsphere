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

package org.apache.shardingsphere.shardingjdbc.orchestration.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationEncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.yaml.YamlOrchestrationEncryptRuleConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Orchestration encrypt data source factory for YAML.
 *
 * @author yangyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationEncryptDataSourceFactory {
    
    private static final OrchestrationConfigurationYamlSwapper ORCHESTRATION_SWAPPER = new OrchestrationConfigurationYamlSwapper();
    
    private static final EncryptRuleConfigurationYamlSwapper ENCRYPT_RULE_SWAPPER = new EncryptRuleConfigurationYamlSwapper();
    
    /**
     * Create encrypt data source.
     *
     * @param yamlFile YAML file for encrypt rule configuration with data source
     * @return encrypt data source
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final File yamlFile) throws IOException, SQLException {
        YamlOrchestrationEncryptRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(config.getDataSource(), config.getEncryptRule(), config.getOrchestration(), config.getProps());
    }
    
    /**
     * Create encrypt data source.
     *
     * @param dataSource data source
     * @param yamlFile YAML file for encrypt rule configuration without data source
     * @return encrypt data source
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final File yamlFile) throws IOException, SQLException {
        YamlOrchestrationEncryptRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(dataSource, config.getEncryptRule(), config.getOrchestration(), config.getProps());
    }
    
    /**
     * Create encrypt data source.
     *
     * @param yamlBytes YAML bytes for for encrypt rule configuration with data source
     * @return encrypt data source
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws IOException, SQLException {
        YamlOrchestrationEncryptRuleConfiguration config = unmarshal(yamlBytes);
        return createDataSource(config.getDataSource(), config.getEncryptRule(), config.getOrchestration(), config.getProps());
    }
    
    /**
     * Create encrypt data source.
     *
     * @param dataSource data source
     * @param yamlBytes YAML bytes for encrypt rule configuration without data source
     * @return encrypt data source
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final DataSource dataSource, final byte[] yamlBytes) throws IOException, SQLException {
        YamlOrchestrationEncryptRuleConfiguration config = unmarshal(yamlBytes);
        return createDataSource(dataSource, config.getEncryptRule(), config.getOrchestration(), config.getProps());
    }
    
    private static DataSource createDataSource(final DataSource dataSource, final YamlEncryptRuleConfiguration yamlEncryptRuleConfiguration,
                                               final YamlOrchestrationConfiguration yamlOrchestrationConfiguration, final Properties properties) throws SQLException {
        if (null == yamlEncryptRuleConfiguration) {
            return new OrchestrationEncryptDataSource(ORCHESTRATION_SWAPPER.swap(yamlOrchestrationConfiguration));
        } else {
            EncryptDataSource encryptDataSource = new EncryptDataSource(dataSource, new EncryptRule(ENCRYPT_RULE_SWAPPER.swap(yamlEncryptRuleConfiguration)), properties);
            return new OrchestrationEncryptDataSource(encryptDataSource, ORCHESTRATION_SWAPPER.swap(yamlOrchestrationConfiguration));
        }
    }
    
    private static YamlOrchestrationEncryptRuleConfiguration unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlOrchestrationEncryptRuleConfiguration.class);
    }
    
    private static YamlOrchestrationEncryptRuleConfiguration unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlOrchestrationEncryptRuleConfiguration.class);
    }
}
