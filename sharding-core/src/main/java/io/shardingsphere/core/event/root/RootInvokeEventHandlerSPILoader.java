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

package io.shardingsphere.core.event.root;

import java.util.ServiceLoader;

/**
 * Root invoke event handler SPI loader.
 *
 * @author zhangliang
 */
public final class RootInvokeEventHandlerSPILoader {
    
    private static final RootInvokeEventHandlerSPILoader INSTANCE = new RootInvokeEventHandlerSPILoader();
    
    private final ServiceLoader<RootInvokeEventHandler> serviceLoader;
    
    private RootInvokeEventHandlerSPILoader() {
        serviceLoader = ServiceLoader.load(RootInvokeEventHandler.class);
    }
    
    /**
     * Get instance.
     * 
     * @return instance
     */
    public static RootInvokeEventHandlerSPILoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Handle root invoke start event.
     *
     * @param event root invoke start event
     */
    public void handle(final RootInvokeStartEvent event) {
        for (RootInvokeEventHandler each : serviceLoader) {
            each.handle(event);
        }
    }
    
    /**
     * Handle root invoke finish event.
     *
     * @param event root invoke finish event
     */
    public void handle(final RootInvokeFinishEvent event) {
        for (RootInvokeEventHandler each : serviceLoader) {
            each.handle(event);
        }
    }
}
