/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.executor.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线工厂.
 * 
 * @author gaohongtao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ExecutionEventBusFactory {
    
    private static final ConcurrentHashMap<String, ExecutionEventBus> CONTAINER = new ConcurrentHashMap<>();
    
    /**
     * 获取事件总线实例.
     * 
     * @param name 事件总线名称
     * @return 事件总线实例
     */
    public static ExecutionEventBus getInstance(final String name) {
        if (CONTAINER.containsKey(name)) {
            return CONTAINER.get(name);
        }
        CONTAINER.putIfAbsent(name, new ExecutionEventBus());
        return CONTAINER.get(name);
    }
}
