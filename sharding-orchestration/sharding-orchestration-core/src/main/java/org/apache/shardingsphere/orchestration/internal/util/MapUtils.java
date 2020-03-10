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

package org.apache.shardingsphere.orchestration.internal.util;

import com.google.common.base.Function;

import java.util.HashMap;
import java.util.Map;

/**
 * Utils for Map.
 */
public final class MapUtils {
    private MapUtils() { }

    /**
     * Convert values from a certain map object.
     * @param fromMap the origin Map object
     * @param function the function to convert values
     * @param <K> key type
     * @param <V> origin value type
     * @param <T> target value type
     * @return the transformed map object
     */
    public static <K, V, T> Map<K, T> transformValues(final Map<K, V> fromMap, final Function<V, T> function) {
        final Map<K, T> toMap = new HashMap<>(fromMap.size());
        fromMap.entrySet().forEach(entry -> toMap.put(entry.getKey(), function.apply(entry.getValue())));
        return toMap;
    }
}
