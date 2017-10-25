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
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationMasterSlaveConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.state.InstanceStateService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Orchestration master slave data source factory.
 * 
 * @author zhangliang 
 * @author caohao  
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationMasterSlaveDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param config orchestration master slave configuration
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationMasterSlaveConfiguration config) throws SQLException {
        config.getRegistryCenter().init();
        MasterSlaveDataSource result = (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(config.getDataSourceMap(), config.getMasterSlaveRuleConfiguration());
        new ConfigurationService(config.getRegistryCenter(), config.getName()).addMasterSlaveConfiguration(config, result);
        new InstanceStateService(config.getRegistryCenter(), config.getName()).addMasterSlaveState(result);
        return result;
    }
}
