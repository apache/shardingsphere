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
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding datasource.
 *
 * @author caohao
 */
@Slf4j
public final class OrchestrationShardingDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private final ShardingDataSource dataSource;
    
    private final OrchestrationFacade orchestrationFacade;
    
    
    public OrchestrationShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig,
                                           final Map<String, Object> configMap, final Properties props, final OrchestrationFacade orchestrationFacade) throws SQLException {
        super(dataSourceMap.values());
        this.dataSource = new ShardingDataSource(getRawDataSourceMap(dataSourceMap), new ShardingRule(getShardingRuleConfiguration(dataSourceMap, shardingRuleConfig), getRawDataSourceMap(dataSourceMap).keySet()), configMap, props);
        this.orchestrationFacade = orchestrationFacade;
        this.orchestrationFacade.init(dataSourceMap, shardingRuleConfig, configMap, props);
    }
    
    @Override
    public ShardingConnection getConnection() {
        return dataSource.getConnection();
    }
    
    @Override
    public void close() {
        dataSource.close();
        orchestrationFacade.close();
    }
    
    private Map<String, DataSource> getRawDataSourceMap(final Map<String, DataSource> dataSourceMap) {
        Map<String, DataSource> result = new LinkedHashMap<>();
        if (null == dataSourceMap) {
            return result;
        }
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            String dataSourceName = entry.getKey();
            DataSource dataSource = entry.getValue();
            if (dataSource instanceof MasterSlaveDataSource) {
                result.putAll(((MasterSlaveDataSource) dataSource).getAllDataSources());
            } else {
                result.put(dataSourceName, dataSource);
            }
        }
        return result;
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = new LinkedList<>();
        if (null == dataSourceMap || !shardingRuleConfig.getMasterSlaveRuleConfigs().isEmpty()) {
            return shardingRuleConfig;
        }
        for (DataSource each : dataSourceMap.values()) {
            if (!(each instanceof MasterSlaveDataSource)) {
                continue;
            }
            MasterSlaveRule masterSlaveRule = ((MasterSlaveDataSource) each).getMasterSlaveRule();
            masterSlaveRuleConfigs.add(new MasterSlaveRuleConfiguration(
                    masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), masterSlaveRule.getSlaveDataSourceNames(), masterSlaveRule.getLoadBalanceAlgorithm()));
        }
        shardingRuleConfig.setMasterSlaveRuleConfigs(masterSlaveRuleConfigs);
        return shardingRuleConfig;
    }
}
