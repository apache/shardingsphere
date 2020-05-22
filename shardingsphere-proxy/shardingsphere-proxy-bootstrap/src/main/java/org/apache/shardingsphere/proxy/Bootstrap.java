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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.MetricsTrackerFacade;
import org.apache.shardingsphere.opentracing.ShardingTracer;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.backend.schema.ShardingSphereSchemas;
import org.apache.shardingsphere.proxy.backend.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.ShardingConfiguration;
import org.apache.shardingsphere.proxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.frontend.bootstrap.ShardingSphereProxy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bootstrap {
    
    private static final int DEFAULT_PORT = 3307;
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/";
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void main(final String[] args) throws IOException, SQLException {
        int port = getPort(args);
        ShardingConfiguration shardingConfig = new ShardingConfigurationLoader().load(getConfigPath(args));
        logRuleConfigurationMap(getRuleConfigurations(shardingConfig.getRuleConfigurationMap()).values());
        if (null == shardingConfig.getServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(shardingConfig.getRuleConfigurationMap(), shardingConfig.getServerConfiguration().getAuthentication(),
                    shardingConfig.getServerConfiguration().getMetrics(), shardingConfig.getServerConfiguration().getProps(), port);
        } else {
            startWithRegistryCenter(shardingConfig.getServerConfiguration(), shardingConfig.getRuleConfigurationMap().keySet(), shardingConfig.getRuleConfigurationMap(), port);
        }
    }
    
    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return DEFAULT_PORT;
        }
        Integer port = Ints.tryParse(args[0]);
        return port == null ? DEFAULT_PORT : port;
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
    
    private static void startWithoutRegistryCenter(final Map<String, YamlProxyRuleConfiguration> ruleConfigs,
                                                   final YamlAuthenticationConfiguration yamlAuthenticationConfig,
                                                   final YamlMetricsConfiguration metricsConfiguration, final Properties properties, final int port) throws SQLException {
        Authentication authentication = new AuthenticationYamlSwapper().swap(yamlAuthenticationConfig);
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = getDataSourceParametersMap(ruleConfigs);
        Map<String, Collection<RuleConfiguration>> schemaRules = getRuleConfigurations(ruleConfigs);
        ProxySchemaContexts.getInstance().init(schemaDataSources, schemaRules, authentication, properties);
        log(authentication, properties);
        initMetrics(metricsConfiguration);
        startProxy(schemaDataSources.keySet(), port, schemaDataSources, schemaRules);
    }
    
    private static void startWithRegistryCenter(final YamlProxyServerConfiguration serverConfig, final Collection<String> shardingSchemaNames,
                                                final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final int port) throws SQLException {
        try (ShardingOrchestrationFacade shardingOrchestrationFacade = new ShardingOrchestrationFacade(
                new OrchestrationConfigurationYamlSwapper().swap(new YamlOrchestrationConfiguration(serverConfig.getOrchestration())), shardingSchemaNames)) {
            initShardingOrchestrationFacade(serverConfig, ruleConfigs, shardingOrchestrationFacade);
            Authentication authentication = shardingOrchestrationFacade.getConfigCenter().loadAuthentication();
            Properties properties = shardingOrchestrationFacade.getConfigCenter().loadProperties();
            Map<String, Map<String, DataSourceParameter>> schemaDataSources = getDataSourceParametersMap(shardingOrchestrationFacade);
            Map<String, Collection<RuleConfiguration>> schemaRules = getSchemaRules(shardingOrchestrationFacade);
            ProxySchemaContexts.getInstance().init(schemaDataSources, schemaRules, authentication, properties);
            log(authentication, properties);
            initMetrics(serverConfig.getMetrics());
            startProxy(shardingSchemaNames, port, schemaDataSources, schemaRules);
        }
    }
    
    private static void log(final Authentication authentication, final Properties properties) {
        ConfigurationLogger.log(authentication);
        ConfigurationLogger.log(properties);
    }

    private static void startProxy(final Collection<String> shardingSchemaNames, final int port, final Map<String, Map<String, DataSourceParameter>> schemaDataSources,
                                   final Map<String, Collection<RuleConfiguration>> schemaRules) throws SQLException {
        ShardingSphereSchemas.getInstance().init(shardingSchemaNames, schemaDataSources, schemaRules);
        initOpenTracing();
        ShardingSphereProxy.getInstance().start(port);
    }
    
    private static Map<String, Collection<RuleConfiguration>> getSchemaRules(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        Map<String, Collection<RuleConfiguration>> result = new LinkedHashMap<>();
        for (String each : shardingOrchestrationFacade.getConfigCenter().getAllShardingSchemaNames()) {
            if (shardingOrchestrationFacade.getConfigCenter().isShardingRule(each)) {
                result.put(each, shardingOrchestrationFacade.getConfigCenter().loadRuleConfigurations(each));
            } else if (shardingOrchestrationFacade.getConfigCenter().isEncryptRule(each)) {
                result.put(each, Collections.singletonList(shardingOrchestrationFacade.getConfigCenter().loadEncryptRuleConfiguration(each)));
            } else if (shardingOrchestrationFacade.getConfigCenter().isShadowRule(each)) {
                result.put(each, Collections.singletonList(shardingOrchestrationFacade.getConfigCenter().loadShadowRuleConfiguration(each)));
            } else {
                result.put(each, Collections.singletonList(shardingOrchestrationFacade.getConfigCenter().loadMasterSlaveRuleConfiguration(each)));
            }
        }
        return result;
    }
    
    private static void initShardingOrchestrationFacade(
            final YamlProxyServerConfiguration serverConfig, final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        if (ruleConfigs.isEmpty()) {
            shardingOrchestrationFacade.init();
        } else {
            shardingOrchestrationFacade.init(getDataSourceConfigurationMap(ruleConfigs),
                    getRuleConfigurations(ruleConfigs), new AuthenticationYamlSwapper().swap(serverConfig.getAuthentication()), serverConfig.getProps());
        }
    }
    
    private static void initOpenTracing() {
        if (ProxySchemaContexts.getInstance().getSchemaContexts().getProperties().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            ShardingTracer.init();
        }
    }
    
    private static void initMetrics(final YamlMetricsConfiguration metricsConfiguration) {
        if (ProxySchemaContexts.getInstance().getSchemaContexts().getProperties().<Boolean>getValue(ConfigurationPropertyKey.PROXY_METRICS_ENABLED)) {
            MetricsTrackerFacade.getInstance().init(new MetricsConfigurationYamlSwapper().swap(metricsConfiguration));
        }
    }
    
    private static Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(getDataSourceParameters(entry.getValue().getDataSources())));
        }
        return result;
    }
    
    private static Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, Collection<RuleConfiguration>> result = new HashMap<>();
        YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            YamlRootRuleConfigurations configurations = new YamlRootRuleConfigurations();
            for (YamlRuleConfiguration each : entry.getValue().getRules()) {
                configurations.getRules().add(each);
            }
            result.put(entry.getKey(), swapperEngine.swapToRuleConfigurations(configurations.getRules()));
        }
        return result;
    }
    
    private static Map<String, Map<String, DataSourceParameter>> getDataSourceParametersMap(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>();
        for (String each : shardingOrchestrationFacade.getConfigCenter().getAllShardingSchemaNames()) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(shardingOrchestrationFacade.getConfigCenter().loadDataSourceConfigurations(each)));
        }
        return result;
    }
    
    private static Map<String, Map<String, DataSourceParameter>> getDataSourceParametersMap(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, Map<String, DataSourceParameter>> result = new HashMap<>(localRuleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            result.put(entry.getKey(), getDataSourceParameters(entry.getValue().getDataSources()));
        }
        return result;
    }
    
    private static Map<String, DataSourceParameter> getDataSourceParameters(final Map<String, YamlDataSourceParameter> dataSourceParameters) {
        Map<String, DataSourceParameter> result = new LinkedHashMap<>();
        for (Entry<String, YamlDataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), createDataSourceParameter(entry.getValue()));
        }
        return result;
    }
    
    private static DataSourceParameter createDataSourceParameter(final YamlDataSourceParameter yamlDataSourceParameter) {
        DataSourceParameter result = new DataSourceParameter();
        result.setConnectionTimeoutMilliseconds(yamlDataSourceParameter.getConnectionTimeoutMilliseconds());
        result.setIdleTimeoutMilliseconds(yamlDataSourceParameter.getIdleTimeoutMilliseconds());
        result.setMaintenanceIntervalMilliseconds(yamlDataSourceParameter.getMaintenanceIntervalMilliseconds());
        result.setMaxLifetimeMilliseconds(yamlDataSourceParameter.getMaxLifetimeMilliseconds());
        result.setMaxPoolSize(yamlDataSourceParameter.getMaxPoolSize());
        result.setMinPoolSize(yamlDataSourceParameter.getMinPoolSize());
        result.setUsername(yamlDataSourceParameter.getUsername());
        result.setPassword(yamlDataSourceParameter.getPassword());
        result.setReadOnly(yamlDataSourceParameter.isReadOnly());
        result.setUrl(yamlDataSourceParameter.getUrl());
        return result;
    }
    
    /**
     * Log rule configurations.
     *
     * @param ruleConfigurations log rule configurations
     */
    private static void logRuleConfigurationMap(final Collection<Collection<RuleConfiguration>> ruleConfigurations) {
        if (CollectionUtils.isNotEmpty(ruleConfigurations)) {
            for (Collection<RuleConfiguration> each : ruleConfigurations) {
                ConfigurationLogger.log(each);
            }
        }
    }
}
