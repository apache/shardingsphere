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

package io.shardingsphere.shardingproxy.runtime;

import io.netty.channel.Channel;
import io.shardingsphere.shardingproxy.backend.netty.result.collector.QueryResultCollector;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime context.
 *
 * @author wuxu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class RuntimeContext {
    
    private static final RuntimeContext INSTANCE = new RuntimeContext();
    
    private final ThreadLocal<Channel> localFrontendChannel = new ThreadLocal<>();
    
    private final ThreadLocal<String> commandPacketId = new ThreadLocal<>();
    
    private final Map<String, CommandExecutor> uniqueCommandExecutor = new ConcurrentHashMap<>();
    
    private final Map<String, Channel> frontendChannel = new ConcurrentHashMap<>();
    
    private final Map<String, FrontendHandler> frontendChannelHandler = new ConcurrentHashMap<>();
    
    private final Map<String, QueryResultCollector> backendChannelQueryResultCollector = new ConcurrentHashMap<>();
    
    /**
     * Get instance of RuntimeContext.
     *
     * @return instance of RuntimeContext
     */
    public static RuntimeContext getInstance() {
        return INSTANCE;
    }
}

