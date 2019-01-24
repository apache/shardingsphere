/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.bootstrap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.spi.executor.SQLExecutionHook;
import org.apache.shardingsphere.spi.parsing.ParsingHook;
import org.apache.shardingsphere.spi.rewrite.RewriteHook;
import org.apache.shardingsphere.spi.root.RootInvokeHook;

/**
 * Sharding bootstrap.
 *
 * @author zhangliang
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingBootstrap {
    
    /**
     * Initialize sharding bootstrap.
     */
    public static void init() {
        registerHookClasses(SQLExecutionHook.class, ParsingHook.class, RootInvokeHook.class, RewriteHook.class);
    }
    
    private static void registerHookClasses(final Class<?>... services) {
        for (Class<?> each : services) {
            NewInstanceServiceLoader.register(each);
        }
    }
}
