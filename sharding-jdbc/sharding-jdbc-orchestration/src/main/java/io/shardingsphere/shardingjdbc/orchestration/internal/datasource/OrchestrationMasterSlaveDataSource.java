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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.orchestration.internal.event.config.MasterSlaveConfigurationDataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.uilt.DataSourceConverter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Orchestration master-slave datasource.
 *
 * @author panjuan
 */
public class OrchestrationMasterSlaveDataSource extends AbstractOrchestrationDataSource {
    
    private MasterSlaveDataSource dataSource;
    
    public OrchestrationMasterSlaveDataSource(final MasterSlaveDataSource masterSlaveDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)), masterSlaveDataSource.getDataSourceMap());
        dataSource = new MasterSlaveDataSource(masterSlaveDataSource.getDataSourceMap(), 
                new OrchestrationMasterSlaveRule(masterSlaveDataSource.getMasterSlaveRule().getMasterSlaveRuleConfiguration()),
                ConfigMapContext.getInstance().getConfigMap(), masterSlaveDataSource.getShardingProperties());
        initOrchestrationFacade();
    }
    
    public OrchestrationMasterSlaveDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        ConfigurationService configService = getOrchestrationFacade().getConfigService();
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = configService.loadMasterSlaveRuleConfiguration(ShardingConstant.LOGIC_SCHEMA_NAME);
        Preconditions.checkState(null != masterSlaveRuleConfig && !Strings.isNullOrEmpty(masterSlaveRuleConfig.getMasterDataSourceName()), "No available master slave rule configuration to load.");
        dataSource = new MasterSlaveDataSource(DataSourceConverter.getDataSourceMap(configService.loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME)),
                new OrchestrationMasterSlaveRule(masterSlaveRuleConfig), configService.loadConfigMap(), new ShardingProperties(configService.loadProperties()));
        getOrchestrationFacade().getListenerManager().initMasterSlaveListeners();
    }
    
    private void initOrchestrationFacade() {
        MasterSlaveRule masterSlaveRule = dataSource.getMasterSlaveRule();
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = new MasterSlaveRuleConfiguration(
                masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), masterSlaveRule.getSlaveDataSourceNames(), masterSlaveRule.getLoadBalanceAlgorithm());
        getOrchestrationFacade().init(ShardingConstant.LOGIC_SCHEMA_NAME, getDataSourceConfigurationMap(),
                masterSlaveRuleConfiguration, ConfigMapContext.getInstance().getConfigMap(), dataSource.getShardingProperties().getProps());
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurationMap() {
        return Maps.transformValues(dataSource.getDataSourceMap(), new Function<DataSource, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final DataSource input) {
                return DataSourceConfiguration.getDataSourceConfiguration(input);
            }
        });
    }
    
    @Override
    public final Connection getConnection() {
        if (isCircuitBreak()) {
            return new CircuitBreakerDataSource().getConnection();
        }
        return dataSource.getConnection();
    }
    
    @Override
    public final void close() {
        dataSource.close();
        getOrchestrationFacade().close();
    }
    
    /**
     * Renew master-slave data source.
     *
     * @param masterSlaveEvent master slave configuration event bus event
     * @throws SQLException sql exception
     */
    @Subscribe
    public void renew(final MasterSlaveConfigurationDataSourceChangedEvent masterSlaveEvent) throws SQLException {
        dataSource.close();
        dataSource = new MasterSlaveDataSource(
                masterSlaveEvent.getDataSourceMap(), masterSlaveEvent.getMasterSlaveRuleConfig(), ConfigMapContext.getInstance().getConfigMap(), masterSlaveEvent.getProps());
    }
}
