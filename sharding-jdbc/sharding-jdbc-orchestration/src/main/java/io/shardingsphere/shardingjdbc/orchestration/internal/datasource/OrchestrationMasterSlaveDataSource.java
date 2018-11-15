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

package io.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.orchestration.internal.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.config.event.MasterSlaveRuleChangedEvent;
import io.shardingsphere.orchestration.internal.config.event.PropertiesChangedEvent;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestration master-slave datasource.
 *
 * @author panjuan
 */
public class OrchestrationMasterSlaveDataSource extends AbstractOrchestrationDataSource {
    
    private MasterSlaveDataSource dataSource;
    
    public OrchestrationMasterSlaveDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        ConfigurationService configService = getOrchestrationFacade().getConfigService();
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = configService.loadMasterSlaveRuleConfiguration(ShardingConstant.LOGIC_SCHEMA_NAME);
        Preconditions.checkState(null != masterSlaveRuleConfig && !Strings.isNullOrEmpty(masterSlaveRuleConfig.getMasterDataSourceName()), "No available master slave rule configuration to load.");
        dataSource = new MasterSlaveDataSource(DataSourceConverter.getDataSourceMap(configService.loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME)),
                new OrchestrationMasterSlaveRule(masterSlaveRuleConfig), configService.loadConfigMap(), configService.loadProperties());
        getOrchestrationFacade().init();
    }
    
    public OrchestrationMasterSlaveDataSource(final MasterSlaveDataSource masterSlaveDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)), masterSlaveDataSource.getDataSourceMap());
        dataSource = new MasterSlaveDataSource(masterSlaveDataSource.getDataSourceMap(),
                new OrchestrationMasterSlaveRule(masterSlaveDataSource.getMasterSlaveRule().getMasterSlaveRuleConfiguration()),
                ConfigMapContext.getInstance().getConfigMap(), masterSlaveDataSource.getShardingProperties().getProps());
        getOrchestrationFacade().init(Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, DataSourceConverter.getDataSourceConfigurationMap(dataSource.getDataSourceMap())),
                getRuleConfigurationMap(), null, ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingProperties().getProps());
    }
    
    private Map<String, RuleConfiguration> getRuleConfigurationMap() {
        MasterSlaveRule masterSlaveRule = dataSource.getMasterSlaveRule();
        Map<String, RuleConfiguration> result = new HashMap<>();
        result.put(ShardingConstant.LOGIC_SCHEMA_NAME, new MasterSlaveRuleConfiguration(
                masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), masterSlaveRule.getSlaveDataSourceNames(), masterSlaveRule.getLoadBalanceAlgorithm()));
        return result;
    }
    
    @Override
    public final Connection getConnection() {
        return isCircuitBreak() ? new CircuitBreakerDataSource().getConnection() : dataSource.getConnection();
    }
    
    @Override
    public final void close() {
        dataSource.close();
        getOrchestrationFacade().close();
    }
    
    /**
     * Renew master-slave rule.
     *
     * @param masterSlaveEvent master-slave configuration changed event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public final void renew(final MasterSlaveRuleChangedEvent masterSlaveEvent) throws SQLException {
        dataSource = new MasterSlaveDataSource(dataSource.getDataSourceMap(),
                masterSlaveEvent.getMasterSlaveRuleConfig(), ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingProperties().getProps());
    }
    
    /**
     * Renew master-slave data source.
     *
     * @param dataSourceChangedEvent data source changed event
     */
    @Subscribe
    @SneakyThrows
    public final void renew(final DataSourceChangedEvent dataSourceChangedEvent) {
        Map<String, DataSourceConfiguration> originalDataSourceConfigurations = DataSourceConverter.getDataSourceConfigurationMap(dataSource.getDataSourceMap());
        Map<String, DataSourceConfiguration> newDataSourceConfigurations = dataSourceChangedEvent.getDataSourceConfigurations();
        Map<String, DataSource> result = new LinkedHashMap<>();
        for (String each : originalDataSourceConfigurations.keySet()) {
            if (originalDataSourceConfigurations.get(each).equals(newDataSourceConfigurations.get(each))) {
                result.put(each, dataSource.getDataSourceMap().get(each));
                newDataSourceConfigurations.remove(each);
            }
        }
        result.putAll(DataSourceConverter.getDataSourceMap(newDataSourceConfigurations));
        
        dataSource.close();
        dataSource = new MasterSlaveDataSource(DataSourceConverter.getDataSourceMap(dataSourceChangedEvent.getDataSourceConfigurations()),
                dataSource.getMasterSlaveRule(), ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingProperties().getProps());
    }
    
    /**
     * Renew properties.
     *
     * @param propertiesEvent properties event
     */
    @SneakyThrows
    @Subscribe
    public final void renew(final PropertiesChangedEvent propertiesEvent) {
        dataSource = new MasterSlaveDataSource(dataSource.getDataSourceMap(), dataSource.getMasterSlaveRule(), ConfigMapContext.getInstance().getConfigMap(), propertiesEvent.getProps());
    }
}
