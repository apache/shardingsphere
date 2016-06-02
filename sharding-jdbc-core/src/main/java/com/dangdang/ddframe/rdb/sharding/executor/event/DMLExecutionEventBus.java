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
 * DML类SQL执行时的事件发布总线.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DMLExecutionEventBus {
    
    private static final String NAME = "DML-EventBus";
    
    /**
     * 发布DML类SQL执行事件.
     * 
     * @param event DML类SQL执行事件
     */
    public static void post(final DMLExecutionEvent event) {
        ExecutionEventBusFactory.getInstance(NAME).post(event);
    }
    
    /**
     * 发布DML类SQL执行事件监听器.
     * 
     * @param listener DML类SQL执行事件监听器
     */
    public static void register(final DMLExecutionEventListener listener) {
        ExecutionEventBusFactory.getInstance(NAME).register(listener);
    }
    
    /**
     * 清除监听器.
     */
    public static void clearListener() {
        ExecutionEventBusFactory.getInstance(NAME).clearListener();
    }
}
