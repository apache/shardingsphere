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

import com.google.common.eventbus.EventBus;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线.
 * 
 * @author gaohongtao.
 */
public class ExecutionEventBus {
    
    private final EventBus instance = new EventBus();
    
    private final ConcurrentHashMap<String, ExecutionEventListener> listeners = new ConcurrentHashMap<>();
    
    /**
     * SQL执行事件.
     *
     * @param event SQL执行事件
     */
    public void post(final ExecutionEvent event) {
        if (listeners.isEmpty()) {
            return;
        }
        instance.post(event);
    }
    
    /**
     * 注册事件监听器.
     *
     * @param listener DML类SQL执行事件监听器
     */
    public void register(final ExecutionEventListener listener) {
        if (null != listeners.putIfAbsent(listener.getName(), listener)) {
            return;
        }
        instance.register(listener);
    }
    
    /**
     * 清除监听器.
     */
    public synchronized void clearListener() {
        for (ExecutionEventListener each : listeners.values()) {
            instance.unregister(each);
        }
        listeners.clear();
    }
}
