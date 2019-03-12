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

package org.apache.shardingsphere.core.optimizer.result;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimizer.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;

import java.util.Collections;

/**
 * Optimize result.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class OptimizeResult {
    
    @Getter
    private final ShardingConditions shardingConditions;
    
    private final InsertColumnValues insertColumnValues;
    
    public OptimizeResult(final ShardingConditions shardingConditions) {
        this(shardingConditions, null);
    }
    
    public OptimizeResult(final InsertColumnValues insertColumnValues) {
        this(new ShardingConditions(Collections.<ShardingCondition>emptyList()), insertColumnValues);
    }
    
    /**
     * Get insert column values.
     * 
     * @return insert column values optional
     */
    public Optional<InsertColumnValues> getInsertColumnValues() {
        return null == insertColumnValues ? Optional.<InsertColumnValues>absent() : Optional.of(insertColumnValues);
    }
}
