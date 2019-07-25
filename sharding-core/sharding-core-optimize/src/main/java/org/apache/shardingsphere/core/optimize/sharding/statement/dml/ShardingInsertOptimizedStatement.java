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
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.GeneratedKey;
import org.apache.shardingsphere.core.optimize.sharding.segment.insert.InsertOptimizeResultUnit;
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
    
    private final Collection<InsertValue> values;
    
    private final GeneratedKey generatedKey;
    
    private final List<InsertOptimizeResultUnit> units = new LinkedList<>();
    
    public ShardingInsertOptimizedStatement(final SQLStatement sqlStatement, final List<ShardingCondition> shardingConditions, 
                                            final ShardingInsertColumns insertColumns, final Collection<InsertValue> values, final GeneratedKey generatedKey) {
        super(sqlStatement, new ShardingConditions(shardingConditions), new EncryptConditions(Collections.<EncryptCondition>emptyList()));
        tables = new Tables(sqlStatement);
        this.insertColumns = insertColumns;
        this.values = values;
        this.generatedKey = generatedKey;
    }

    /**
     * Create insert optimize unit.
     *
     * @param insertValues insert values
     * @param parameters SQL parameters
     * @param startIndexOfAppendedParameters start index of appended parameters
     * @return insert optimize result unit
     */
    public InsertOptimizeResultUnit createUnit(final ExpressionSegment[] insertValues, final Object[] parameters, final int startIndexOfAppendedParameters) {
        InsertOptimizeResultUnit result = new InsertOptimizeResultUnit(insertColumns.getAllColumnNames(), insertValues, parameters, startIndexOfAppendedParameters);
        return result;
    }

    /**
     * Add insert optimize result unit into units.
     *
     * @param  insertOptimizeResultUnit one insertOptimizeResultUnit object
     */
    public void addUnit(final InsertOptimizeResultUnit insertOptimizeResultUnit) {
        units.add(insertOptimizeResultUnit);
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
