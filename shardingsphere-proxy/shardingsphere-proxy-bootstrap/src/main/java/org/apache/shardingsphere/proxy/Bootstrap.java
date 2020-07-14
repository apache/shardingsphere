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

import com.google.common.primitives.Ints;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.control.panel.spi.FacadeConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.apache.shardingsphere.control.panel.spi.opentracing.OpenTracingConfiguration;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.constant.Constants;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.converter.ProxyConfigurationConverter;
import org.apache.shardingsphere.proxy.config.converter.ProxyConfigurationConverterFactory;
import org.apache.shardingsphere.proxy.config.ShardingConfiguration;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.frontend.bootstrap.ShardingSphereProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class Bootstrap {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/";
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws Exception exception
     */
    public static void main(final String[] args) throws Exception {
        int port = getPort(args);
        System.setProperty(Constants.PORT_KEY, String.valueOf(port));
        ShardingConfiguration shardingConfig = new ShardingConfigurationLoader().load(getConfigPath(args));
        logRuleConfigurationMap(getRuleConfigurations(shardingConfig.getRuleConfigurationMap()).values());
        boolean isOrchestration = null != shardingConfig.getServerConfiguration().getOrchestration();
        try (ProxyConfigurationConverter converter = ProxyConfigurationConverterFactory.newInstances(isOrchestration)) {
            ProxyConfiguration proxyConfiguration = converter.convert(shardingConfig);
            initialize(proxyConfiguration, port, converter);
        }
    }
    
    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return Constants.DEFAULT_PORT;
        }
        Integer port = Ints.tryParse(args[0]);
        return port == null ? Constants.DEFAULT_PORT : port;
    }
    
    private static String getConfigPath(final String[] args) {
        if (args.length < 2) {
            return DEFAULT_CONFIG_PATH;
        }
        return paddingWithSlash(args[1]);
    }
    
    private static String paddingWithSlash(final String arg) {
        String path = arg.endsWith("/") ? arg : (arg + "/");
        return path.startsWith("/") ? path : ("/" + path);
    }
    
    private static void initialize(final ProxyConfiguration proxyConfiguration, final int port, final ProxyConfigurationConverter converter) throws SQLException {
        Authentication authentication = proxyConfiguration.getAuthentication();
        Properties props = proxyConfiguration.getProps();
        log(authentication, props);
        initProxySchemaContexts(proxyConfiguration.getSchemaDataSources(), proxyConfiguration.getSchemaRules(), authentication, props, converter);
        initControlPanelFacade(proxyConfiguration.getMetrics(), proxyConfiguration.getCluster());
        updateServerInfo();
        ShardingSphereProxy.getInstance().start(port);
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
    
    private static void initProxySchemaContexts(final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final Map<String, Collection<RuleConfiguration>> schemaRules,
                                                final Authentication authentication, final Properties properties, final ProxyConfigurationConverter converter) throws SQLException {
        // TODO Consider loading from configuration.
        DatabaseType databaseType = schemaDataSources.isEmpty() ? new MySQLDatabaseType() 
                : DatabaseTypes.getActualDatabaseType(
                JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(schemaDataSources.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType());
        SchemaContextsBuilder schemaContextsBuilder =
                new SchemaContextsBuilder(createDataSourcesMap(schemaDataSources), schemaDataSources, authentication, databaseType, schemaRules, properties);
        ProxySchemaContexts.getInstance().init(converter.contextsAware(schemaContextsBuilder));
    }
    
    private static Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        return schemaDataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> createDataSources(entry.getValue()), (oldVal, currVal) -> oldVal, LinkedHashMap::new));
    }
    
    private static Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            try {
                result.put(entry.getKey(), JDBCRawBackendDataSourceFactory.getInstance().build(entry.getKey(), entry.getValue()));
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new ShardingSphereException(String.format("Can not build data source, name is `%s`.", entry.getKey()), ex);
            }
        }
        return result;
    }
    
    private static void log(final Authentication authentication, final Properties properties) {
        ConfigurationLogger.log(authentication);
        ConfigurationLogger.log(properties);
    }
    
    private static Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        return localRuleConfigs.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> swapperEngine.swapToRuleConfigurations(entry.getValue().getRules())));
    }
    
    /**
     * Log rule configurations.
     *
     * @param ruleConfigurations log rule configurations
     */
    private static void logRuleConfigurationMap(final Collection<Collection<RuleConfiguration>> ruleConfigurations) {
        if (CollectionUtils.isNotEmpty(ruleConfigurations)) {
            ruleConfigurations.forEach(ConfigurationLogger::log);
        }
    }
}
