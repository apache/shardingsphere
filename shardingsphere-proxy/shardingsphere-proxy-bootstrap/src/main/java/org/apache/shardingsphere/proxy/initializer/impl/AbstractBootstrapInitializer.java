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

package org.apache.shardingsphere.proxy.initializer.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.manager.ContextManagerBuilder;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Abstract bootstrap initializer.
 */
@Slf4j
public abstract class AbstractBootstrapInitializer implements BootstrapInitializer {
    
    private final ShardingSphereMode mode;
    
    @Getter
    private final DistMetaDataPersistService distMetaDataPersistService;
    
    public AbstractBootstrapInitializer(final ShardingSphereMode mode) {
        this.mode = mode;
        distMetaDataPersistService = mode.getPersistRepository().isPresent() ? new DistMetaDataPersistService(mode.getPersistRepository().get()) : null;
    }
    
    @Override
    public final void init(final YamlProxyConfiguration yamlConfig) throws SQLException {
        ProxyConfiguration proxyConfig = new YamlProxyConfigurationSwapper().swap(yamlConfig);
        boolean isOverwrite = null == yamlConfig.getServerConfiguration().getMode() || yamlConfig.getServerConfiguration().getMode().isOverwrite();
        ContextManager contextManager = createContextManagerBuilder().build(
                mode, getDataSourcesMap(proxyConfig.getSchemaDataSources()), proxyConfig.getSchemaRules(), proxyConfig.getGlobalRules(), proxyConfig.getProps(), isOverwrite);
        ProxyContext.getInstance().init(contextManager);
        setDatabaseServerInfo();
        initScalingInternal(yamlConfig);
    }
    
    protected abstract ContextManagerBuilder createContextManagerBuilder();
    
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
    
    protected final Optional<ServerConfiguration> getScalingConfiguration(final YamlProxyConfiguration yamlConfig) {
        if (null == yamlConfig.getServerConfiguration().getScaling()) {
            return Optional.empty();
        }
        ServerConfiguration result = new ServerConfiguration();
        result.setBlockQueueSize(yamlConfig.getServerConfiguration().getScaling().getBlockQueueSize());
        result.setWorkerThread(yamlConfig.getServerConfiguration().getScaling().getWorkerThread());
        return Optional.of(result);
    }
    
    private void initScalingInternal(final YamlProxyConfiguration yamlConfig) {
        log.debug("Init scaling");
        initScaling(yamlConfig);
    }
    
    protected abstract void initScaling(YamlProxyConfiguration yamlConfig);
}
