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

package io.shardingsphere.core.spi;

import io.shardingsphere.core.spi.event.connection.close.CloseConnectionEventHandlerLoader;
import io.shardingsphere.core.spi.event.connection.get.GetConnectionEventHandlerLoader;
import io.shardingsphere.core.spi.event.executor.SQLExecutionEventHandlerLoader;
import io.shardingsphere.core.spi.event.parsing.ParsingEventHandlerLoader;
import io.shardingsphere.core.spi.root.RootInvokeHandlerLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding SPI loader.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSPILoader {
    
    /**
     * Load all sharding SPI.
     */
    public static void loadAllShardingSPI() {
        RootInvokeHandlerLoader.getInstance();
        ParsingEventHandlerLoader.getInstance();
        GetConnectionEventHandlerLoader.getInstance();
        SQLExecutionEventHandlerLoader.getInstance();
        CloseConnectionEventHandlerLoader.getInstance();
    }
}
