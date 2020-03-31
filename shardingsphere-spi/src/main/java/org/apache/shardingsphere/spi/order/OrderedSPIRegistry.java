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

package org.apache.shardingsphere.spi.order;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ordered SPI registry.
 */
public final class OrderedSPIRegistry {
    
    /**
     * Get registered services.
     * 
     * @param orderedSPIClass class of ordered SPI
     * @param <T> type of ordered SPI class
     * @return registered services
     */
    public static <T extends OrderedSPI> Collection<T> getRegisteredServices(final Class<T> orderedSPIClass) {
        Map<Integer, T> result = new TreeMap<>();
        for (T each : ShardingSphereServiceLoader.newServiceInstances(orderedSPIClass)) {
            result.put(each.getOrder(), each);
        }
        return result.values();
    }
}
