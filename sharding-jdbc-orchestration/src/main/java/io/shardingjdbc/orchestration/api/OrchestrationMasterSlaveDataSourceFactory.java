/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.api;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationFacade;
import io.shardingjdbc.orchestration.internal.OrchestrationMasterSlaveDataSource;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.yaml.YamlOrchestrationMasterSlaveRuleConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import java.io.*;
import java.sql.SQLException;
import java.util.Map;

/**
 * Orchestration master-slave data source factory.
 *
 * @author zhangliang
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationMasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param orchestrationConfig orchestration configuration
     * @param configMap config map
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
            final Map<String, Object> configMap, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        OrchestrationFacade orchestrationFacade = new OrchestrationFacade(orchestrationConfig);
        if (null == masterSlaveRuleConfig || null == masterSlaveRuleConfig.getMasterDataSourceName()) {
            ConfigurationService configService = orchestrationFacade.getConfigService();
            final MasterSlaveRuleConfiguration cloudMasterSlaveRuleConfig = configService.loadMasterSlaveRuleConfiguration();
            Preconditions.checkState(null != cloudMasterSlaveRuleConfig, "Missing the master-slave rule configuration on register center");
            return createDataSource(configService.loadDataSourceMap(), cloudMasterSlaveRuleConfig, configService.loadMasterSlaveConfigMap(), orchestrationFacade);
        } else {
            return createDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, orchestrationFacade);
        }
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param yamlMasterSlaveRuleConfig yaml master-slave rule configuration
     * @param orchestrationConfig orchestration configuration
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final YamlMasterSlaveRuleConfiguration yamlMasterSlaveRuleConfig, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        OrchestrationFacade orchestrationFacade = new OrchestrationFacade(orchestrationConfig);
        if (null == yamlMasterSlaveRuleConfig) {
            ConfigurationService configService = orchestrationFacade.getConfigService();
            final MasterSlaveRuleConfiguration cloudMasterSlaveRuleConfig = configService.loadMasterSlaveRuleConfiguration();
            Preconditions.checkState(null != cloudMasterSlaveRuleConfig, "Missing the master-slave rule configuration on register center");
            return createDataSource(configService.loadDataSourceMap(), cloudMasterSlaveRuleConfig, configService.loadMasterSlaveConfigMap(), orchestrationFacade);
        } else {
            return createDataSource(dataSourceMap, yamlMasterSlaveRuleConfig.getMasterSlaveRuleConfiguration(), yamlMasterSlaveRuleConfig.getConfigMap(), orchestrationFacade);
        }
    }
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param orchestrationFacade orchestration facade
     * @param configMap config map
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    private static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
            final Map<String, Object> configMap, final OrchestrationFacade orchestrationFacade) throws SQLException {
        OrchestrationMasterSlaveDataSource result = new OrchestrationMasterSlaveDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, orchestrationFacade);
        result.init();
        return result;
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param yamlFile yaml file for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param dataSourceMap data source map
     * @param yamlFile yaml file for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlFile);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param yamlByteArray yaml byte array for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final byte[] yamlByteArray) throws SQLException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlByteArray);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param dataSourceMap data source map
     * @param yamlByteArray yaml byte array for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlByteArray) throws SQLException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlByteArray);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule(), config.getOrchestration().getOrchestrationConfiguration());
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
