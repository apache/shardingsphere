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

import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.yaml.ProxyConfiguration;
import io.shardingsphere.proxy.config.yaml.ProxyYamlConfigurationLoader;
import io.shardingsphere.proxy.config.yaml.ProxyYamlRuleConfiguration;
import io.shardingsphere.proxy.config.yaml.ProxyYamlServerConfiguration;
import io.shardingsphere.proxy.frontend.ShardingProxy;
import io.shardingsphere.proxy.listener.ProxyListenerRegister;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer;

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
        ShardingTracer.init(new SkywalkingTracer());
        ProxyConfiguration proxyConfig = new ProxyYamlConfigurationLoader().load();
        int port = getPort(args);
        new ProxyListenerRegister().register();
        if (null == proxyConfig.getServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(proxyConfig.getServerConfiguration(), proxyConfig.getRuleConfigurationMap(), port);
        } else {
            startWithRegistryCenter(proxyConfig.getServerConfiguration(), proxyConfig.getRuleConfigurationMap(), port);
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
        ProxyContext.getInstance().init(getYamlServerConfiguration(serverConfig), getSchemaDataSourceMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(
            final ProxyYamlServerConfiguration serverConfig, final Map<String, ProxyYamlRuleConfiguration> ruleConfigs, final int port) throws InterruptedException {
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(serverConfig.getOrchestration().getOrchestrationConfiguration())) {
            if (!ruleConfigs.isEmpty()) {
                orchestrationFacade.init(getYamlServerConfiguration(serverConfig), getSchemaDataSourceMap(ruleConfigs), getRuleConfiguration(ruleConfigs));
            }
            ProxyContext.getInstance().init(orchestrationFacade.getConfigService().loadYamlServerConfiguration(), 
                    orchestrationFacade.getConfigService().loadProxyDataSources(), orchestrationFacade.getConfigService().loadProxyConfiguration());
            new ShardingProxy().start(port);
        }
    }
    
    private static YamlServerConfiguration getYamlServerConfiguration(final ProxyYamlServerConfiguration serverConfig) {
        YamlServerConfiguration result = new YamlServerConfiguration();
        result.setProxyAuthority(serverConfig.getProxyAuthority());
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
