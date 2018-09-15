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

package io.shardingsphere.proxy;

import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.ServerConfiguration;
import io.shardingsphere.proxy.config.RuleConfiguration;
import io.shardingsphere.proxy.config.ConfigurationLoader;
import io.shardingsphere.proxy.frontend.ShardingProxy;
import io.shardingsphere.proxy.listener.ProxyListenerRegister;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        ConfigurationLoader configLoader = new ConfigurationLoader();
        configLoader.load();
        int port = getPort(args);
        new ProxyListenerRegister().register();
        if (null == configLoader.getServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(configLoader.getServerConfiguration(), configLoader.getRuleConfigurations(), port);
        } else {
            startWithRegistryCenter(configLoader.getServerConfiguration(), configLoader.getRuleConfigurations(), port);
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
    
    private static void startWithoutRegistryCenter(final ServerConfiguration serverConfig, final Collection<RuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        ProxyContext.getInstance().init(getYamlServerConfiguration(serverConfig), getSchemaDataSourceMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(final ServerConfiguration serverConfig, final Collection<RuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(serverConfig.getOrchestration().getOrchestrationConfiguration())) {
            if (!ruleConfigs.isEmpty()) {
                orchestrationFacade.init(getYamlServerConfiguration(serverConfig), getSchemaDataSourceMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
            }
            ProxyContext.getInstance().init(orchestrationFacade.getConfigService().loadYamlServerConfiguration(), 
                    orchestrationFacade.getConfigService().loadProxyDataSources(), orchestrationFacade.getConfigService().loadProxyConfiguration());
            new ShardingProxy().start(port);
        }
    }
    
    private static YamlServerConfiguration getYamlServerConfiguration(final ServerConfiguration serverConfig) {
        YamlServerConfiguration yamlServerConfig = new YamlServerConfiguration();
        yamlServerConfig.setProxyAuthority(serverConfig.getProxyAuthority());
        yamlServerConfig.setProps(serverConfig.getProps());
        return yamlServerConfig;
    }
    
    private static Map<String, Map<String, DataSourceParameter>> getSchemaDataSourceMap(final Collection<RuleConfiguration> localRuleConfigs) {
        Map<String, Map<String, DataSourceParameter>> result = new HashMap<>(localRuleConfigs.size(), 1);
        for (RuleConfiguration each : localRuleConfigs) {
            result.put(each.getSchemaName(), each.getDataSources());
        }
        return result;
    }
    
    private static Map<String, YamlRuleConfiguration> getRuleConfiguration(final Collection<RuleConfiguration> localRuleConfigs) {
        Map<String, YamlRuleConfiguration> result = new HashMap<>();
        for (RuleConfiguration each : localRuleConfigs) {
            YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfiguration();
            yamlRuleConfig.setShardingRule(each.getShardingRule());
            yamlRuleConfig.setMasterSlaveRule(each.getMasterSlaveRule());
            result.put(each.getSchemaName(), yamlRuleConfig);
        }
        return result;
    }
}
