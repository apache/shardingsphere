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

package org.apache.shardingsphere.shardingproxy.context;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.state.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Properties;

/**
 * Global context.
 *
 * @author chenqingyang
 * @author panjuan
 */
@Getter
public final class GlobalContext {
    
    private static final GlobalContext INSTANCE = new GlobalContext();
    
    private ShardingProperties shardingProperties = new ShardingProperties(new Properties());
    
    private ShardingTransactionManagerEngine shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
    
    private Authentication authentication;
    
    private boolean isCircuitBreak;
    
    private GlobalContext() {
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Get instance of proxy context.
     *
     * @return instance of proxy context.
     */
    public static GlobalContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy context.
     *
     * @param authentication authentication
     * @param props properties
     */
    public void init(final Authentication authentication, final Properties props) {
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        this.authentication = authentication;
    }
    
    /**
     * Get transaction type.
     *
     * @return transaction type
     */
    public TransactionType getTransactionType() {
        return TransactionType.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_TYPE));
    }
    
    /**
     * Renew properties.
     *
     * @param propertiesChangedEvent properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent propertiesChangedEvent) {
        shardingProperties = new ShardingProperties(propertiesChangedEvent.getProps());
    }
    
    /**
     * Renew authentication.
     *
     * @param authenticationChangedEvent authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent authenticationChangedEvent) {
        authentication = authenticationChangedEvent.getAuthentication();
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param circuitStateChangedEvent circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent circuitStateChangedEvent) {
        isCircuitBreak = circuitStateChangedEvent.isCircuitBreak();
    }
}
