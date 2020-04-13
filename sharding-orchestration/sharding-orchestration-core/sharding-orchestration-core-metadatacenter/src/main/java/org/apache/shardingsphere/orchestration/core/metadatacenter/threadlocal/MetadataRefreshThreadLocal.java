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

package org.apache.shardingsphere.orchestration.core.metadatacenter.threadlocal;

import lombok.NoArgsConstructor;

/**
 * Metadata refresh thread local.
 */
@NoArgsConstructor
public final class MetadataRefreshThreadLocal {
    
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();
    
    private static final MetadataRefreshThreadLocal INSTANCE = new MetadataRefreshThreadLocal();
    
    /**
     * Gets instance.
     *
     * @return metadata refresh thread local
     */
    public static MetadataRefreshThreadLocal getInstance() {
        return INSTANCE;
    }
    
    /**
     * Set object for thread local.
     *
     * @param object object
     */
    public void set(final Object object) {
        THREAD_LOCAL.set(object);
    }
    
    /**
     * Gets and remove.
     *
     * @return object
     */
    public Object getAndRemove() {
        Object result = THREAD_LOCAL.get();
        THREAD_LOCAL.remove();
        return result;
    }
    
    /**
     * clean thread local for gc.
     */
    public void remove() {
        THREAD_LOCAL.remove();
    }
}

