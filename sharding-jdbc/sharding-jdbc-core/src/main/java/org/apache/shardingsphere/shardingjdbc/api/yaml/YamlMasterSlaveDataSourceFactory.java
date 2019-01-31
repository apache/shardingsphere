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
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Master-slave data source factory for YAML.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlMasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     *
     * @param yamlFile yaml file for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlMasterSlaveConfiguration config = YamlEngine.unmarshal(yamlFile, YamlMasterSlaveConfiguration.class);
        return MasterSlaveDataSourceFactory.createDataSource(config.getDataSources(), 
                new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), config.getConfigMap(), config.getProps());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param yamlBytes yaml bytes for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        YamlMasterSlaveConfiguration config = YamlEngine.unmarshal(yamlBytes, YamlMasterSlaveConfiguration.class);
        return MasterSlaveDataSourceFactory.createDataSource(config.getDataSources(), 
                new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), config.getConfigMap(), config.getProps());
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
        YamlMasterSlaveConfiguration config = YamlEngine.unmarshal(yamlFile, YamlMasterSlaveConfiguration.class);
        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), config.getConfigMap(), config.getProps());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param yamlBytes yaml bytes for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlMasterSlaveConfiguration config = YamlEngine.unmarshal(yamlBytes, YamlMasterSlaveConfiguration.class);
        return MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, new MasterSlaveRuleConfigurationYamlSwapper().swap(config.getMasterSlaveRule()), config.getConfigMap(), config.getProps());
    }
}
