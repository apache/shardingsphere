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

package io.shardingsphere.core.bootstrap;

import io.shardingsphere.core.event.ShardingEventListenerRegistrySPILoader;
import io.shardingsphere.core.spi.connection.close.SPICloseConnectionHook;
import io.shardingsphere.core.spi.connection.get.SPIGetConnectionHook;
import io.shardingsphere.core.spi.executor.SPISQLExecutionHook;
import io.shardingsphere.core.spi.parsing.SPIParsingHook;
import io.shardingsphere.core.spi.root.SPIRootInvokeHook;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding bootstrap.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingBootstrap {
    
    /**
     * Initialize sharding bootstrap.
     */
    public static void init() {
        ShardingEventListenerRegistrySPILoader.registerListeners();
        new SPIRootInvokeHook();
        new SPIParsingHook();
        new SPIGetConnectionHook();
        new SPISQLExecutionHook();
        new SPICloseConnectionHook();
    }
}
