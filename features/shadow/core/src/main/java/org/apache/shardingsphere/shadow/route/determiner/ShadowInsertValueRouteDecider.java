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

package org.apache.shardingsphere.shadow.route.determiner;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.exception.syntax.UnsupportedShadowInsertValueException;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Shadow insert value route decider.
 */
@HighFrequencyInvocation
public final class ShadowInsertValueRouteDecider {
    
    private final ShadowRule rule;
    
    private final InsertStatementContext sqlStatementContext;
    
    private final String tableName;
    
    public ShadowInsertValueRouteDecider(final ShadowRule rule, final InsertStatementContext sqlStatementContext) {
        this.rule = rule;
        this.sqlStatementContext = sqlStatementContext;
        tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
    }
    
    /**
     * Decide shadow routing for every insert value.
     *
     * @return shadow routing flags ordered by insert value
     */
    public List<Boolean> decide() {
        List<Boolean> result = new LinkedList<>();
        for (int i = 0; i < sqlStatementContext.getInsertValueContexts().size(); i++) {
            result.add(isShadow(i));
        }
        return result;
    }
    
    private boolean isShadow(final int insertValueIndex) {
        for (String each : rule.getShadowColumnNames(ShadowOperationType.INSERT, tableName)) {
            int columnIndex = sqlStatementContext.getInsertColumnNames().indexOf(each);
            if (columnIndex < 0) {
                continue;
            }
            Comparable<?> columnValue = getColumnValue(insertValueIndex, columnIndex);
            for (ColumnShadowAlgorithm<Comparable<?>> algorithm : rule.getColumnShadowAlgorithms(ShadowOperationType.INSERT, tableName, each)) {
                if (ColumnShadowAlgorithmDeterminer.isShadow(algorithm,
                        new ShadowCondition(tableName, ShadowOperationType.INSERT, new ShadowColumnCondition(tableName, each, Collections.singletonList(columnValue))))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private Comparable<?> getColumnValue(final int insertValueIndex, final int columnIndex) {
        Object result = sqlStatementContext.getInsertValueContexts().get(insertValueIndex).getLiteralValue(columnIndex)
                .orElseThrow(() -> new UnsupportedShadowInsertValueException(columnIndex));
        ShardingSpherePreconditions.checkState(result instanceof Comparable<?>, () -> new UnsupportedShadowInsertValueException(columnIndex));
        return (Comparable<?>) result;
    }
}
