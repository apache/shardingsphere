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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Sharding condition engine for insert clause.
 */
@RequiredArgsConstructor
public final class InsertClauseShardingConditionEngine {
    
    private final ShardingSphereDatabase database;
    
    private final ShardingRule shardingRule;
    
    private final TimestampServiceRule timestampServiceRule;
    
    /**
     * Create sharding conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @return sharding conditions
     */
    public List<ShardingCondition> createShardingConditions(final InsertStatementContext sqlStatementContext, final List<Object> params) {
        List<ShardingCondition> result = null == sqlStatementContext.getInsertSelectContext()
                ? createShardingConditionsWithInsertValues(sqlStatementContext, params)
                : createShardingConditionsWithInsertSelect(sqlStatementContext, params);
        appendGeneratedKeyConditions(sqlStatementContext, result);
        return result;
    }
    
    private List<ShardingCondition> createShardingConditionsWithInsertValues(final InsertStatementContext sqlStatementContext, final List<Object> params) {
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Collection<String> columnNames = getColumnNames(sqlStatementContext);
        List<InsertValueContext> insertValueContexts = sqlStatementContext.getInsertValueContexts();
        List<ShardingCondition> result = new ArrayList<>(insertValueContexts.size());
        int rowNumber = 0;
        for (InsertValueContext each : insertValueContexts) {
            result.add(createShardingCondition(tableName, columnNames.iterator(), each, params, ++rowNumber));
        }
        appendMissingShardingConditions(sqlStatementContext, columnNames, result);
        return result;
    }
    
    private void appendMissingShardingConditions(final InsertStatementContext sqlStatementContext, final Collection<String> columnNames, final List<ShardingCondition> shardingConditions) {
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName());
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new NoSuchTableException(tableName));
        Collection<String> allColumnNames = schema.getTable(tableName).getColumnNames();
        if (columnNames.size() == allColumnNames.size()) {
            return;
        }
        for (String each : allColumnNames) {
            if (!columnNames.contains(each) && !shardingRule.isGenerateKeyColumn(each, tableName) && shardingRule.findShardingColumn(each, tableName).isPresent()) {
                appendMissingShardingConditions(shardingConditions, each, tableName);
            }
        }
    }
    
    private void appendMissingShardingConditions(final List<ShardingCondition> shardingConditions, final String columnName, final String tableName) {
        for (ShardingCondition each : shardingConditions) {
            each.getValues().add(new ListShardingConditionValue<>(columnName, tableName, Collections.singletonList(null)));
        }
    }
    
    private Collection<String> getColumnNames(final InsertStatementContext insertStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = insertStatementContext.getGeneratedKeyContext();
        if (generatedKey.isPresent() && generatedKey.get().isGenerated()) {
            Collection<String> result = new LinkedHashSet<>(insertStatementContext.getColumnNames());
            result.remove(generatedKey.get().getColumnName());
            return result;
        }
        return new LinkedHashSet<>(insertStatementContext.getColumnNames());
    }
    
    private ShardingCondition createShardingCondition(final String tableName, final Iterator<String> columnNames,
                                                      final InsertValueContext insertValueContext, final List<Object> params, final int rowNumber) {
        ShardingCondition result = new ShardingCondition();
        for (ExpressionSegment each : insertValueContext.getValueExpressions()) {
            if (!columnNames.hasNext()) {
                throw new InsertColumnsAndValuesMismatchedException(rowNumber);
            }
            Optional<String> shardingColumn = shardingRule.findShardingColumn(columnNames.next(), tableName);
            if (!shardingColumn.isPresent()) {
                continue;
            }
            if (each instanceof SimpleExpressionSegment) {
                List<Integer> parameterMarkerIndexes = each instanceof ParameterMarkerExpressionSegment
                        ? Collections.singletonList(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex())
                        : Collections.emptyList();
                result.getValues().add(new ListShardingConditionValue<>(shardingColumn.get(), tableName, Collections.singletonList(getShardingValue((SimpleExpressionSegment) each, params)),
                        parameterMarkerIndexes));
            } else if (each instanceof CommonExpressionSegment) {
                generateShardingCondition((CommonExpressionSegment) each, result, shardingColumn.get(), tableName);
            } else if (ExpressionConditionUtils.isNowExpression(each)) {
                result.getValues().add(new ListShardingConditionValue<>(shardingColumn.get(), tableName, Collections.singletonList(timestampServiceRule.getTimestamp())));
            }
        }
        return result;
    }
    
    private void generateShardingCondition(final CommonExpressionSegment expressionSegment, final ShardingCondition condition, final String shardingColumn, final String tableName) {
        try {
            Integer value = Integer.valueOf(expressionSegment.getText());
            condition.getValues().add(new ListShardingConditionValue<>(shardingColumn, tableName, Collections.singletonList(value)));
        } catch (final NumberFormatException ex) {
            condition.getValues().add(new ListShardingConditionValue<>(shardingColumn, tableName, Collections.singletonList(expressionSegment.getText())));
        }
    }
    
    private Object getShardingValue(final SimpleExpressionSegment expressionSegment, final List<Object> params) {
        return expressionSegment instanceof ParameterMarkerExpressionSegment
                ? params.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex())
                : ((LiteralExpressionSegment) expressionSegment).getLiterals();
    }
    
    private List<ShardingCondition> createShardingConditionsWithInsertSelect(final InsertStatementContext sqlStatementContext, final List<Object> params) {
        SelectStatementContext selectStatementContext = sqlStatementContext.getInsertSelectContext().getSelectStatementContext();
        return new LinkedList<>(new WhereClauseShardingConditionEngine(database, shardingRule, timestampServiceRule).createShardingConditions(selectStatementContext, params));
    }
    
    private void appendGeneratedKeyConditions(final InsertStatementContext sqlStatementContext, final List<ShardingCondition> shardingConditions) {
        Optional<GeneratedKeyContext> generatedKey = sqlStatementContext.getGeneratedKeyContext();
        String tableName = sqlStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        if (generatedKey.isPresent() && generatedKey.get().isGenerated() && shardingRule.findTableRule(tableName).isPresent()) {
            generatedKey.get().getGeneratedValues().addAll(generateKeys(tableName, sqlStatementContext.getValueListCount()));
            generatedKey.get().setSupportAutoIncrement(shardingRule.isSupportAutoIncrement(tableName));
            if (shardingRule.findShardingColumn(generatedKey.get().getColumnName(), tableName).isPresent()) {
                appendGeneratedKeyCondition(generatedKey.get(), tableName, shardingConditions);
            }
        }
    }
    
    private Collection<Comparable<?>> generateKeys(final String tableName, final int valueListCount) {
        return IntStream.range(0, valueListCount).mapToObj(each -> shardingRule.generateKey(tableName)).collect(Collectors.toList());
    }
    
    private void appendGeneratedKeyCondition(final GeneratedKeyContext generatedKey, final String tableName, final List<ShardingCondition> shardingConditions) {
        Iterator<Comparable<?>> generatedValuesIterator = generatedKey.getGeneratedValues().iterator();
        for (ShardingCondition each : shardingConditions) {
            each.getValues().add(new ListShardingConditionValue<>(generatedKey.getColumnName(), tableName, Collections.<Comparable<?>>singletonList(generatedValuesIterator.next())));
        }
    }
}
