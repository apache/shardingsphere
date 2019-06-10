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
import lombok.AccessLevel;
import lombok.Getter;

import org.apache.shardingsphere.api.config.RuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.internal.registry.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.internal.registry.config.service.ConfigurationService;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * Orchestration encrypt datasource.
 *
 * @author yangyi
 */
@Getter(AccessLevel.PROTECTED)
public class OrchestrationEncryptDataSource extends AbstractOrchestrationDataSource {
    
    private static final String ENCRYPT_DATASOURCE = "encrypt-datasource";
    
    private final EncryptDataSource dataSource;
    
    public OrchestrationEncryptDataSource(final OrchestrationConfiguration orchestrationConfig) {
        super(new ShardingOrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        ConfigurationService configService = getShardingOrchestrationFacade().getConfigService();
        EncryptRuleConfiguration encryptRuleConfiguration = configService.loadEncryptRuleConfiguration(ShardingConstant.LOGIC_SCHEMA_NAME);
        Preconditions.checkState(!encryptRuleConfiguration.getEncryptorRuleConfigs().isEmpty(), "No available encrypt rule configuration to load.");
        Map<String, DataSource> dataSourceMap = DataSourceConverter.getDataSourceMap(configService.loadDataSourceConfigurations(ShardingConstant.LOGIC_SCHEMA_NAME));
        Preconditions.checkState(1 == dataSourceMap.size(), String.format("There should be only one datasource for encrypt, but now has %d datasource(s)", dataSourceMap.size()));
        dataSource = new EncryptDataSource(dataSourceMap.values().iterator().next(), encryptRuleConfiguration);
        initShardingOrchestrationFacade();
    }
    
    public OrchestrationEncryptDataSource(final EncryptDataSource dataSource, final OrchestrationConfiguration orchestrationConfig) {
        super(new ShardingOrchestrationFacade(orchestrationConfig, Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME)));
        this.dataSource = new EncryptDataSource(dataSource.getDataSource(), dataSource.getEncryptRule().getEncryptRuleConfig());
        initShardingOrchestrationFacade(
            Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, DataSourceConverter.getDataSourceConfigurationMap(Collections.singletonMap(ENCRYPT_DATASOURCE, dataSource.getDataSource()))),
            getRuleConfigurationMap(), new Properties());
    }
    
    private Map<String, RuleConfiguration> getRuleConfigurationMap() {
        Map<String, RuleConfiguration> result = new HashMap<>(1);
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration();
        for (Entry<String, EncryptorRuleConfiguration> entry : dataSource.getEncryptRule().getEncryptRuleConfig().getEncryptorRuleConfigs().entrySet()) {
            encryptRuleConfig.getEncryptorRuleConfigs().put(entry.getKey(),
                new EncryptorRuleConfiguration(entry.getValue().getType(), entry.getValue().getQualifiedColumns(), entry.getValue().getAssistedQueryColumns(), entry.getValue().getProperties()));
        }
        result.put(ShardingConstant.LOGIC_SCHEMA_NAME, encryptRuleConfig);
        return result;
    }
}
