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

package io.shardingsphere.shardingproxy.listener;

import io.shardingsphere.core.bootstrap.ShardingBootstrap;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;

/**
 * Listener register for Proxy.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class ProxyListenerRegister {
    
    private final GlobalRegistry globalRegistry = GlobalRegistry.getInstance();
    
    /**
     * Register all listeners.
     */
    public void register() {
        ShardingBootstrap.init();
        globalRegistry.register();
    }
}
