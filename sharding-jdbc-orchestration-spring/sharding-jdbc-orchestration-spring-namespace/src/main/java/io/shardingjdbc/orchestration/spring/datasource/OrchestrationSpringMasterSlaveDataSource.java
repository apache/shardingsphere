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

package io.shardingjdbc.orchestration.spring.datasource;

import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationFacade;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Orchestration master-slave datasource for spring namespace.
 *
 * @author caohao
 */
public class OrchestrationSpringMasterSlaveDataSource extends MasterSlaveDataSource {
    
    private final OrchestrationConfiguration config;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final MasterSlaveRuleConfiguration masterSlaveRuleConfig;
    
    private final Map<String, Object> configMap;
    
    public OrchestrationSpringMasterSlaveDataSource(final String name, final boolean overwrite, final CoordinatorRegistryCenter registryCenter, 
                                                    final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                                    final Map<String, Object> configMap) throws SQLException {
        super(masterSlaveRuleConfig.build(dataSourceMap), configMap);
        this.dataSourceMap = dataSourceMap;
        this.masterSlaveRuleConfig = masterSlaveRuleConfig;
        this.configMap = configMap;
        config = new OrchestrationConfiguration(name, registryCenter, overwrite);
    }
    
    /**
     * Initial all orchestration actions for master-slave data source.
     */
    public void init() {
        new OrchestrationFacade(config).initMasterSlaveOrchestration(dataSourceMap, masterSlaveRuleConfig, this, configMap);
    }
}
