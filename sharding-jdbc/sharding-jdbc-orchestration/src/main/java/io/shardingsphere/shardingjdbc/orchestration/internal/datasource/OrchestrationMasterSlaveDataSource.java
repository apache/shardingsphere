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
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.config.OrchestrationType;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.orchestration.internal.event.config.MasterSlaveConfigurationEventBusEvent;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.datasource.CircuitBreakerDataSource;
import io.shardingsphere.orchestration.internal.rule.OrchestrationMasterSlaveRule;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Orchestration master-slave datasource.
 *
 * @author panjuan
 */
public class OrchestrationMasterSlaveDataSource extends AbstractOrchestrationDataSource {
    
    private MasterSlaveDataSource dataSource;
    
    public OrchestrationMasterSlaveDataSource(final MasterSlaveDataSource masterSlaveDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig), masterSlaveDataSource.getDataSourceMap());
        dataSource = new MasterSlaveDataSource(masterSlaveDataSource.getDataSourceMap(), new OrchestrationMasterSlaveRule(masterSlaveDataSource.getMasterSlaveRule().getMasterSlaveRuleConfiguration()),
                ConfigMapContext.getInstance().getMasterSlaveConfig(), masterSlaveDataSource.getShardingProperties());
        initOrchestrationFacade(dataSource);
    }
    
    public OrchestrationMasterSlaveDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new OrchestrationFacade(orchestrationConfig));
        ConfigurationService configService = getOrchestrationFacade().getConfigService();
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = configService.loadMasterSlaveRuleConfiguration();
        Preconditions.checkState(null != masterSlaveRuleConfig && !Strings.isNullOrEmpty(masterSlaveRuleConfig.getMasterDataSourceName()), "No available master slave rule configuration to load.");
        dataSource = new MasterSlaveDataSource(configService.loadDataSourceMap(), new OrchestrationMasterSlaveRule(masterSlaveRuleConfig), configService.loadMasterSlaveConfigMap(),
                new ShardingProperties(configService.loadMasterSlaveProperties()));
        getOrchestrationFacade().init(OrchestrationType.MASTER_SLAVE);
    }
    
    private void initOrchestrationFacade(final MasterSlaveDataSource masterSlaveDataSource) {
        MasterSlaveRule masterSlaveRule = masterSlaveDataSource.getMasterSlaveRule();
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = new MasterSlaveRuleConfiguration(
                masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), masterSlaveRule.getSlaveDataSourceNames(), masterSlaveRule.getLoadBalanceAlgorithm());
        getOrchestrationFacade().init(masterSlaveDataSource.getDataSourceMap(), 
                masterSlaveRuleConfiguration, ConfigMapContext.getInstance().getMasterSlaveConfig(), masterSlaveDataSource.getShardingProperties().getProps());
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
    public void renew(final MasterSlaveConfigurationEventBusEvent masterSlaveEvent) throws SQLException {
        dataSource.close();
        dataSource = new MasterSlaveDataSource(
                masterSlaveEvent.getDataSourceMap(), masterSlaveEvent.getMasterSlaveRuleConfig(), ConfigMapContext.getInstance().getMasterSlaveConfig(), masterSlaveEvent.getProps());
    }
}
