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

package io.shardingjdbc.core.api;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.yaml.masterslave.YamlMasterSlaveConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Master-slave data source factory.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap configuration map
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap) throws SQLException {
        return new MasterSlaveDataSource(dataSourceMap, masterSlaveRuleConfig, configMap);
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
        YamlMasterSlaveConfiguration config = YamlMasterSlaveConfiguration.unmarshal(yamlFile);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getMasterSlaveRule().getConfigMap());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param yamlBytes yaml bytes for master-slave rule configuration with data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        YamlMasterSlaveConfiguration config = YamlMasterSlaveConfiguration.unmarshal(yamlBytes);
        return createDataSource(config.getDataSources(), config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getMasterSlaveRule().getConfigMap());
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
        YamlMasterSlaveConfiguration config = YamlMasterSlaveConfiguration.unmarshal(yamlFile);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getMasterSlaveRule().getConfigMap());
    }
    
    /**
     * Create master-slave data source.
     *
     * <p>One master data source can configure multiple slave data source.</p>
     *
     * @param dataSourceMap data source map
     * @param yamlBytes yaml bytes for master-slave rule configuration without data sources
     * @return master-slave data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlMasterSlaveConfiguration config = YamlMasterSlaveConfiguration.unmarshal(yamlBytes);
        return createDataSource(dataSourceMap, config.getMasterSlaveRule().getMasterSlaveRuleConfiguration(), config.getMasterSlaveRule().getConfigMap());
    }
}
