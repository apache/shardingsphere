/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.api;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationFacade;
import io.shardingjdbc.orchestration.yaml.YamlOrchestrationMasterSlaveRuleConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
     * @param orchestrationConfig orchestration master-slave configuration
     * 
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        MasterSlaveDataSource result = (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig);
        new OrchestrationFacade(orchestrationConfig).initMasterSlaveOrchestration(dataSourceMap, masterSlaveRuleConfig, result);
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
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getOrchestration().getOrchestrationConfiguration());
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
        return createDataSource(dataSourceMap, config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param yamlByteArray yaml byte array for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlByteArray) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlByteArray);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getOrchestration().getOrchestrationConfiguration());
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
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlByteArray) throws SQLException, IOException {
        YamlOrchestrationMasterSlaveRuleConfiguration config = unmarshal(yamlByteArray);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getOrchestration().getOrchestrationConfiguration());
    }
    
    private static YamlOrchestrationMasterSlaveRuleConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlOrchestrationMasterSlaveRuleConfiguration.class)).loadAs(inputStreamReader, YamlOrchestrationMasterSlaveRuleConfiguration.class);
        }
    }
    
    private static YamlOrchestrationMasterSlaveRuleConfiguration unmarshal(final byte[] yamlByteArray) throws IOException {
        return new Yaml(new Constructor(YamlOrchestrationMasterSlaveRuleConfiguration.class)).loadAs(new ByteArrayInputStream(yamlByteArray), YamlOrchestrationMasterSlaveRuleConfiguration.class);
    }
}
