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

package io.shardingsphere.proxy.frontend.common.executor;

import io.netty.channel.ChannelId;
import io.netty.channel.EventLoopGroup;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.proxy.config.ProxyContext;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

/**
 * Executor group.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ExecutorGroup {
    
    private static final ProxyContext PROXY_CONTEXT = ProxyContext.getInstance();
    
    private final EventLoopGroup eventLoopGroup;
    
    private final ChannelId channelId;
    
    /**
     * Get executor service.
     * 
     * @return executor service
     */
    public ExecutorService getExecutorService() {
        return TransactionType.XA == PROXY_CONTEXT.getTransactionType() ? ChannelThreadExecutorGroup.getInstance().get(channelId) : eventLoopGroup;
    }
}
