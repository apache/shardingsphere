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

package org.apache.shardingsphere.core.optimize.sharding.statement.dml;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collections;
import java.util.List;

/**
 * Insert optimized statement for sharding.
 *
 * @author zhangliang
 */
@Getter
@ToString(callSuper = true)
public final class ShardingInsertOptimizedStatement extends ShardingConditionOptimizedStatement implements InsertOptimizedStatement {
    
    private final Tables tables;
    
    private final List<String> columnNames;
    
    private final GeneratedKey generatedKey;
    
    private final List<InsertValue> insertValues;
    
    public ShardingInsertOptimizedStatement(final SQLStatement sqlStatement, final List<ShardingCondition> shardingConditions, 
                                            final List<String> columnNames, final GeneratedKey generatedKey, final List<InsertValue> insertValues) {
        super(sqlStatement, new ShardingConditions(shardingConditions), new EncryptConditions(Collections.<EncryptCondition>emptyList()));
        tables = new Tables(sqlStatement);
        this.columnNames = columnNames;
        this.generatedKey = generatedKey;
        this.insertValues = insertValues;
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
