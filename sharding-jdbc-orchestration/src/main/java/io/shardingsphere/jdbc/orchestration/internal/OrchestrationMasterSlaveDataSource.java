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

import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.connection.MasterSlaveConnection;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration master-slave datasource.
 *
 * @author caohao
 * @author panjuan
 */
@Slf4j
public final class OrchestrationMasterSlaveDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private final OrchestrationFacade orchestrationFacade;
    
    private final MasterSlaveDataSource dataSource;
    
    private Collection<String> disabledDataSourceNames = new LinkedList<>();
    
    private boolean isCircuitBreak;
    
    public OrchestrationMasterSlaveDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                              final Map<String, Object> configMap, final Properties props, final OrchestrationFacade orchestrationFacade) throws SQLException {
        super(getAllDataSources(dataSourceMap, masterSlaveRuleConfig.getMasterDataSourceName(), masterSlaveRuleConfig.getSlaveDataSourceNames()));
        this.dataSource = new MasterSlaveDataSource(dataSourceMap, masterSlaveRuleConfig, configMap, props);
        this.orchestrationFacade = orchestrationFacade;
    }
    
    private static Collection<DataSource> getAllDataSources(final Map<String, DataSource> dataSourceMap, final String masterDataSourceName, final Collection<String> slaveDataSourceNames) {
        Collection<DataSource> result = new LinkedList<>();
        result.add(dataSourceMap.get(masterDataSourceName));
        for (String each : slaveDataSourceNames) {
            result.add(dataSourceMap.get(each));
        }
        return result;
    }
    
    /**
     * Initialize for master-slave orchestration.
     */
    public void init() {
        dataSource.close();
        orchestrationFacade.init(dataSource.getDataSourceMap(), new MasterSlaveRuleConfiguration(dataSource.getMasterSlaveRule()), ConfigMapContext.getInstance().getMasterSlaveConfig(), dataSource.getShardingProperties().getProps());
    }
    
    @Override
    public MasterSlaveConnection getConnection() {
        return dataSource.getConnection();
    }
    
    @Override
    public void close() {
        orchestrationFacade.close();
    }
}
