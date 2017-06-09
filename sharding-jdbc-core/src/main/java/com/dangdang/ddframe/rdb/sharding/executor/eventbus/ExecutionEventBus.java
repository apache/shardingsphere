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

package com.dangdang.ddframe.rdb.sharding.executor.eventbus;

import com.dangdang.ddframe.rdb.sharding.executor.eventbus.event.AbstractExecutionEvent;
import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutionEventBus {
    
    private static final ExecutionEventBus INSTANCE = new ExecutionEventBus();
    
    private final EventBus instance = new EventBus();
    
    private final ConcurrentHashMap<String, ExecutionEventListener> listeners = new ConcurrentHashMap<>();
    
    /**
     * 获取事件总线实例.
     * 
     * @return 事件总线实例
     */
    public static ExecutionEventBus getInstance() {
        return INSTANCE;
    }
    
    /**
     * SQL执行事件.
     *
     * @param event SQL执行事件
     */
    public void post(final AbstractExecutionEvent event) {
        if (!listeners.isEmpty()) {
            instance.post(event);
        }
    }
    
    /**
     * 注册事件监听器.
     *
     * @param listener SQL执行事件监听器
     */
    public void register(final ExecutionEventListener listener) {
        if (null == listeners.putIfAbsent(listener.getName(), listener)) {
            instance.register(listener);
        }
    }
    
    /**
     * 清除监听器.
     */
    public void clearListener() {
        for (ExecutionEventListener each : listeners.values()) {
            instance.unregister(each);
        }
        listeners.clear();
    }
}
