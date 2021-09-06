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

package org.apache.shardingsphere.proxy.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingWorker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Bootstrap initializer.
 */
@RequiredArgsConstructor
@Slf4j
public final class BootstrapInitializer {
    
    /**
     * Initialize.
     *
     * @param yamlConfig YAML proxy configuration
     * @throws SQLException SQL exception
     */
    public void init(final YamlProxyConfiguration yamlConfig) throws SQLException {
        ModeConfiguration modeConfig = null == yamlConfig.getServerConfiguration().getMode()
                ? null : new ModeConfigurationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getMode());
        initContext(yamlConfig, modeConfig);
        setDatabaseServerInfo();
        initScaling(yamlConfig, modeConfig);
    }
    
    private void initContext(final YamlProxyConfiguration yamlConfig, final ModeConfiguration modeConfig) throws SQLException {
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        boolean isOverwrite = null == modeConfig || modeConfig.isOverwrite();
        Map<String, Map<String, DataSource>> dataSourcesMap = getDataSourcesMap(proxyConfig.getSchemaDataSources());
        ContextManager contextManager = ContextManagerBuilderFactory.newInstance(modeConfig).build(modeConfig, dataSourcesMap,
                proxyConfig.getSchemaRules(), proxyConfig.getGlobalRules(), proxyConfig.getProps(), isOverwrite);
        ProxyContext.getInstance().init(contextManager);
    }
    
    // TODO add DataSourceParameter param to ContextManagerBuilder to avoid re-build data source
    private Map<String, Map<String, DataSource>> getDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>(dataSourceParametersMap.size(), 1);
        for (Entry<String, Map<String, DataSourceParameter>> entry : dataSourceParametersMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceMap(DataSourceParameterConverter.getDataSourceConfigurationMap(entry.getValue())));
        }
        return result;
    }
    
    private Map<String, DataSource> getDataSourceMap(final Map<String, DataSourceConfiguration> dataSourceConfigMap) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceConfigMap.size(), 1);
        for (Entry<String, DataSourceConfiguration> entry : dataSourceConfigMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue().createDataSource());
        }
        return result;
    }
    
    private void setDatabaseServerInfo() {
        findBackendDataSource().ifPresent(dataSourceSample -> {
            DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSourceSample);
            log.info(databaseServerInfo.toString());
            switch (databaseServerInfo.getDatabaseName()) {
                case "MySQL":
                    MySQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
                    break;
                case "PostgreSQL":
                    PostgreSQLServerInfo.setServerVersion(databaseServerInfo.getDatabaseVersion());
                    break;
                default:
            }
        });
    }
    
    private Optional<DataSource> findBackendDataSource() {
        for (String each : ProxyContext.getInstance().getAllSchemaNames()) {
            return ProxyContext.getInstance().getMetaData(each).getResource().getDataSources().values().stream().findFirst();
        }
        return Optional.empty();
    }
    
    private void initScaling(final YamlProxyConfiguration yamlConfig, final ModeConfiguration modeConfig) {
        Optional<ServerConfiguration> scalingConfig = findScalingConfiguration(yamlConfig);
        if (!scalingConfig.isPresent()) {
            return;
        }
        // TODO decouple "Cluster" to pluggable
        if (null != modeConfig && "Cluster".equals(modeConfig.getType())) {
            scalingConfig.get().setModeConfiguration(modeConfig);
            ScalingContext.getInstance().init(scalingConfig.get());
            ScalingWorker.init(); 
        } else {
            ScalingContext.getInstance().init(scalingConfig.get());
        }
    }
    
    private Optional<ServerConfiguration> findScalingConfiguration(final YamlProxyConfiguration yamlConfig) {
        if (null == yamlConfig.getServerConfiguration().getScaling()) {
            return Optional.empty();
        }
        
        ServerConfiguration result = new ServerConfiguration();
        result.setBlockQueueSize(yamlConfig.getServerConfiguration().getScaling().getBlockQueueSize());
        result.setWorkerThread(yamlConfig.getServerConfiguration().getScaling().getWorkerThread());
        YamlShardingSphereAlgorithmConfiguration autoSwitchConfig = yamlConfig.getServerConfiguration().getScaling().getClusterAutoSwitchAlgorithm();
        if (null != autoSwitchConfig) {
            result.setClusterAutoSwitchAlgorithm(new ShardingSphereAlgorithmConfiguration(autoSwitchConfig.getType(), autoSwitchConfig.getProps()));
        }
        return Optional.of(result);
    }
}
