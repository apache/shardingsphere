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

package org.apache.shardingsphere.proxy.frontend.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection limit context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionLimitContext {
    
    private static final ConnectionLimitContext INSTANCE = new ConnectionLimitContext();
    
    private final AtomicInteger activeConnections = new AtomicInteger();
    
    /**
     * Get instance of ConnectionLimitContext.
     *
     * @return instance of ConnectionLimitContext.
     */
    public static ConnectionLimitContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Channel active state.
     *
     * @return Whether the connection can be established.
     */
    public boolean connectionAllowed() {
        return activeConnections.incrementAndGet() <= getMaxConnections() || !limitsMaxConnections();
    }
    
    /**
     * Channel inactive state.
     */
    public void connectionInactive() {
        activeConnections.decrementAndGet();
    }
    
    /**
     * Check if limits number of frontend connections.
     *
     * @return limits max connections
     */
    public boolean limitsMaxConnections() {
        return getMaxConnections() > 0;
    }
    
    /**
     * Get connection limit size.
     *
     * @return limit size.
     */
    public int getMaxConnections() {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS);
    }
}
