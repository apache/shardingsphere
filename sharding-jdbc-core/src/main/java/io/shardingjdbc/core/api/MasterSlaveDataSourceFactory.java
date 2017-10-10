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

package io.shardingjdbc.core.api;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.yaml.masterslave.YamlMasterSlaveDataSource;
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
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig) throws SQLException {
        return new MasterSlaveDataSource(masterSlaveRuleConfig.build(dataSourceMap));
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
        return new YamlMasterSlaveDataSource(yamlFile);
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
        return new YamlMasterSlaveDataSource(dataSourceMap, yamlFile);
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
        return new YamlMasterSlaveDataSource(yamlByteArray);
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
        return new YamlMasterSlaveDataSource(dataSourceMap, yamlByteArray);
    }
}
