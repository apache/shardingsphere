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
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.YamlCenterRepositoryConfigurationSwapperUtil;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.yaml.YamlOrchestrationMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration master-slave data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationMasterSlaveDataSourceFactory {
    
    private static final MasterSlaveRuleConfigurationYamlSwapper MASTER_SLAVE_RULE_SWAPPER = new MasterSlaveRuleConfigurationYamlSwapper();
    
    /**
     * Create master-slave data source.
     *
     * @param yamlFile YAML file for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule(), config.getProps(), config.getOrchestration());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile YAML file for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule(), config.getProps(), config.getOrchestration());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param yamlBytes YAML bytes for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlBytes);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule(), config.getProps(), config.getOrchestration());
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param yamlBytes YAML bytes for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlBytes);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule(), config.getProps(), config.getOrchestration());
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlMasterSlaveRuleConfiguration yamlMasterSlaveRuleConfiguration, 
                                               final Properties props, final Map<String, YamlCenterRepositoryConfiguration> yamlInstanceConfigurationMap) throws SQLException {
        if (null == yamlMasterSlaveRuleConfiguration) {
            return new OrchestrationMasterSlaveDataSource(new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)));
        } else {
            MasterSlaveDataSource masterSlaveDataSource = new MasterSlaveDataSource(dataSourceMap, new MasterSlaveRule(MASTER_SLAVE_RULE_SWAPPER.swap(yamlMasterSlaveRuleConfiguration)), props);
            return new OrchestrationMasterSlaveDataSource(masterSlaveDataSource, new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)));
        }
    }
    
    private static YamlOrchestrationMasterSlaveRuleConfiguration unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlOrchestrationMasterSlaveRuleConfiguration.class);
    }
    
    private static YamlOrchestrationMasterSlaveRuleConfiguration unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlOrchestrationMasterSlaveRuleConfiguration.class);
    }
}
