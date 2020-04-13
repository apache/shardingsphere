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

package org.apache.shardingsphere.orchestration.core.metadatacenter.ignore;

import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Metadata refresh ignore.
 */
@NoArgsConstructor
public final class MetadataRefreshIgnore {
    
    private static final MetadataRefreshIgnore INSTANCE = new MetadataRefreshIgnore();
    
    private volatile AtomicBoolean myself = new AtomicBoolean(false);
    
    /**
     * Gets instance.
     *
     * @return metadata refresh ignore
     */
    public static MetadataRefreshIgnore getInstance() {
        return INSTANCE;
    }
    
    /**
     * Set myself.
     */
    public void setMyself() {
        myself.compareAndSet(false, true);
    }
    
    /**
     * Get myself.
     *
     * @return myself
     */
    public boolean getMyself() {
        boolean result = myself.get();
        myself.set(false);
        return result;
    }
}

