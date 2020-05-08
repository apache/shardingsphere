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

package org.apache.shardingsphere.shardingproxy;

import com.google.common.primitives.Ints;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.core.log.ConfigurationLogger;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.root.RuleRootConfigurationsYamlSwapper;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.MetricsTrackerFacade;
import org.apache.shardingsphere.opentracing.ShardingTracer;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.config.ShardingConfiguration;
import org.apache.shardingsphere.shardingproxy.config.ShardingConfigurationLoader;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.shardingproxy.frontend.bootstrap.ShardingProxy;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;

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
 * Sharding-Proxy Bootstrap.
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
        logAndInitContext(authentication, properties);
        initMetrics(metricsConfiguration);
        Map<String, Map<String, YamlDataSourceParameter>> schemaRules = getDataSourceParameterMap(ruleConfigs);
        startProxy(schemaRules.keySet(), port, schemaRules, getRuleConfigurations(ruleConfigs));
        Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources = getDataSourceParameterMap(ruleConfigs);
        startProxy(schemaDataSources.keySet(), port, schemaDataSources, getRuleConfigurations(ruleConfigs));
    }
    
    private static void startWithRegistryCenter(final YamlProxyServerConfiguration serverConfig, final Collection<String> shardingSchemaNames,
                                                final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final int port) throws SQLException {
        try (ShardingOrchestrationFacade shardingOrchestrationFacade = new ShardingOrchestrationFacade(
                new OrchestrationConfigurationYamlSwapper().swap(new YamlOrchestrationConfiguration(serverConfig.getOrchestration())), shardingSchemaNames)) {
            initShardingOrchestrationFacade(serverConfig, ruleConfigs, shardingOrchestrationFacade);
            Authentication authentication = shardingOrchestrationFacade.getConfigCenter().loadAuthentication();
            Properties properties = shardingOrchestrationFacade.getConfigCenter().loadProperties();
            logAndInitContext(authentication, properties);
            initMetrics(serverConfig.getMetrics());
            startProxy(shardingSchemaNames, port, getSchemaDataSourceParameterMap(shardingOrchestrationFacade), getSchemaRules(shardingOrchestrationFacade));
        }
    }
    
    private static void logAndInitContext(final Authentication authentication, final Properties properties) {
        ConfigurationLogger.log(authentication);
        ConfigurationLogger.log(properties);
        ShardingProxyContext.getInstance().init(authentication, properties);
    }

    private static void startProxy(final Collection<String> shardingSchemaNames, final int port, final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources,
                                   final Map<String, Collection<RuleConfiguration>> schemaRules) throws SQLException {
        LogicSchemas.getInstance().init(shardingSchemaNames, schemaDataSources, schemaRules);
        initOpenTracing();
        ShardingProxy.getInstance().start(port);
    }
    
    private static Map<String, Map<String, YamlDataSourceParameter>> getSchemaDataSourceParameterMap(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        Map<String, Map<String, YamlDataSourceParameter>> result = new LinkedHashMap<>();
        for (String each : shardingOrchestrationFacade.getConfigCenter().getAllShardingSchemaNames()) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(shardingOrchestrationFacade.getConfigCenter().loadDataSourceConfigurations(each)));
        }
        return result;
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
        if (ShardingProxyContext.getInstance().getProperties().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            ShardingTracer.init();
        }
    }
    
    private static void initMetrics(final YamlMetricsConfiguration metricsConfiguration) {
        if (ShardingProxyContext.getInstance().getProperties().<Boolean>getValue(ConfigurationPropertyKey.PROXY_METRICS_ENABLED)) {
            MetricsTrackerFacade.getInstance().init(new MetricsConfigurationYamlSwapper().swap(metricsConfiguration));
        }
    }
    
    private static Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, YamlProxyRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(entry.getValue().getDataSources()));
        }
        return result;
    }
    
    private static Map<String, Map<String, YamlDataSourceParameter>> getDataSourceParameterMap(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, Map<String, YamlDataSourceParameter>> result = new HashMap<>(localRuleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSources());
        }
        return result;
    }
    
    private static Map<String, Collection<RuleConfiguration>> getRuleConfigurations(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, Collection<RuleConfiguration>> result = new HashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            if (null != entry.getValue().getShardingRule()) {
                YamlRootRuleConfigurations configurations = new YamlRootRuleConfigurations();
                configurations.setShardingRule(entry.getValue().getShardingRule());
                configurations.getMasterSlaveRules().putAll(entry.getValue().getMasterSlaveRules());
                configurations.setEncryptRule(entry.getValue().getEncryptRule());
                result.put(entry.getKey(), new RuleRootConfigurationsYamlSwapper().swap(configurations));
            } else if (!entry.getValue().getMasterSlaveRules().isEmpty()) {
                for (Entry<String, YamlMasterSlaveRuleConfiguration> configEntry : entry.getValue().getMasterSlaveRules().entrySet()) {
                    result.put(entry.getKey(), Collections.singleton(new MasterSlaveRuleConfigurationYamlSwapper().swap(configEntry.getValue())));    
                }
            } else if (null != entry.getValue().getEncryptRule()) {
                result.put(entry.getKey(), Collections.singleton(new EncryptRuleConfigurationYamlSwapper().swap(entry.getValue().getEncryptRule())));
            } else if (null != entry.getValue().getShadowRule()) {
                result.put(entry.getKey(), Collections.singleton(new ShadowRuleConfigurationYamlSwapper().swap(entry.getValue().getShadowRule())));
            }
        }
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
