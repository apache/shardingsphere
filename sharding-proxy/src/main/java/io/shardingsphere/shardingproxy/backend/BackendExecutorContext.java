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

package io.shardingsphere.shardingproxy.backend;

import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Backend executor context.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendExecutorContext {
    
    private static final BackendExecutorContext INSTANCE = new BackendExecutorContext();
    
    @Getter
    private final ShardingExecuteEngine executeEngine = new ShardingExecuteEngine(GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.EXECUTOR_SIZE));
    
    /**
     * Get backend executor context instance.
     * 
     * @return instance of executor context
     */
    public static BackendExecutorContext getInstance() {
        return INSTANCE;
    }
}
