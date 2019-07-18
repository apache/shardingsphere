/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.internal.registry.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.EncryptRuleChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestration encrypt datasource.
 *
 * @author yangyi
 */
@Getter(AccessLevel.PROTECTED)
public class OrchestrationEncryptDataSource extends AbstractOrchestrationDataSource {
    
    private static final String ENCRYPT_DATASOURCE = "dataSource";
    
    private EncryptDataSource dataSource;
    
    public OrchestrationEncryptDataSource(final OrchestrationConfiguration orchestrationConfig) {
        super(new ShardingOrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        ConfigurationService configService = getShardingOrchestrationFacade().getConfigService();
        EncryptRuleConfiguration encryptRuleConfiguration = configService.loadEncryptRuleConfiguration(ShardingConstant.LOGIC_SCHEMA_NAME);
        Preconditions.checkState(!encryptRuleConfiguration.getEncryptors().isEmpty(), "No available encrypt rule configuration to load.");
        Map<String, DataSourceConfiguration> dataSourceConfigurations = configService.loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME);
        checkDataSourceConfiguration(dataSourceConfigurations);
        dataSource = new EncryptDataSource(DataSourceConverter.getDataSourceMap(dataSourceConfigurations).values().iterator().next(), encryptRuleConfiguration, configService.loadProperties());
        initShardingOrchestrationFacade();
    }
    
    public OrchestrationEncryptDataSource(final EncryptDataSource dataSource, final OrchestrationConfiguration orchestrationConfig) {
        super(new ShardingOrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        this.dataSource = new EncryptDataSource(dataSource.getDataSource(), dataSource.getEncryptRule().getEncryptRuleConfig(), dataSource.getShardingProperties().getProps());
        initShardingOrchestrationFacade(
            Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, DataSourceConverter.getDataSourceConfigurationMap(Collections.singletonMap(ENCRYPT_DATASOURCE, dataSource.getDataSource()))),
            getRuleConfigurationMap(), dataSource.getShardingProperties().getProps());
    }
    
    private void checkDataSourceConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Preconditions.checkState(1 == dataSourceConfigurations.size(), String.format("There should be only one datasource for encrypt, but now has %d datasource(s)", dataSourceConfigurations.size()));
    }
    
    private Map<String, RuleConfiguration> getRuleConfigurationMap() {
        Map<String, RuleConfiguration> result = new HashMap<>(1);
        result.put(ShardingConstant.LOGIC_SCHEMA_NAME, dataSource.getEncryptRule().getEncryptRuleConfig());
        return result;
    }
    
    /**
     * Renew encrypt data source.
     *
     * @param dataSourceChangedEvent data source changed event
     */
    @Subscribe
    @SneakyThrows
    public final synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) {
        Map<String, DataSourceConfiguration> dataSourceConfigurations = dataSourceChangedEvent.getDataSourceConfigurations();
        dataSource.close();
        checkDataSourceConfiguration(dataSourceConfigurations);
        dataSource = new EncryptDataSource(DataSourceConverter.getDataSourceMap(dataSourceConfigurations).values().iterator().next(),
            dataSource.getEncryptRule().getEncryptRuleConfig(), dataSource.getShardingProperties().getProps());
        getDataSourceConfigurations().clear();
        getDataSourceConfigurations().putAll(dataSourceConfigurations);
    }
    
    /**
     * Renew encrypt rule.
     *
     * @param encryptRuleChangedEvent encrypt configuration changed event
     */
    @Subscribe
    @SneakyThrows
    public final synchronized void renew(final EncryptRuleChangedEvent encryptRuleChangedEvent) {
        dataSource = new EncryptDataSource(dataSource.getDataSource(), encryptRuleChangedEvent.getEncryptRuleConfiguration(), dataSource.getShardingProperties().getProps());
    }
    
    /**
     * Renew properties.
     *
     * @param propertiesChangedEvent properties changed event
     */
    @SneakyThrows
    @Subscribe
    public final synchronized void renew(final PropertiesChangedEvent propertiesChangedEvent) {
        dataSource = new EncryptDataSource(dataSource.getDataSource(), dataSource.getEncryptRule().getEncryptRuleConfig(), propertiesChangedEvent.getProps());
    }
}
