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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pipeline lazy initializer.
 *
 * @param <T> the type of the object managed by this initializer class
 */
public abstract class PipelineLazyInitializer<T> extends LazyInitializer<T> {
    
    private final AtomicBoolean initialized = new AtomicBoolean();
    
    @Override
    protected final T initialize() {
        T result = doInitialize();
        initialized.set(true);
        return result;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized.get();
    }
    
    protected abstract T doInitialize();
}
