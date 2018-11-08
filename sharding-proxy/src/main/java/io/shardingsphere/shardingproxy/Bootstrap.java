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

import io.shardingsphere.api.config.RuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.shardingproxy.config.ShardingConfiguration;
import io.shardingsphere.shardingproxy.config.ShardingConfigurationLoader;
import io.shardingsphere.shardingproxy.config.yaml.YamlProxyRuleConfiguration;
import io.shardingsphere.shardingproxy.config.yaml.YamlProxyServerConfiguration;
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
            startWithoutRegistryCenter(shardingConfig.getRuleConfigurationMap(), shardingConfig.getServerConfiguration().getAuthentication(),
                    shardingConfig.getServerConfiguration().getConfigMap(), shardingConfig.getServerConfiguration().getProps(), port);
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
    
    private static void startWithoutRegistryCenter(final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final Authentication authentication,
                                                   final Map<String, Object> configMap, final Properties prop, final int port) throws InterruptedException {
        GlobalRegistry.getInstance().init(getDataSourceParameterMap(ruleConfigs), getRuleConfiguration(ruleConfigs), authentication, configMap, prop);
        initOpenTracing();
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(final YamlProxyServerConfiguration serverConfig,
                                                final Collection<String> shardingSchemaNames, final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(serverConfig.getOrchestration().getOrchestrationConfiguration(), shardingSchemaNames)) {
            initOrchestrationFacade(serverConfig, ruleConfigs, orchestrationFacade);
            GlobalRegistry.getInstance().init(getSchemaDataSourceParameterMap(orchestrationFacade), getSchemaRules(orchestrationFacade),
                    orchestrationFacade.getConfigService().loadAuthentication(), orchestrationFacade.getConfigService().loadConfigMap(), orchestrationFacade.getConfigService().loadProperties(), true);
            initOpenTracing();
            new ShardingProxy().start(port);
        }
    }
    
    private static Map<String, Map<String, DataSourceParameter>> getSchemaDataSourceParameterMap(final OrchestrationFacade orchestrationFacade) {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>();
        for (String each : orchestrationFacade.getConfigService().getAllShardingSchemaNames()) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(orchestrationFacade.getConfigService().loadDataSourceConfigurations(each)));
        }
        return result;
    }
    
    private static Map<String, RuleConfiguration> getSchemaRules(final OrchestrationFacade orchestrationFacade) {
        Map<String, RuleConfiguration> result = new LinkedHashMap<>();
        for (String each : orchestrationFacade.getConfigService().getAllShardingSchemaNames()) {
            if (orchestrationFacade.getConfigService().isShardingRule(each)) {
                result.put(each, orchestrationFacade.getConfigService().loadShardingRuleConfiguration(each));
            } else {
                result.put(each, orchestrationFacade.getConfigService().loadMasterSlaveRuleConfiguration(each));
            }
        }
        return result;
    }
    
    private static void initOrchestrationFacade(final YamlProxyServerConfiguration serverConfig,
                                                final Map<String, YamlProxyRuleConfiguration> ruleConfigs, final OrchestrationFacade orchestrationFacade) {
        if (ruleConfigs.isEmpty()) {
            orchestrationFacade.init();
        } else {
            orchestrationFacade.init(getDataSourceConfigurationMap(ruleConfigs),
                    getRuleConfiguration(ruleConfigs), serverConfig.getAuthentication(), serverConfig.getConfigMap(), serverConfig.getProps());
        }
    }
    
    private static void initOpenTracing() {
        if (GlobalRegistry.getInstance().getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_OPENTRACING_ENABLED)) {
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

    private static Map<String, Map<String, DataSourceParameter>> getDataSourceParameterMap(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, Map<String, DataSourceParameter>> result = new HashMap<>(localRuleConfigs.size(), 1);
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDataSources());
        }
        return result;
    }
    
    private static Map<String, RuleConfiguration> getRuleConfiguration(final Map<String, YamlProxyRuleConfiguration> localRuleConfigs) {
        Map<String, RuleConfiguration> result = new HashMap<>();
        for (Entry<String, YamlProxyRuleConfiguration> entry : localRuleConfigs.entrySet()) {
            result.put(entry.getKey(), null != entry.getValue().getShardingRule() ? entry.getValue().getShardingRule().getShardingRuleConfiguration()
                    : entry.getValue().getMasterSlaveRule().getMasterSlaveRuleConfiguration());
        }
        return result;
    }
}
