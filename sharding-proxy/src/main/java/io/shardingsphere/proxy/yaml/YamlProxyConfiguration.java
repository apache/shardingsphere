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

import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusEvent;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Yaml sharding configuration for proxy.
 *
 * @author zhangyonglun
 * @author panjuan
 */
@NoArgsConstructor
public final class YamlProxyConfiguration extends OrchestrationProxyConfiguration {
    
    public YamlProxyConfiguration(final Map<String, DataSourceParameter> dataSources, final OrchestrationProxyConfiguration config) {
        setDataSources(dataSources);
        setShardingRule(config.getShardingRule());
        setMasterSlaveRule(config.getMasterSlaveRule());
        setProxyAuthority(config.getProxyAuthority());
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
}
