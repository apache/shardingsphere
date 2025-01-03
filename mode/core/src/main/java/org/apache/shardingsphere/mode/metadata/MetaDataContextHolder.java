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

package org.apache.shardingsphere.mode.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Meta data context holder.
 */
@AllArgsConstructor
@Slf4j
public final class MetaDataContextHolder {
    
    @Getter
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final CompletableFuture<MetaDataContexts> future = new CompletableFuture<>();
    
    /**
     * Get meta data contexts.
     *
     * @return meta data contexts
     */
    public MetaDataContexts getMetaDataContextsAsync() {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (final InterruptedException | java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException ex) {
            return metaDataContexts.get();
        }
    }
    
    /**
     * Update meta data contexts.
     *
     * @param reloadMetaDataContexts reload meta data contexts
     */
    public void updateMetaDataContextsAsync(final MetaDataContexts reloadMetaDataContexts) {
        metaDataContexts.set(reloadMetaDataContexts);
        future.complete(reloadMetaDataContexts);
    }
}
