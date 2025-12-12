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

package org.apache.shardingsphere.data.pipeline.core.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline context manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineContextManager {
    
    private static final Map<PipelineContextKey, ContextManager> CONTEXT_MAP = new ConcurrentHashMap<>();
    
    /**
     * Get context.
     *
     * @param key key
     * @return context
     */
    public static ContextManager getContext(final PipelineContextKey key) {
        return CONTEXT_MAP.get(key);
    }
    
    /**
     * Get context.
     *
     * @return context
     */
    public static ContextManager getProxyContext() {
        return CONTEXT_MAP.get(new PipelineContextKey(InstanceType.PROXY));
    }
    
    /**
     * Put context.
     *
     * @param key key
     * @param context context
     */
    public static void putContext(final PipelineContextKey key, final ContextManager context) {
        CONTEXT_MAP.put(key, context);
    }
    
    /**
     * Remove context.
     *
     * @param key key
     */
    public static void removeContext(final PipelineContextKey key) {
        CONTEXT_MAP.remove(key);
    }
}
