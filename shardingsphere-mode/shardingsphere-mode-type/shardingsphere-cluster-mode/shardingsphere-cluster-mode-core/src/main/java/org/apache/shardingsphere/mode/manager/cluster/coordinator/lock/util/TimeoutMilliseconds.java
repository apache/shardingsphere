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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * Timeout milliseconds.
 */
public final class TimeoutMilliseconds {
    
    public static final long MAX_TRY_LOCK = 3 * 60 * 1000L;
    
    public static final long MIN_TRY_LOCK = 200L;
    
    public static final long DEFAULT_REGISTRY = 50L;
    
    public static final long MAX_ACK_EXPEND = 100L;
    
    /**
     * Sleep interval.
     *
     * @param timeMilliseconds time milliseconds
     */
    @SneakyThrows(InterruptedException.class)
    public static void sleepInterval(final long timeMilliseconds) {
        TimeUnit.MILLISECONDS.sleep(timeMilliseconds);
    }
}
