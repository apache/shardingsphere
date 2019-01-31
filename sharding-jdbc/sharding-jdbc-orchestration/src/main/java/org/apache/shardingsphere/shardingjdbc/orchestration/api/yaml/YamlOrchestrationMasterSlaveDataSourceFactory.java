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
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.yaml.YamlOrchestrationMasterSlaveRuleConfiguration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration master-slave data source factory for YAML.
 *
 * @author zhangliang
 * @author caohao
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationMasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     *
     * @param yamlFile yaml file for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule(), config.getConfigMap(), config.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile yaml file for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule(), config.getConfigMap(), config.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param yamlByteArray yaml byte array for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final byte[] yamlByteArray) throws SQLException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlByteArray);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule(), config.getConfigMap(), config.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param yamlByteArray yaml byte array for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlByteArray) throws SQLException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlByteArray);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule(), config.getConfigMap(), config.getProps(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlMasterSlaveRuleConfiguration yamlConfig, 
                                               final Map<String, Object> configMap, final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        if (null == yamlConfig) {
            return new OrchestrationMasterSlaveDataSource(orchestrationConfig);
        } else {
            MasterSlaveDataSource masterSlaveDataSource = new MasterSlaveDataSource(dataSourceMap, new MasterSlaveRuleConfigurationYamlSwapper().swap(yamlConfig), configMap, props);
            return new OrchestrationMasterSlaveDataSource(masterSlaveDataSource, orchestrationConfig);
        }
    }
    
    private static YamlOrchestrationMasterSlaveRuleConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlOrchestrationMasterSlaveRuleConfiguration.class)).loadAs(inputStreamReader, YamlOrchestrationMasterSlaveRuleConfiguration.class);
        }
    }
    
    private static YamlOrchestrationMasterSlaveRuleConfiguration unmarshal(final byte[] yamlByteArray) {
        return new Yaml(new Constructor(YamlOrchestrationMasterSlaveRuleConfiguration.class)).loadAs(new ByteArrayInputStream(yamlByteArray), YamlOrchestrationMasterSlaveRuleConfiguration.class);
    }
}
