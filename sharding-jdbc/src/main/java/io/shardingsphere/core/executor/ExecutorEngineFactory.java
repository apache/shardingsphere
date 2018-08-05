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

package io.shardingsphere.core.executor;

import io.shardingsphere.core.constant.ProxyMode;
import io.shardingsphere.core.executor.type.connection.ConnectionStrictlyExecutorEngine;
import io.shardingsphere.core.executor.type.memory.MemoryStrictlyExecutorEngine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JDBC execute engine factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorEngineFactory {
    
    /**
     * Create instance for execute engine.
     *
     * @param proxyMode proxy mode
     * @param executorSize executor size
     * @return instance for text protocol
     */
    public static ExecutorEngine createTextProtocolInstance(final ProxyMode proxyMode, final int executorSize) {
        return ProxyMode.MEMORY_STRICTLY == proxyMode ? new MemoryStrictlyExecutorEngine(executorSize) : new ConnectionStrictlyExecutorEngine(executorSize);
    }
}
