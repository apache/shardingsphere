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
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
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
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.route.decorator.RouteDecorator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
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
        registerDecorator();
        int port = getPort(args);
        ShardingConfiguration shardingConfig = new ShardingConfigurationLoader().load(getConfigPath(args));
        logRuleConfigurationMap(getRuleConfiguration(shardingConfig.getRuleConfigurationMap()).values());
        if (null == shardingConfig.getServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(shardingConfig.getRuleConfigurationMap(), shardingConfig.getServerConfiguration().getAuthentication(), shardingConfig.getServerConfiguration().getProps(), port);
        } else {
            startWithRegistryCenter(shardingConfig.getServerConfiguration(), shardingConfig.getRuleConfigurationMap().keySet(), shardingConfig.getRuleConfigurationMap(), port);
        }
    }
    
    private static void registerDecorator() {
        ShardingSphereServiceLoader.register(RouteDecorator.class);
        ShardingSphereServiceLoader.register(SQLRewriteContextDecorator.class);
        ShardingSphereServiceLoader.register(ResultProcessEngine.class);
    }
    
    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return DEFAULT_PORT;
        }
        Integer paredPort = Ints.tryParse(args[0]);
        return paredPort == null ? DEFAULT_PORT : paredPort;
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
                                                   final YamlAuthenticationConfiguration yamlAuthenticationConfig, final Properties properties, final int port) throws SQLException {
        Authentication authentication = new AuthenticationYamlSwapper().swap(yamlAuthenticationConfig);
        logAndInitContext(authentication, properties);
        Map<String, Map<String, YamlDataSourceParameter>> schemaRules = getDataSourceParameterMap(ruleConfigs);
        startProxy(schemaRules.keySet(), port, schemaRules,
                getRuleConfiguration(ruleConfigs), false);
    }
    
    private static void startWithRegistryCenter(final YamlProxyServerConfiguration serverConfig, final Collection<String> shardingSchemaNames,
                                                final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final int port) throws SQLException {
        try (ShardingOrchestrationFacade shardingOrchestrationFacade = new ShardingOrchestrationFacade(
                new OrchestrationConfigurationYamlSwapper().swap(new YamlOrchestrationConfiguration(serverConfig.getOrchestration())), shardingSchemaNames)) {
            initShardingOrchestrationFacade(serverConfig, ruleConfigs, shardingOrchestrationFacade);
            Authentication authentication = shardingOrchestrationFacade.getConfigCenter().loadAuthentication();
            Properties properties = shardingOrchestrationFacade.getConfigCenter().loadProperties();
            logAndInitContext(authentication, properties);
            startProxy(shardingSchemaNames, port, getSchemaDataSourceParameterMap(shardingOrchestrationFacade),
                    getSchemaRules(shardingOrchestrationFacade), true);
        }
    }
    
    private static void logAndInitContext(final Authentication authentication, final Properties properties) {
        ConfigurationLogger.log(authentication);
        ConfigurationLogger.log(properties);
        ShardingProxyContext.getInstance().init(authentication, properties);
    }

    private static void startProxy(final Collection<String> shardingSchemaNames, final int port,
                                   final Map<String, Map<String, YamlDataSourceParameter>> schemaDataSources,
                                   final Map<String, RuleConfiguration> schemaRules, final boolean isUsingRegistry) throws SQLException {
        LogicSchemas.getInstance().init(shardingSchemaNames, schemaDataSources, schemaRules, isUsingRegistry);
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
    
    private static Map<String, RuleConfiguration> getSchemaRules(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        Map<String, RuleConfiguration> result = new LinkedHashMap<>();
        for (String each : shardingOrchestrationFacade.getConfigCenter().getAllShardingSchemaNames()) {
            if (shardingOrchestrationFacade.getConfigCenter().isEncryptRule(each)) {
                result.put(each, shardingOrchestrationFacade.getConfigCenter().loadEncryptRuleConfiguration(each));
            } else if (shardingOrchestrationFacade.getConfigCenter().isShardingRule(each)) {
                result.put(each, shardingOrchestrationFacade.getConfigCenter().loadShardingRuleConfiguration(each));
            } else if (shardingOrchestrationFacade.getConfigCenter().isShadowRule(each)) {
                result.put(each, shardingOrchestrationFacade.getConfigCenter().loadShadowRuleConfiguration(each));
            } else {
                result.put(each, shardingOrchestrationFacade.getConfigCenter().loadMasterSlaveRuleConfiguration(each));
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
                    getRuleConfiguration(ruleConfigs), new AuthenticationYamlSwapper().swap(serverConfig.getAuthentication()), serverConfig.getProps());
        }
    }
    
    private static void initOpenTracing() {
        if (ShardingProxyContext.getInstance().getProperties().<Boolean>getValue(ConfigurationPropertyKey.PROXY_OPENTRACING_ENABLED)) {
            ShardingTracer.init();
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
    
    private static Map<String, RuleConfiguration> getRuleConfiguration(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, RuleConfiguration> result = new HashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            if (null != entry.getValue().getShardingRule()) {
                result.put(entry.getKey(), new ShardingRuleConfigurationYamlSwapper().swap(entry.getValue().getShardingRule()));
            } else if (null != entry.getValue().getMasterSlaveRule()) {
                result.put(entry.getKey(), new MasterSlaveRuleConfigurationYamlSwapper().swap(entry.getValue().getMasterSlaveRule()));
            } else if (null != entry.getValue().getEncryptRule()) {
                result.put(entry.getKey(), new EncryptRuleConfigurationYamlSwapper().swap(entry.getValue().getEncryptRule()));
            } else if (null != entry.getValue().getShadowRule()) {
                result.put(entry.getKey(), new ShadowRuleConfigurationYamlSwapper().swap(entry.getValue().getShadowRule()));
            }
        }
        return result;
    }
    
    /**
     * Log rule configurations.
     *
     * @param ruleConfigurations log rule configurations
     */
    private static void logRuleConfigurationMap(final Collection<RuleConfiguration> ruleConfigurations) {
        if (CollectionUtils.isNotEmpty(ruleConfigurations)) {
            for (RuleConfiguration each : ruleConfigurations) {
                ConfigurationLogger.log(each);
            }
        }
    }
}
