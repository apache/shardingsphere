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

package io.shardingjdbc.orchestration.api.datasource;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationMasterSlaveConfiguration;
import io.shardingjdbc.orchestration.internal.config.ConfigurationService;
import io.shardingjdbc.orchestration.internal.state.InstanceStateService;
import lombok.Getter;

import java.sql.SQLException;

/**
 * Orchestration master slave data source.
 *
 * @author caohao
 */
@Getter
public class OrchestrationMasterSlaveDataSource {
    
    private final OrchestrationMasterSlaveConfiguration config;
    
    private final MasterSlaveDataSource dataSource;
    
    private final ConfigurationService configurationService;
    
    private final InstanceStateService instanceStateService;
    
    public OrchestrationMasterSlaveDataSource(final OrchestrationMasterSlaveConfiguration config) throws SQLException {
        config.getRegistryCenter().init();
        this.config = config;
        dataSource = (MasterSlaveDataSource) MasterSlaveDataSourceFactory.createDataSource(config.getDataSourceMap(), config.getMasterSlaveRuleConfiguration());
        configurationService = new ConfigurationService(config.getName(), config.getRegistryCenter());
        instanceStateService = new InstanceStateService(config.getName(), config.getRegistryCenter());
    }
    
    /**
     * Initial orchestration master-slave data source.
     */
    public void init() {
        configurationService.persistMasterSlaveConfiguration(config);
        configurationService.addMasterSlaveConfigurationChangeListener(dataSource);
        instanceStateService.addMasterSlaveState(dataSource);
    }
}
