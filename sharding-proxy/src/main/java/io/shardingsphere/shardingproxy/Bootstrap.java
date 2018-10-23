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

import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.orchestration.config.OrchestrationType;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.shardingproxy.config.ShardingConfiguration;
import io.shardingsphere.shardingproxy.config.ShardingConfigurationLoader;
import io.shardingsphere.shardingproxy.config.yaml.ProxyYamlRuleConfiguration;
import io.shardingsphere.shardingproxy.config.yaml.ProxyYamlServerConfiguration;
import io.shardingsphere.shardingproxy.frontend.ShardingProxy;
import io.shardingsphere.shardingproxy.listener.ProxyListenerRegister;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
     * Main Entrance.
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
            startWithoutRegistryCenter(shardingConfig.getServerConfiguration(), shardingConfig.getRuleConfigurationMap(), port);
        } else {
            startWithRegistryCenter(shardingConfig.getServerConfiguration(), shardingConfig.getRuleConfigurationMap(), port);
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
            final ProxyYamlServerConfiguration serverConfig, final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        GlobalRegistry.getInstance().init(getYamlServerConfiguration(serverConfig), getSchemaDataSourceMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
        initOpenTracing();
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(
            final ProxyYamlServerConfiguration serverConfig, final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(serverConfig.getOrchestration().getOrchestrationConfiguration())) {
            initOrchestrationFacade(serverConfig, ruleConfigs, orchestrationFacade);
            GlobalRegistry.getInstance().init(orchestrationFacade.getConfigService().loadYamlServerConfiguration(), 
                    orchestrationFacade.getConfigService().loadProxyDataSources(), orchestrationFacade.getConfigService().loadProxyConfiguration(), true);
            initOpenTracing();
            new ShardingProxy().start(port);
        }
    }
    
    private static void initOrchestrationFacade(final ProxyYamlServerConfiguration serverConfig, final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final OrchestrationFacade orchestrationFacade) {
        if (ruleConfigs.isEmpty()) {
            orchestrationFacade.init(OrchestrationType.PROXY);
        } else {
            orchestrationFacade.init(getYamlServerConfiguration(serverConfig), getSchemaDataSourceMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
        }
    }
    
    private static void initOpenTracing() {
        if (GlobalRegistry.getInstance().isOpenTracingEnable()) {
            ShardingTracer.init();
        }
    }
    
    private static YamlServerConfiguration getYamlServerConfiguration(final ProxyYamlServerConfiguration serverConfig) {
        YamlServerConfiguration result = new YamlServerConfiguration();
        result.setAuthentication(serverConfig.getAuthentication());
        result.setProps(serverConfig.getProps());
        return result;
    }
    
    private static Map<String, Map<String, DataSourceParameter>> getSchemaDataSourceMap(final Map<String, ProxyYamlRuleConfiguration> localRuleConfigs) {
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
            yamlRuleConfig.setShardingRule(entry.getValue().getShardingRule());
            yamlRuleConfig.setMasterSlaveRule(entry.getValue().getMasterSlaveRule());
            result.put(entry.getKey(), yamlRuleConfig);
        }
        return result;
    }
}
