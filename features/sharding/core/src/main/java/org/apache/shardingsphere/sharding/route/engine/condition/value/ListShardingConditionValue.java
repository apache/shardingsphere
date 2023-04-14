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

package org.apache.shardingsphere.sharding.route.engine.condition.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharding condition value for list values.
 * 
 * @param <T> type of sharding condition value
 */
@RequiredArgsConstructor
@Getter
public final class ListShardingConditionValue<T> implements ShardingConditionValue {
    
    private final String columnName;
    
    private final String tableName;
    
    private final Collection<T> values;
    
    private final List<Integer> parameterMarkerIndexes;
    
    public ListShardingConditionValue(final String columnName, final String tableName, final Collection<T> values) {
        this(columnName, tableName, values, Collections.emptyList());
    }
    
    @Override
    public String toString() {
        String condition = 1 == values.size() ? " = " + new ArrayList<>(values).get(0) : " in (" + values.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
        return tableName + "." + columnName + condition;
    }
}
