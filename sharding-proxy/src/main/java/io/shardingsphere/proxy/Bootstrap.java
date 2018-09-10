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

import io.shardingsphere.core.api.config.ProxyServerConfiguration;
import io.shardingsphere.core.api.config.ProxySchemaRule;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.config.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.YamlProxyServerConfiguration;
import io.shardingsphere.proxy.config.YamlProxyShardingRuleConfiguration;
import io.shardingsphere.proxy.config.loader.ProxyConfigLoader;
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
    
    private static final ProxyConfigLoader CONFIG_LOADER = ProxyConfigLoader.getInstance();
    
    private static final ProxyContext PROXY_CONTEXT = ProxyContext.getInstance();
    
    /**
     * Main Entrance.
     *
     * @param args startup arguments
     * @throws InterruptedException interrupted exception
     * @throws IOException          IO exception
     */
    public static void main(final String[] args) throws InterruptedException, IOException {
        CONFIG_LOADER.loadConfiguration();
        int port = getPort(args);
        new ProxyListenerRegister().register();
        if (null == CONFIG_LOADER.getYamlServerConfiguration().getOrchestration()) {
            startWithoutRegistryCenter(CONFIG_LOADER.getYamlServerConfiguration(), CONFIG_LOADER.getYamlProxyShardingRuleConfigurations(), port);
        } else {
            startWithRegistryCenter(CONFIG_LOADER.getYamlServerConfiguration(), CONFIG_LOADER.getYamlProxyShardingRuleConfigurations(), port);
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
    
    private static void startWithoutRegistryCenter(final YamlProxyServerConfiguration serverConfiguration, final Collection<YamlProxyShardingRuleConfiguration> shardingRuleConfigurations,
                                                   final int port) throws InterruptedException {
        OrchestrationProxyConfiguration configuration = getOrchestrationConfiguration(serverConfiguration, shardingRuleConfigurations);
        PROXY_CONTEXT.init(configuration.getServerConfiguration(), configuration.getSchemaDataSourceMap(), configuration.getSchemaShardingRuleMap());
        new ShardingProxy().start(port);
    }
    
    private static void startWithRegistryCenter(final YamlProxyServerConfiguration serverConfiguration, final Collection<YamlProxyShardingRuleConfiguration> shardingRuleConfigurations,
                                                final int port) throws InterruptedException {
        try (OrchestrationFacade orchestrationFacade = new OrchestrationFacade(serverConfiguration.getOrchestration().getOrchestrationConfiguration())) {
            if(!shardingRuleConfigurations.isEmpty()){
                orchestrationFacade.init(getOrchestrationConfiguration(serverConfiguration, shardingRuleConfigurations));
            }
            PROXY_CONTEXT.init(orchestrationFacade.getConfigService().loadProxyServerConiguration(), orchestrationFacade.getConfigService().loadProxyDataSources(),
                    orchestrationFacade.getConfigService().loadProxyConfiguration());
            new ShardingProxy().start(port);
        }
    }
    
    private static OrchestrationProxyConfiguration getOrchestrationConfiguration(final YamlProxyServerConfiguration serverConfiguration, 
                                                                                 final Collection<YamlProxyShardingRuleConfiguration> shardingRuleConfigurations) {
        Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap = new HashMap<>();
        Map<String, ProxySchemaRule> schemaShardingRuleMap = new HashMap<>();
        for (YamlProxyShardingRuleConfiguration localConfig : shardingRuleConfigurations) {
            ProxySchemaRule proxySchemaRule = new ProxySchemaRule(localConfig.getShardingRule(), localConfig.getMasterSlaveRule());
            schemaShardingRuleMap.put(localConfig.getSchemaName(), proxySchemaRule);
            schemaDataSourceMap.put(localConfig.getSchemaName(), localConfig.getDataSources());
        }
        return new OrchestrationProxyConfiguration(new ProxyServerConfiguration(serverConfiguration.getProxyAuthority(), serverConfiguration.getProps()), schemaDataSourceMap, schemaShardingRuleMap);
        
    }
}
