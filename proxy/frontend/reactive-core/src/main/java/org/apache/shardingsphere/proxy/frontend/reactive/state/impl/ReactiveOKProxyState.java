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

package org.apache.shardingsphere.proxy.frontend.reactive.state.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.reactive.command.ReactiveCommandExecuteTask;
import org.apache.shardingsphere.proxy.frontend.reactive.protocol.ReactiveDatabaseProtocolFrontendEngineFactory;
import org.apache.shardingsphere.proxy.frontend.reactive.spi.ReactiveDatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.impl.OKProxyState;

/**
 * Reactive OK proxy state.
 */
public final class ReactiveOKProxyState implements OKProxyState {
    
    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        ReactiveDatabaseProtocolFrontendEngine reactiveDatabaseProtocolFrontendEngine = getOrCreateReactiveEngine(context.channel(), databaseProtocolFrontendEngine);
        context.executor().execute(new ReactiveCommandExecuteTask(reactiveDatabaseProtocolFrontendEngine, connectionSession, context, message));
    }
    
    private ReactiveDatabaseProtocolFrontendEngine getOrCreateReactiveEngine(final Channel channel, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine) {
        Attribute<ReactiveDatabaseProtocolFrontendEngine> attr = channel.attr(AttributeKey.valueOf(ReactiveDatabaseProtocolFrontendEngine.class.getName()));
        ReactiveDatabaseProtocolFrontendEngine result = attr.get();
        if (null == result) {
            result = ReactiveDatabaseProtocolFrontendEngineFactory.newInstance(databaseProtocolFrontendEngine.getType());
            attr.setIfAbsent(result);
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "ExperimentalVertx";
    }
}
