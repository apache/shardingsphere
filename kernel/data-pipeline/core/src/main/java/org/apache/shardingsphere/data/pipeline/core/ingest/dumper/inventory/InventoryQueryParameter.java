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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Inventory query parameter.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class InventoryQueryParameter {
    
    private final QueryType queryType;
    
    private final Object uniqueKeyValue;
    
    private final QueryRange uniqueKeyValueRange;
    
    /**
     * Build for point query.
     *
     * @param uniqueKeyValue unique key value
     * @return inventory query parameter
     */
    public static InventoryQueryParameter buildForPointQuery(final Object uniqueKeyValue) {
        return new InventoryQueryParameter(QueryType.POINT_QUERY, uniqueKeyValue, null);
    }
    
    /**
     * Build for range query.
     *
     * @param uniqueKeyValueRange unique key value range
     * @return inventory query parameter
     */
    public static InventoryQueryParameter buildForRangeQuery(final QueryRange uniqueKeyValueRange) {
        return new InventoryQueryParameter(QueryType.RANGE_QUERY, null, uniqueKeyValueRange);
    }
}
