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

package org.apache.shardingsphere.core.optimize.statement.sharding;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimize.statement.sharding.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.List;

/**
 * Optimized statement for insert clause.
 *
 * @author zhangliang
 */
public final class InsertClauseOptimizedStatement extends ShardingOptimizedStatement {
    
    @Getter
    private final InsertOptimizeResult insertOptimizeResult;
    
    @Setter
    private GeneratedKey generatedKey;
    
    public InsertClauseOptimizedStatement(final SQLStatement sqlStatement, final List<ShardingCondition> shardingConditions, final InsertOptimizeResult insertOptimizeResult) {
        super(sqlStatement, new ShardingConditions(shardingConditions));
        this.insertOptimizeResult = insertOptimizeResult;
    }
    
    /**
     * Get generated key.
     *
     * @return generated key
     */
    public Optional<GeneratedKey> getGeneratedKey() {
        return Optional.fromNullable(generatedKey);
    }
}
