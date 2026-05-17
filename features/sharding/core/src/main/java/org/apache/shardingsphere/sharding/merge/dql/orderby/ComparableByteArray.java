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

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Comparable wrapper for byte arrays ordered by unsigned lexicographic comparison, matching MySQL VARBINARY collation.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public final class ComparableByteArray implements Comparable<ComparableByteArray> {
    
    private final byte[] value;
    
    @Override
    public int compareTo(final ComparableByteArray other) {
        int minLength = Math.min(value.length, other.value.length);
        for (int index = 0; index < minLength; index++) {
            int diff = (value[index] & 0xFF) - (other.value[index] & 0xFF);
            if (0 != diff) {
                return diff;
            }
        }
        return value.length - other.value.length;
    }
}
