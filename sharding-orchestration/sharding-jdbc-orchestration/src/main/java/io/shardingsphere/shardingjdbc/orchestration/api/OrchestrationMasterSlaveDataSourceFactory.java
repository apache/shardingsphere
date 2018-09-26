/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingjdbc.orchestration.api;

import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding data source factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationMasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap config map
     * @param props properties
     * @param orchestrationConfig orchestration configuration
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap, 
                                              final Properties props, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        if (null == masterSlaveRuleConfig || null == masterSlaveRuleConfig.getMasterDataSourceName()) {
            return createDataSource(orchestrationConfig);
        }
        MasterSlaveDataSource masterSlaveDataSource = new MasterSlaveDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, props);
        return new OrchestrationMasterSlaveDataSource(masterSlaveDataSource, orchestrationConfig);
    }
    
    /**
     * Create master-slave data source.
     *
     * @param orchestrationConfig orchestration configuration
     * @return master-slave data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        return new OrchestrationMasterSlaveDataSource(orchestrationConfig);
    }
}
