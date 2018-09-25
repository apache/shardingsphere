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

package io.shardingsphere.core.spi.root;

import java.util.ServiceLoader;

/**
 * Root invoke handler loader.
 *
 * @author zhangliang
 */
public final class RootInvokeHandlerLoader {
    
    private static final RootInvokeHandlerLoader INSTANCE = new RootInvokeHandlerLoader();
    
    private final ServiceLoader<RootInvokeHandler> serviceLoader;
    
    private RootInvokeHandlerLoader() {
        serviceLoader = ServiceLoader.load(RootInvokeHandler.class);
    }
    
    /**
     * Get instance.
     * 
     * @return instance
     */
    public static RootInvokeHandlerLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Start invoke.
     */
    public void start() {
        for (RootInvokeHandler each : serviceLoader) {
            each.start();
        }
    }
    
    /**
     * Finish invoke.
     */
    public void finish() {
        for (RootInvokeHandler each : serviceLoader) {
            each.finish();
        }
    }
}
