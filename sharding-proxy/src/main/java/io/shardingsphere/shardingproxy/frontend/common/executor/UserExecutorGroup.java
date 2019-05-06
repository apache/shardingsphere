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

import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.util.ShardingExecutorService;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import lombok.Getter;

/**
 * Command execute engine.
 *
 * @author wuxu
 * @author zhaojun
 */
public final class UserExecutorGroup implements AutoCloseable {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private static final String NAME_FORMAT = "Command-%d";
    
    private static final UserExecutorGroup INSTANCE = new UserExecutorGroup();
    
    private ShardingExecutorService shardingExecutorService;
    
    @Getter
    private final ListeningExecutorService executorService;
    
    private UserExecutorGroup() {
        shardingExecutorService = new ShardingExecutorService(GLOBAL_REGISTRY.getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.ACCEPTOR_SIZE), NAME_FORMAT);
        executorService = shardingExecutorService.getExecutorService();
    }
    
    /**
     * Get instance of user executor group.
     *
     * @return user executor group
     */
    public static UserExecutorGroup getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void close() {
        shardingExecutorService.close();
    }
}
