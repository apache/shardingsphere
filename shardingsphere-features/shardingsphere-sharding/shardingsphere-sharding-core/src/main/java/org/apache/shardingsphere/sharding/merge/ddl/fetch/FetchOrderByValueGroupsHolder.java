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

package org.apache.shardingsphere.sharding.merge.ddl.fetch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold fetch order by value groups for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FetchOrderByValueGroupsHolder {
    
    private static final ThreadLocal<Map<String, List<FetchOrderByValueGroup>>> ORDER_BY_VALUE_GROUPS = ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    private static final ThreadLocal<Map<String, Long>> MIN_GROUP_ROW_COUNTS = ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    /**
     * Get order by value groups.
     *
     * @return order by value groups
     */
    public static Map<String, List<FetchOrderByValueGroup>> getOrderByValueGroups() {
        return ORDER_BY_VALUE_GROUPS.get();
    }
    
    /**
     * Get min group row counts.
     *
     * @return min group row counts
     */
    public static Map<String, Long> getMinGroupRowCounts() {
        return MIN_GROUP_ROW_COUNTS.get();
    }
    
    /**
     * Remove.
     */
    public static void remove() {
        ORDER_BY_VALUE_GROUPS.remove();
        MIN_GROUP_ROW_COUNTS.remove();
    }
}
