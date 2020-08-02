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

package org.apache.shardingsphere.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.control.panel.spi.FacadeConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.apache.shardingsphere.control.panel.spi.opentracing.OpenTracingConfiguration;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.constant.Constants;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.kernel.context.SchemaContextsAware;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.proxy.arg.BootstrapArguments;
import org.apache.shardingsphere.proxy.backend.schema.ProxyDataSourceContext;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.proxy.frontend.bootstrap.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.orchestration.OrchestrationBootstrap;
import org.apache.shardingsphere.proxy.orchestration.schema.ProxyOrchestrationSchemaContexts;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws Exception exception
     */
    public static void main(final String[] args) throws Exception {
        BootstrapArguments bootstrapArgs = new BootstrapArguments(args);
        int port = bootstrapArgs.getPort();
        System.setProperty(Constants.PORT_KEY, String.valueOf(port));
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(bootstrapArgs.getConfigurationPath());
        if (null == yamlConfig.getServerConfiguration().getOrchestration()) {
            init(new YamlProxyConfigurationSwapper().swap(yamlConfig), port, false);
        } else {
            try (OrchestrationFacade orchestrationFacade = OrchestrationFacade.getInstance()) {
                init(new OrchestrationBootstrap(orchestrationFacade).init(yamlConfig), port, true);
            }
        }
    }
    
    private static void init(final ProxyConfiguration proxyConfig, final int port, final boolean orchestrationEnabled) throws SQLException {
        log(proxyConfig);
        initProxySchemaContexts(proxyConfig, orchestrationEnabled);
        initControlPanelFacade(proxyConfig.getMetrics(), proxyConfig.getCluster());
        updateServerInfo();
        ShardingSphereProxy.getInstance().start(port);
    }
    
    private static void log(final ProxyConfiguration proxyConfig) {
        proxyConfig.getSchemaRules().values().forEach(ConfigurationLogger::log);
        ConfigurationLogger.log(proxyConfig.getAuthentication());
        ConfigurationLogger.log(proxyConfig.getProps());
    }
    
    private static void initProxySchemaContexts(final ProxyConfiguration proxyConfig, final boolean orchestrationEnabled) throws SQLException {
        ProxyDataSourceContext dataSourceContext = new ProxyDataSourceContext(proxyConfig.getSchemaDataSources());
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(
                dataSourceContext.getDataSourcesMap(), dataSourceContext.getDatabaseType(), proxyConfig.getSchemaRules(), proxyConfig.getAuthentication(), proxyConfig.getProps());
        ProxySchemaContexts.getInstance().init(createSchemaContextsAware(schemaContextsBuilder, orchestrationEnabled));
    }
    
    private static SchemaContextsAware createSchemaContextsAware(final SchemaContextsBuilder schemaContextsBuilder, final boolean orchestrationEnabled) throws SQLException {
        return orchestrationEnabled ? new ProxyOrchestrationSchemaContexts(schemaContextsBuilder.build()) : schemaContextsBuilder.build();
    }
    
    private static void initControlPanelFacade(final MetricsConfiguration metricsConfiguration, final ClusterConfiguration clusterConfiguration) {
        List<FacadeConfiguration> facadeConfigurations = new LinkedList<>();
        if (null != metricsConfiguration && metricsConfiguration.getEnable()) {
            facadeConfigurations.add(metricsConfiguration);
        }
        if (ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            facadeConfigurations.add(new OpenTracingConfiguration());
        }
        if (ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_CLUSTER_ENABLED)) {
            facadeConfigurations.add(clusterConfiguration);
        }
        new ControlPanelFacadeEngine().init(facadeConfigurations);
    }
    
    private static void updateServerInfo() {
        List<String> schemaNames = ProxySchemaContexts.getInstance().getSchemaNames();
        if (CollectionUtils.isEmpty(schemaNames)) {
            return;
        }
        Map<String, DataSource> dataSources = Objects.requireNonNull(ProxySchemaContexts.getInstance().getSchema(schemaNames.get(0))).getSchema().getDataSources();
        DataSource singleDataSource = dataSources.values().iterator().next();
        try (Connection connection = singleDataSource.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseName = databaseMetaData.getDatabaseProductName();
            String databaseVersion = databaseMetaData.getDatabaseProductVersion();
            log.info("database name {} , database version {}", databaseName, databaseVersion);
            MySQLServerInfo.setServerVersion(databaseVersion);
        } catch (final SQLException ex) {
            throw new ShardingSphereException("Get database server info failed", ex);
        }
    }
}
