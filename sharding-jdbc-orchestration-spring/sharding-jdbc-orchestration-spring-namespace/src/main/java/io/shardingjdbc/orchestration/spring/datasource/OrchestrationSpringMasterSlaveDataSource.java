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
import io.shardingjdbc.orchestration.reg.api.RegistryCenterConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Orchestration master-slave datasource for spring namespace.
 *
 * @author caohao
 * @author zhangliang
 */
public class OrchestrationSpringMasterSlaveDataSource extends MasterSlaveDataSource {
    
    public OrchestrationSpringMasterSlaveDataSource(final String name, final boolean overwrite, final RegistryCenterConfiguration regCenterConfig, 
                                                    final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                                    final Map<String, Object> configMap) throws SQLException {
        super(getOrchestrationFacade(name, overwrite, regCenterConfig).loadMasterSlaveRuleConfiguration(masterSlaveRuleConfig).build(
                getOrchestrationFacade(name, overwrite, regCenterConfig).loadDataSourceMap(dataSourceMap)),
                getOrchestrationFacade(name, overwrite, regCenterConfig).loadMasterSlaveConfigMap(configMap));
        getOrchestrationFacade(name, overwrite, regCenterConfig).getOrchestrationMasterSlaveDataSource(dataSourceMap, masterSlaveRuleConfig, configMap);
    }
    
    private static OrchestrationFacade getOrchestrationFacade(final String name, final boolean overwrite, final RegistryCenterConfiguration regCenterConfig) {
        return new OrchestrationFacade(new OrchestrationConfiguration(name, regCenterConfig, overwrite));
    }
}
