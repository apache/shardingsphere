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

package org.apache.shardingsphere.core.optimize.statement.dml.insert;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.dml.DMLOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Optimized statement for insert.
 *
 * @author zhangliang
 */
@Getter
public final class InsertOptimizedStatement extends DMLOptimizedStatement {
    
    private final Collection<String> columnNames = new LinkedHashSet<>();
    
    private final List<InsertOptimizeResultUnit> units = new LinkedList<>();
    
    @Setter
    private GeneratedKey generatedKey;
    
    public InsertOptimizedStatement(final SQLStatement sqlStatement, final List<ShardingCondition> shardingConditions, final Collection<String> columnNames) {
        super(sqlStatement, new ShardingConditions(shardingConditions));
        this.columnNames.addAll(columnNames);
    }
    
    /**
     * Add insert optimize result uint.
     *
     * @param insertValues insert values
     * @param parameters SQL parameters
     * @param startIndexOfAppendedParameters start index of appended parameters
     * @return insert optimize result unit
     */
    public InsertOptimizeResultUnit addUnit(final ExpressionSegment[] insertValues, final Object[] parameters, final int startIndexOfAppendedParameters) {
        InsertOptimizeResultUnit result = new InsertOptimizeResultUnit(columnNames, insertValues, parameters, startIndexOfAppendedParameters);
        units.add(result);
        return result;
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
