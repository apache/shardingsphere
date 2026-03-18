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
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.impl.CircuitBreakProxyState;
import org.apache.shardingsphere.proxy.frontend.state.impl.OKProxyState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy state context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyStateContext {
    
    private static final Map<InstanceState, ProxyState> STATES = new ConcurrentHashMap<>(2, 1F);
    
    static {
        STATES.put(InstanceState.OK, new OKProxyState());
        STATES.put(InstanceState.CIRCUIT_BREAK, new CircuitBreakProxyState());
    }
    
    /**
     * Execute command.
     *
     * @param context channel handler context
     * @param message message
     * @param databaseProtocolFrontendEngine database protocol frontend engine
     * @param connectionSession connection session
     */
    public static void execute(final ChannelHandlerContext context, final Object message,
                               final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        InstanceState currentState = ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance().getState().getCurrentState();
        STATES.get(currentState).execute(context, message, databaseProtocolFrontendEngine, connectionSession);
    }
}
