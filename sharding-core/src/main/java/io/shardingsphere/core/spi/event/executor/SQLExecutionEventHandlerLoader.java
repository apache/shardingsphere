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

package io.shardingsphere.core.spi.event.executor;

import java.util.ServiceLoader;

/**
 * SQL Execution event handler loader.
 *
 * @author zhangliang
 */
public final class SQLExecutionEventHandlerLoader {
    
    private static final SQLExecutionEventHandlerLoader INSTANCE = new SQLExecutionEventHandlerLoader();
    
    private final ServiceLoader<SQLExecutionEventHandler> serviceLoader;
    
    private SQLExecutionEventHandlerLoader() {
        serviceLoader = ServiceLoader.load(SQLExecutionEventHandler.class);
    }
    
    /**
     * Get instance.
     * 
     * @return instance
     */
    public static SQLExecutionEventHandlerLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Handle SQL execution start event.
     *
     * @param event SQL execution start event
     */
    public void start(final SQLExecutionStartEvent event) {
        for (SQLExecutionEventHandler each : serviceLoader) {
            each.start(event);
        }
    }
    
    /**
     * Handle SQL execution finish event.
     *
     * @param event SQL execution finish event
     */
    public void finish(final SQLExecutionFinishEvent event) {
        for (SQLExecutionEventHandler each : serviceLoader) {
            each.finish(event);
        }
    }
}
