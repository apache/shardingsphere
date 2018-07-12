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

package io.shardingsphere.proxy.yaml;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusEvent;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusInstance;
import io.shardingsphere.proxy.config.RuleRegistry;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

/**
 * Yaml sharding configuration for proxy.
 *
 * @author zhangyonglun
 * @author panjuan
 */
public final class YamlProxyConfiguration extends OrchestrationProxyConfiguration {
    
    /**
     * Unmarshal yaml sharding configuration from yaml file.
     * 
     * @param yamlFile yaml file
     * @return yaml sharding configuration
     * @throws IOException IO Exception
     *
     */
    public static YamlProxyConfiguration unmarshal(final File yamlFile) throws IOException {
        try (
                FileInputStream fileInputStream = new FileInputStream(yamlFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8")
        ) {
            return new Yaml(new Constructor(YamlProxyConfiguration.class)).loadAs(inputStreamReader, YamlProxyConfiguration.class);
        }
    }
    
    /**
     * Initialize yaml proxy configuration.
     */
    public void init() {
        if (isFromRegistryCenter()) {
            OrchestrationFacade orchestrationFacade = new OrchestrationFacade(getOrchestration().getOrchestrationConfiguration());
            renew(orchestrationFacade.getConfigService().loadDataSources(), orchestrationFacade.getConfigService().loadProxyConfiguration());
        }
        ProxyEventBusInstance.getInstance().register(new YamlProxyConfiguration());
    }
    
    private boolean isFromRegistryCenter() {
        return null != getOrchestration() && getShardingRule().getTables().isEmpty() && null == getMasterSlaveRule().getMasterDataSourceName();
    }
    
    /**
     * Renew yaml proxy configuration.
     *
     * @param proxyEventBusEvent proxy event bus event.
     */
    @Subscribe
    public void renew(final ProxyEventBusEvent proxyEventBusEvent) {
        renew(proxyEventBusEvent.getDataSources(), proxyEventBusEvent.getOrchestrationConfig());
        RuleRegistry.getInstance().init(this);
    }
    
    /**
     * Get sharding rule from yaml.
     *
     * @param dataSourceNames data source names
     * @return sharding rule from yaml
     */
    public ShardingRule obtainShardingRule(final Collection<String> dataSourceNames) {
        return new ShardingRule(getShardingRule().getShardingRuleConfiguration(), dataSourceNames.isEmpty() ? getDataSources().keySet() : dataSourceNames);
    }
    
    /**
     * Get master slave rule from yaml.
     *
     * @return master slave rule.
     */
    public MasterSlaveRule obtainMasterSlaveRule() {
        return null == getMasterSlaveRule().getMasterDataSourceName() ? new MasterSlaveRule(new MasterSlaveRuleConfiguration("", "", Collections.singletonList(""), null))
                : new MasterSlaveRule(getMasterSlaveRule().getMasterSlaveRuleConfiguration());
    }
    
    /**
     * Get orchestration configuration.
     *
     * @return Orchestration configuration
     */
    public Optional<OrchestrationConfiguration> obtainOrchestrationConfiguration() {
        return null != getOrchestration() ? Optional.fromNullable(getOrchestration().getOrchestrationConfiguration()) : Optional.<OrchestrationConfiguration>absent();
    }
}
