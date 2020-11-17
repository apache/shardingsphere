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

package org.apache.shardingsphere.proxy.frontend.state;

import io.netty.channel.ChannelHandlerContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.impl.CircuitBreakProxyState;
import org.apache.shardingsphere.proxy.frontend.state.impl.LockProxyState;
import org.apache.shardingsphere.proxy.frontend.state.impl.OKProxyState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy state context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyStateContext {
    
    private static final Map<StateType, ProxyState> STATES = new ConcurrentHashMap<>(3, 1);
    
    static {
        STATES.put(StateType.OK, new OKProxyState());
        STATES.put(StateType.LOCK, new LockProxyState());
        STATES.put(StateType.CIRCUIT_BREAK, new CircuitBreakProxyState());
    }
    
    /**
     * Execute command.
     *
     * @param context channel handler context
     * @param message message
     * @param databaseProtocolFrontendEngine database protocol frontend engine
     * @param backendConnection backend connection
     */
    public static void execute(final ChannelHandlerContext context, final Object message, 
                               final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendConnection backendConnection) {
        STATES.get(StateContext.getCurrentState()).execute(context, message, databaseProtocolFrontendEngine, backendConnection);
    }
}
