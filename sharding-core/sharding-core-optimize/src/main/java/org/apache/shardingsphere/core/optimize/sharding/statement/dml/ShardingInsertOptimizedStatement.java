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
import org.apache.shardingsphere.core.optimize.api.segment.OptimizedInsertValue;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.ShardingInsertColumns;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
    
    private final ShardingInsertColumns insertColumns;
    
    private final GeneratedKey generatedKey;
    
    private final List<OptimizedInsertValue> optimizedInsertValues = new LinkedList<>();
    
    public ShardingInsertOptimizedStatement(final SQLStatement sqlStatement, 
                                            final List<ShardingCondition> shardingConditions, final ShardingInsertColumns insertColumns, final GeneratedKey generatedKey) {
        super(sqlStatement, new ShardingConditions(shardingConditions), new EncryptConditions(Collections.<EncryptCondition>emptyList()));
        tables = new Tables(sqlStatement);
        this.insertColumns = insertColumns;
        this.generatedKey = generatedKey;
    }
    
    /**
     * Create optimized insert value.
     *
     * @param generateKeyColumnName generate key column name
     * @param derivedColumnNames derived column names
     * @param valueExpressions value expressions
     * @param parameters SQL parameters
     * @param startIndexOfAppendedParameters start index of appended parameters
     * @return optimized insert value
     */
    public OptimizedInsertValue createOptimizedInsertValue(final String generateKeyColumnName, final Collection<String> derivedColumnNames,
                                                           final ExpressionSegment[] valueExpressions, final Object[] parameters, final int startIndexOfAppendedParameters) {
        List<String> allColumnNames = new LinkedList<>(insertColumns.getRegularColumnNames());
        allColumnNames.add(generateKeyColumnName);
        allColumnNames.addAll(derivedColumnNames);
        return new OptimizedInsertValue(allColumnNames, valueExpressions, parameters, startIndexOfAppendedParameters);
    }
    
    /**
     * Add optimized insert value.
     *
     * @param  optimizedInsertValue optimized insert value
     */
    public void addOptimizedInsertValue(final OptimizedInsertValue optimizedInsertValue) {
        optimizedInsertValues.add(optimizedInsertValue);
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
