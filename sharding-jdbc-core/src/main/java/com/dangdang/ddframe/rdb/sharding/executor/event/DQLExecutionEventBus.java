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

/**
 * DQL类SQL执行时的事件发布总线.
 * 
 * @author gaohongtao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DQLExecutionEventBus {
    
    private static final String NAME = "DQL-EventBus";
    
    /**
     * 发布DQL类SQL执行事件.
     * 
     * @param event DQL类SQL执行事件
     */
    public static void post(final DQLExecutionEvent event) {
        ExecutionEventBusFactory.getInstance(NAME).post(event);
    }
    
    /**
     * 发布DQL类SQL执行事件监听器.
     * 
     * @param listener DQL类SQL执行事件监听器
     */
    public static void register(final DQLExecutionEventListener listener) {
        ExecutionEventBusFactory.getInstance(NAME).register(listener);
    }
    
    /**
     * 清除监听器.
     */
    public static void clearListener() {
        ExecutionEventBusFactory.getInstance(NAME).clearListener();
    }
}
