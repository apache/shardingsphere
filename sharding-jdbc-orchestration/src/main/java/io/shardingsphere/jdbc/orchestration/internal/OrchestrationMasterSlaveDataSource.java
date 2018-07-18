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

package io.shardingsphere.jdbc.orchestration.internal;

import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration master-slave datasource.
 *
 * @author caohao
 */
@Slf4j
public class OrchestrationMasterSlaveDataSource extends MasterSlaveDataSource implements AutoCloseable {
    
    private final OrchestrationFacade orchestrationFacade;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final MasterSlaveRuleConfiguration masterSlaveRuleConfig;
    
    private final Map<String, Object> configMap;
    
    private final Properties props;
    
    public OrchestrationMasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                              final Map<String, Object> configMap, final Properties props, final OrchestrationFacade orchestrationFacade) throws SQLException {
        super(dataSourceMap, masterSlaveRuleConfig, configMap, props);
        this.orchestrationFacade = orchestrationFacade;
        this.dataSourceMap = dataSourceMap;
        this.masterSlaveRuleConfig = masterSlaveRuleConfig;
        this.configMap = configMap;
        this.props = props;
    }
    
    /**
     * Initialize for master-slave orchestration.
     */
    public void init() {
        orchestrationFacade.init(dataSourceMap, masterSlaveRuleConfig, configMap, props, this);
    }
    
    @Override
    public void close() {
        orchestrationFacade.close();
    }
}
