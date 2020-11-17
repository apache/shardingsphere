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

package org.apache.shardingsphere.proxy.frontend.state.impl;

import com.google.common.eventbus.Subscribe;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;
import org.apache.shardingsphere.governance.core.registry.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.CircuitBreakException;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyState;
import org.apache.shardingsphere.proxy.frontend.state.ProxyStateMachine;
import org.apache.shardingsphere.proxy.frontend.state.ProxyStateType;

import java.util.Optional;

/**
 * Circuit break proxy state.
 */
public final class CircuitBreakProxyState implements ProxyState {
    
    public CircuitBreakProxyState() {
        GovernanceEventBus.getInstance().register(this);
    }
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final BackendConnection backendConnection) {
        context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(new CircuitBreakException()));
        Optional<DatabasePacket<?>> databasePacket = databaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket();
        databasePacket.ifPresent(context::writeAndFlush);
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param event circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent event) {
        if (event.isCircuitBreak()) {
            ProxyStateMachine.switchState(ProxyStateType.CIRCUIT_BREAK);
        } else {
            // TODO check previous state, maybe lock
            ProxyStateMachine.switchState(ProxyStateType.OK);
        }
    }
}
