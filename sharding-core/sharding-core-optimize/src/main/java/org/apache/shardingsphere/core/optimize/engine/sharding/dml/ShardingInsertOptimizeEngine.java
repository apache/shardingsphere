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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.GeneratedKey;
import org.apache.shardingsphere.core.optimize.condition.RouteCondition;
import org.apache.shardingsphere.core.optimize.condition.RouteConditions;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine for sharding.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ShardingInsertOptimizeEngine implements OptimizeEngine {
    
    private final ShardingRule shardingRule;
    
    private final InsertStatement insertStatement;
    
    private final List<Object> parameters;
    
    @Override
    public OptimizeResult optimize() {
        InsertOptimizeResult insertOptimizeResult = new InsertOptimizeResult(insertStatement.getColumnNames());
        List<AndCondition> andConditions = insertStatement.getShardingConditions().getOrConditions();
        List<RouteCondition> routeConditions = new ArrayList<>(andConditions.size());
        Optional<GeneratedKey> generatedKey = GeneratedKey.getGenerateKey(shardingRule, parameters, insertStatement);
        boolean isGeneratedValue = generatedKey.isPresent() && generatedKey.get().isGenerated();
        Iterator<Comparable<?>> generatedValues = isGeneratedValue ? generatedKey.get().getGeneratedValues().iterator() : null;
        if (generatedKey.isPresent()) {
            appendGeneratedKeyColumn(generatedKey.get(), insertOptimizeResult);
        }
        appendAssistedQueryColumns(insertOptimizeResult);
        int parametersCount = 0;
        for (int i = 0; i < andConditions.size(); i++) {
            AndCondition andCondition = andConditions.get(i);
            InsertValue insertValue = insertStatement.getValues().get(i);
            ExpressionSegment[] eachInsertValues = createEachInsertValues(insertValue, isGeneratedValue);
            Object[] eachParameters = createEachParameters(insertValue, parametersCount, isGeneratedValue);
            InsertOptimizeResultUnit unit = insertOptimizeResult.addUnit(eachInsertValues, eachParameters, insertValue.getParametersCount());
            RouteCondition routeCondition = new RouteCondition();
            routeCondition.getRouteValues().addAll(getRouteValues(andCondition));
            if (isGeneratedValue) {
                fillGeneratedKeyUnit(generatedKey.get().getColumnName(), generatedValues.next(), unit, routeCondition);
            }
            if (shardingRule.getEncryptRule().getEncryptorEngine().isHasShardingQueryAssistedEncryptor(insertStatement.getTables().getSingleTableName())) {
                fillAssistedQueryUnit(insertOptimizeResult.getColumnNames(), unit);
            }
            routeConditions.add(routeCondition);
            parametersCount += insertValue.getParametersCount();
        }
        return generatedKey.isPresent()
                ? createOptimizeResult(insertOptimizeResult, routeConditions, generatedKey.get()) : new OptimizeResult(new RouteConditions(routeConditions), insertOptimizeResult);
    }
    
    private void appendGeneratedKeyColumn(final GeneratedKey generatedKey, final InsertOptimizeResult insertOptimizeResult) {
        if (generatedKey.isGenerated()) {
            insertOptimizeResult.getColumnNames().add(generatedKey.getColumnName());
        }
    }
    
    private void appendAssistedQueryColumns(final InsertOptimizeResult insertOptimizeResult) {
        for (String each : shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName())) {
            insertOptimizeResult.getColumnNames().add(each);
        }
    }
    
    private ExpressionSegment[] createEachInsertValues(final InsertValue insertValue, final boolean isGeneratedValue) {
        ExpressionSegment[] result = new ExpressionSegment[insertValue.getAssignments().size() + getDerivedColumnsCount(isGeneratedValue)];
        insertValue.getAssignments().toArray(result);
        return result;
    }
    
    private Object[] createEachParameters(final InsertValue insertValue, final int parametersBeginIndex, final boolean isGeneratedValue) {
        if (0 == insertValue.getParametersCount()) {
            return new Object[0];
        }
        Object[] result = new Object[insertValue.getParametersCount() + getDerivedColumnsCount(isGeneratedValue)];
        parameters.subList(parametersBeginIndex, parametersBeginIndex + insertValue.getParametersCount()).toArray(result);
        return result;
    }
    
    private int getDerivedColumnsCount(final boolean isGeneratedValue) {
        int assistedQueryColumnsCount = shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
        return isGeneratedValue ? assistedQueryColumnsCount + 1 : assistedQueryColumnsCount;
    }
    
    private Collection<ListRouteValue> getRouteValues(final AndCondition andCondition) {
        Collection<ListRouteValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListRouteValue<>(each.getColumn().getName(), each.getColumn().getTableName(), each.getConditionValues(parameters)));
        }
        return result;
    }
    
    private void fillGeneratedKeyUnit(final String generatedKeyColumnName, final Comparable<?> generatedValue, final InsertOptimizeResultUnit unit, final RouteCondition routeCondition) {
        fillUnit(generatedValue, unit);
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingRule.isShardingColumn(generatedKeyColumnName, tableName)) {
            routeCondition.getRouteValues().add(new ListRouteValue<>(generatedKeyColumnName, tableName, Collections.<Comparable<?>>singletonList(generatedValue)));
        }
    }
    
    private void fillAssistedQueryUnit(final Collection<String> columnNames, final InsertOptimizeResultUnit unit) {
        for (String each : columnNames) {
            if (shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumn(insertStatement.getTables().getSingleTableName(), each).isPresent()) {
                fillUnit((Comparable<?>) unit.getColumnValue(each), unit);
            }
        }
    }
    
    private void fillUnit(final Comparable<?> insertValue, final InsertOptimizeResultUnit unit) {
        if (parameters.isEmpty()) {
            // TODO fix start index and stop index
            unit.addColumnValue(new LiteralExpressionSegment(0, 0, insertValue));
        } else {
            // TODO fix start index and stop index
            unit.addColumnValue(new ParameterMarkerExpressionSegment(0, 0, parameters.size() - 1));
            unit.addColumnParameter(insertValue);
        }
    }
    
    private OptimizeResult createOptimizeResult(final InsertOptimizeResult insertOptimizeResult, final List<RouteCondition> routeConditions, final GeneratedKey generatedKey) {
        OptimizeResult result = new OptimizeResult(new RouteConditions(routeConditions), insertOptimizeResult);
        result.setGeneratedKey(generatedKey);
        return result;
    }
}
