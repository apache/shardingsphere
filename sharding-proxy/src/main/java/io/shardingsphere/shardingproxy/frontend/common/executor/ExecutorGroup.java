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

package io.shardingsphere.shardingproxy.frontend.common.executor;

import io.netty.channel.ChannelId;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutor;
import io.shardingsphere.shardingproxy.frontend.mysql.CommandExecutorContext;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Executor group.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ExecutorGroup {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private final ChannelId channelId;
    
    /**
     * Execute command.
     *
     * @param commandExecutor a command executor to be run
     */
    public void execute(final CommandExecutor commandExecutor) {
        if (TransactionType.XA == GLOBAL_REGISTRY.getTransactionType()) {
            ChannelThreadExecutorGroup.getInstance().get(channelId).execute(commandExecutor);
            return;
        }
        CommandExecutorContext.getInstance().getCommandExecuteEngine().execute(commandExecutor);
    }
}
