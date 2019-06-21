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

package org.apache.shardingsphere.core.optimize.result;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimize.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;

import java.util.Collections;
import java.util.List;

/**
 * Optimize result.
 *
 * @author panjuan
 */
@Getter
@Setter
public final class OptimizeResult {
    
    private ShardingConditions shardingConditions;
    
    @Getter(AccessLevel.NONE)
    private final InsertOptimizeResult insertOptimizeResult;
    
    private Pagination pagination;
    
    private GeneratedKey generatedKey;
    
    private String logicTableNameForDropIndex;
    
    public OptimizeResult(final List<ShardingCondition> shardingConditions) {
        this(new ShardingConditions(shardingConditions), null);
    }
    
    public OptimizeResult(final InsertOptimizeResult insertOptimizeResult) {
        this(new ShardingConditions(Collections.<ShardingCondition>emptyList()), insertOptimizeResult);
    }
    
    public OptimizeResult(final ShardingConditions shardingConditions, final InsertOptimizeResult insertOptimizeResult) {
        this.shardingConditions = shardingConditions;
        this.insertOptimizeResult = insertOptimizeResult;
    }
    
    /**
     * Get insert optimize result.
     * 
     * @return insert optimize result
     */
    public Optional<InsertOptimizeResult> getInsertOptimizeResult() {
        return Optional.fromNullable(insertOptimizeResult);
    }
    
    /**
     * Get generated key.
     * 
     * @return generated key
     */
    public Optional<GeneratedKey> getGeneratedKey() {
        return Optional.fromNullable(generatedKey);
    }
    
    /**
     * Get logic table name for drop index.
     * 
     * @return logic table name for drop index
     */
    public Optional<String> getLogicTableNameForDropIndex() {
        return Optional.fromNullable(logicTableNameForDropIndex);
    }
}
