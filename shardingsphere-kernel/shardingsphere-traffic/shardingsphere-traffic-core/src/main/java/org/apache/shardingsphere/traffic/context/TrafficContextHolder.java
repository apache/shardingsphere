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

package org.apache.shardingsphere.traffic.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * Hold traffic context for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrafficContextHolder {
    
    private static final ThreadLocal<TrafficContext> TRAFFIC_CONTEXT = new ThreadLocal<>();
    
    /**
     * Set traffic context.
     *
     * @param trafficContext traffic context
     */
    public static void set(final TrafficContext trafficContext) {
        TRAFFIC_CONTEXT.set(trafficContext);
    }
    
    /**
     * Get traffic context.
     *
     * @return traffic context
     */
    public static Optional<TrafficContext> get() {
        return Optional.ofNullable(TRAFFIC_CONTEXT.get());
    }
    
    /**
     * Remove traffic context.
     */
    public static void remove() {
        TRAFFIC_CONTEXT.remove();
    }
}
