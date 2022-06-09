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
import org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByValue;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold fetch order by value queues for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FetchOrderByValueQueuesHolder {
    
    private static final ThreadLocal<Map<String, Queue<OrderByValue>>> ORDER_BY_VALUE_QUEUES = ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    private static final ThreadLocal<Map<String, Long>> REMAINING_ROW_COUNTS = ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    /**
     * Get order by value queues.
     *
     * @return order by value queues
     */
    public static Map<String, Queue<OrderByValue>> getOrderByValueQueues() {
        return ORDER_BY_VALUE_QUEUES.get();
    }
    
    /**
     * Get remaining row counts.
     *
     * @return remaining row counts
     */
    public static Map<String, Long> getRemainingRowCounts() {
        return REMAINING_ROW_COUNTS.get();
    }
    
    /**
     * Remove fetch order by value queues.
     */
    public static void remove() {
        ORDER_BY_VALUE_QUEUES.remove();
        REMAINING_ROW_COUNTS.remove();
    }
}
