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

package org.apache.shardingsphere.scaling.core.spi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Map;
import java.util.TreeMap;

/**
 * Scaling entry factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalingEntryFactory {
    
    private static final Map<String, ScalingEntry> SCALING_ENTRY_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        ShardingSphereServiceLoader.register(ScalingEntry.class);
        for (ScalingEntry each : ShardingSphereServiceLoader.getSingletonServiceInstances(ScalingEntry.class)) {
            SCALING_ENTRY_MAP.put(each.getDatabaseType(), each);
        }
    }
    
    /**
     * Get instance of scaling entry.
     *
     * @param databaseType database type
     * @return instance of scaling entry
     */
    public static ScalingEntry getInstance(final String databaseType) {
        if (SCALING_ENTRY_MAP.containsKey(databaseType)) {
            return SCALING_ENTRY_MAP.get(databaseType);
        }
        throw new UnsupportedOperationException(String.format("Unsupported database type '%s'", databaseType));
    }
}
