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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

/**
 * Connection limit context.
 */
public final class ConnectionLimitContext {
    
    public static final ConnectionLimitContext INSTANCE = new ConnectionLimitContext();

    private final AtomicInteger activeConnections = new AtomicInteger();
    
    private ConnectionLimitContext() {
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Channel active state.
     * @return Whether the connection can be established.
     */
    public boolean connect() {
        if (this.getConnectionLimit() < 0) {
            return true;
        }
        if (this.activeConnections.incrementAndGet() <= this.getConnectionLimit()) {
            return true;
        }
        return false;
    }
    
    /**
     * Channel inactive state.
     */
    public void disconnect() {
        if (this.getConnectionLimit() < 0) {
            return;
        }
        this.activeConnections.decrementAndGet();
    }
    
    /**
     * Connection limit size.
     * @return limit size.
     */
    public int getConnectionLimit() {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_CONNECTION_LIMIT);
    }
}
