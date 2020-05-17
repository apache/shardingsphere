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

package org.apache.shardingsphere.proxy.context;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.orchestration.core.common.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;

import java.util.Properties;

/**
 * Context of ShardingSphere-Proxy.
 */
@Getter
public final class ShardingSphereProxyContext {
    
    private static final ShardingSphereProxyContext INSTANCE = new ShardingSphereProxyContext();
    
    private ConfigurationProperties properties = new ConfigurationProperties(new Properties());
    
    private Authentication authentication;
    
    private boolean isCircuitBreak;
    
    private ShardingSphereProxyContext() {
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Get instance of ShardingSphere-Proxy's context.
     *
     * @return instance of ShardingSphere-Proxy's context.
     */
    public static ShardingSphereProxyContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy context.
     *
     * @param authentication authentication
     * @param props properties
     */
    public void init(final Authentication authentication, final Properties props) {
        this.authentication = authentication;
        properties = new ConfigurationProperties(props);
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationLogger.log(event.getProps());
        properties = new ConfigurationProperties(event.getProps());
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        ConfigurationLogger.log(event.getAuthentication());
        authentication = event.getAuthentication();
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param event circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent event) {
        isCircuitBreak = event.isCircuitBreak();
    }
}
