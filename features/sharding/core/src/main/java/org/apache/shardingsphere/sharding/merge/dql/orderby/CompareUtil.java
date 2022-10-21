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

package org.apache.shardingsphere.sharding.merge.dql.orderby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;

/**
 * Compare util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompareUtil {
    
    /**
     * Compare two object with order type.
     *
     * @param thisValue this value
     * @param otherValue other value
     * @param orderDirection order direction 
     * @param nullOrderDirection order direction for null value
     * @param caseSensitive case sensitive
     * @return compare result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compareTo(final Comparable thisValue, final Comparable otherValue, final OrderDirection orderDirection, final OrderDirection nullOrderDirection, final boolean caseSensitive) {
        if (null == thisValue && null == otherValue) {
            return 0;
        }
        if (null == thisValue) {
            return orderDirection == nullOrderDirection ? -1 : 1;
        }
        if (null == otherValue) {
            return orderDirection == nullOrderDirection ? 1 : -1;
        }
        if (!caseSensitive && thisValue instanceof String && otherValue instanceof String) {
            return compareToCaseInsensitiveString((String) thisValue, (String) otherValue, orderDirection);
        }
        return OrderDirection.ASC == orderDirection ? thisValue.compareTo(otherValue) : -thisValue.compareTo(otherValue);
    }
    
    private static int compareToCaseInsensitiveString(final String thisValue, final String otherValue, final OrderDirection orderDirection) {
        int result = thisValue.toUpperCase().compareTo(otherValue.toUpperCase());
        return OrderDirection.ASC == orderDirection ? result : -result;
    }
}
