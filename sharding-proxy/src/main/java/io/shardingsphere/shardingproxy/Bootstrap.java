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

package io.shardingsphere.shardingproxy;

import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.shardingproxy.config.ShardingConfiguration;
import io.shardingsphere.shardingproxy.config.ShardingConfigurationLoader;
import io.shardingsphere.shardingproxy.config.yaml.ProxyYamlRuleConfiguration;
import io.shardingsphere.shardingproxy.config.yaml.ProxyYamlServerConfiguration;
import io.shardingsphere.shardingproxy.frontend.ShardingProxy;
import io.shardingsphere.shardingproxy.listener.ProxyListenerRegister;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.util.DataSourceConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Sharding-Proxy Bootstrap.
 *
 * @author zhangliang
 * @author wangkai
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bootstrap {
    
    private static final int DEFAULT_PORT = 3307;
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws InterruptedException interrupted exception
     * @throws IOException IO exception
     */
    public static void main(final String[] args) throws InterruptedException, IOException {
        ShardingConfiguration shardingConfig = new ShardingConfigurationLoader().load();
        int port = getPort(args);
        new ProxyListenerRegister().register();
        if (null == shardingConfig.getServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(
                    shardingConfig.getRuleConfigurationMap(), shardingConfig.getServerConfiguration().getAuthentication(), shardingConfig.getServerConfiguration().getProps(), port);
        } else {
            startWithRegistryCenter(shardingConfig.getServerConfiguration(), shardingConfig.getRuleConfigurationMap().keySet(), shardingConfig.getRuleConfigurationMap(), port);
        }
    }
    
    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (final NumberFormatException ex) {
            return DEFAULT_PORT;
        }
    }
    
    private static void startWithoutRegistryCenter(
            final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final Authentication authentication, final Properties prop, final int port) throws InterruptedException {
        GlobalRegistry.getInstance().init(Bootstrap.getDataSourceParameterMap(ruleConfigs), getRuleConfiguration(ruleConfigs), authentication, prop);
        initOpenTracing();
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(final ProxyYamlServerConfiguration serverConfig, 
                                                final Collection<String> shardingSchemaNames, final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(serverConfig.getOrchestration().getOrchestrationConfiguration(), shardingSchemaNames)) {
            Map<String, Map<String, DataSourceParameter>> schemaDataSourceParameterMap = new LinkedHashMap<>();
            initOrchestrationFacade(serverConfig, ruleConfigs, orchestrationFacade);
            Map<String, YamlRuleConfiguration> schemaRules = new LinkedHashMap<>();
            for (String each : orchestrationFacade.getConfigService().getAllShardingSchemaNames()) {
                schemaDataSourceParameterMap.put(each, DataSourceConverter.getDataSourceParameterMap(orchestrationFacade.getConfigService().loadDataSourceConfigurations(each)));
                YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfiguration();
                if (orchestrationFacade.getConfigService().isShardingRule(each)) {
                    yamlRuleConfig.setShardingRule(orchestrationFacade.getConfigService().loadShardingRuleConfiguration(each));
                } else {
                    yamlRuleConfig.setMasterSlaveRule(orchestrationFacade.getConfigService().loadMasterSlaveRuleConfiguration(each));
                }
                schemaRules.put(each, yamlRuleConfig);
            }
            GlobalRegistry.getInstance().init(
                    schemaDataSourceParameterMap, schemaRules, orchestrationFacade.getConfigService().loadAuthentication(), orchestrationFacade.getConfigService().loadProperties(), true);
            initOpenTracing();
            new ShardingProxy().start(port);
        }
    }
    
    private static void initOrchestrationFacade(final ProxyYamlServerConfiguration serverConfig, 
                                                final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final OrchestrationFacade orchestrationFacade) {
        if (ruleConfigs.isEmpty()) {
            orchestrationFacade.getListenerManager().initProxyListeners();
        } else {
            orchestrationFacade.init(getDataSourceConfigurationMap(ruleConfigs), getRuleConfiguration(ruleConfigs), serverConfig.getAuthentication(), serverConfig.getProps());
        }
    }
    
    private static void initOpenTracing() {
        if (GlobalRegistry.getInstance().isOpenTracingEnable()) {
            ShardingTracer.init();
        }
    }
    
    private static Map<String, Map<String, DataSourceConfiguration>> getDataSourceConfigurationMap(final Map<String, ProxyYamlRuleConfiguration> ruleConfigs) {
        Map<String, Map<String, DataSourceConfiguration>> result = new LinkedHashMap<>();
        for (Entry<String, ProxyYamlRuleConfiguration> entry : ruleConfigs.entrySet()) {
            result.put(entry.getKey(), DataSourceConverter.getDataSourceConfigurationMap(entry.getValue().getDataSources()));
        }
        return result;
    }

    private static Map<String, Map<String, DataSourceParameter>> getDataSourceParameterMap(final Map<String, ProxyYamlRuleConfiguration> localRuleConfigs) {
        Map<String, Map<String, DataSourceParameter>> result = new HashMap<>(localRuleConfigs.size(), 1);
        for (Entry<String, ProxyYamlRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSources());
        }
        return result;
    }
    
    private static Map<String, YamlRuleConfiguration> getRuleConfiguration(final Map<String, ProxyYamlRuleConfiguration> localRuleConfigs) {
        Map<String, YamlRuleConfiguration> result = new HashMap<>();
        for (Entry<String, ProxyYamlRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfiguration();
            yamlRuleConfig.setShardingRule(null == entry.getValue().getShardingRule() ? null : entry.getValue().getShardingRule().getShardingRuleConfiguration());
            yamlRuleConfig.setMasterSlaveRule(null == entry.getValue().getMasterSlaveRule() ? null : entry.getValue().getMasterSlaveRule().getMasterSlaveRuleConfiguration());
            result.put(entry.getKey(), yamlRuleConfig);
        }
        return result;
    }
}
