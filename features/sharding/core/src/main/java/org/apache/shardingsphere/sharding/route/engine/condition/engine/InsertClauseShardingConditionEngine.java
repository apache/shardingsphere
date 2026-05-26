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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding condition engine for insert clause.
 */
@RequiredArgsConstructor
public final class InsertClauseShardingConditionEngine {
    
    private final ShardingSphereDatabase database;
    
    private final ShardingRule rule;
    
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
        String tableName = sqlStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue())
                .orElseGet(() -> sqlStatementContext.getTablesContext().getTableNames().iterator().next());
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
        String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        IdentifierValue tableIdentifier = sqlStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier())
                .orElseGet(() -> new IdentifierValue(sqlStatementContext.getTablesContext().getTableNames().iterator().next()));
        String tableName = tableIdentifier.getValue();
        ShardingSpherePreconditions.checkState(schema.containsTable(tableIdentifier), () -> new NoSuchTableException(tableName));
        for (String each : schema.getTable(tableIdentifier).findColumnNamesIfNotExistedFrom(columnNames)) {
            if (!rule.isGenerateKeyColumn(each, tableName) && rule.findShardingColumn(each, tableName).isPresent()) {
                appendMissingShardingConditions(tableName, each, shardingConditions);
            }
        }
    }
    
    private void appendMissingShardingConditions(final String tableName, final String columnName, final List<ShardingCondition> shardingConditions) {
        for (ShardingCondition each : shardingConditions) {
            each.getValues().add(new ListShardingConditionValue<>(columnName, tableName, Collections.singletonList(null)));
        }
    }
    
    private Collection<String> getColumnNames(final InsertStatementContext insertStatementContext) {
        Optional<GeneratedKeyContext> generatedKey = insertStatementContext.getGeneratedKeyContext();
        List<String> columnNames = insertStatementContext.getColumnNames();
        if (generatedKey.isPresent() && generatedKey.get().isGenerated()) {
            Collection<String> result = new LinkedHashSet<>(columnNames);
            result.remove(generatedKey.get().getColumnName());
            return result;
        }
        return new LinkedHashSet<>(columnNames);
    }
    
    private ShardingCondition createShardingCondition(final String tableName, final Iterator<String> columnNames,
                                                      final InsertValueContext insertValueContext, final List<Object> params, final int rowNumber) {
        ShardingCondition result = new ShardingCondition();
        for (ExpressionSegment each : insertValueContext.getValueExpressions()) {
            if (!columnNames.hasNext()) {
                throw new InsertColumnsAndValuesMismatchedException(rowNumber);
            }
            Optional<String> shardingColumn = rule.findShardingColumn(columnNames.next(), tableName);
            if (!shardingColumn.isPresent()) {
                continue;
            }
            appendCastRoutedValueIfPresent(each, params, shardingColumn.get(), tableName, result)
                    .orElseGet(() -> appendRoutedValueWithoutCast(each, params, shardingColumn.get(), tableName, result));
        }
        return result;
    }
    
    /**
     * Route a {@code TypeCastExpression} sharding-key value by the database-visible cast result instead of the raw bound
     * Java value, so that PostgreSQL/openGauss runtime semantics for {@code expression::type} (string-to-integer,
     * lossy-numeric-to-integer, boolean conversions, etc.) are preserved by routing.
     *
     * <p>Returns {@link Optional#empty()} when the segment is not a cast or when {@link PostgreSQLCastEvaluator} cannot
     * resolve the cast (unsupported target, parse failure, overflow, or a non-marker / non-literal inner expression such
     * as a {@code SubqueryExpressionSegment}). In that case the caller falls back to the no-cast path.</p>
     *
     * @param expressionSegment expression segment to route
     * @param params bound parameter values for parameter markers
     * @param shardingColumn sharding column name to attach the routed value to
     * @param tableName sharding table name to attach the routed value to
     * @param condition sharding condition to append the routed value to
     * @return {@link Optional#of(Object)} with a non-null placeholder when a cast-derived sharding condition was
     *         appended, otherwise {@link Optional#empty()}
     */
    private Optional<Object> appendCastRoutedValueIfPresent(final ExpressionSegment expressionSegment, final List<Object> params,
                                                            final String shardingColumn, final String tableName, final ShardingCondition condition) {
        if (!(expressionSegment instanceof TypeCastExpression)) {
            return Optional.empty();
        }
        List<String> castTargetTypesOuterToInner = new ArrayList<>();
        ExpressionSegment innermost = expressionSegment;
        while (innermost instanceof TypeCastExpression) {
            castTargetTypesOuterToInner.add(((TypeCastExpression) innermost).getDataType());
            innermost = ((TypeCastExpression) innermost).getExpression();
        }
        if (!(innermost instanceof ParameterMarkerExpressionSegment) && !(innermost instanceof LiteralExpressionSegment)) {
            return Optional.empty();
        }
        Object value;
        List<Integer> parameterMarkerIndexes;
        if (innermost instanceof ParameterMarkerExpressionSegment) {
            int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) innermost).getParameterMarkerIndex();
            if (parameterMarkerIndex < 0 || parameterMarkerIndex >= params.size()) {
                return Optional.empty();
            }
            value = params.get(parameterMarkerIndex);
            parameterMarkerIndexes = Collections.singletonList(parameterMarkerIndex);
        } else {
            value = ((LiteralExpressionSegment) innermost).getLiterals();
            parameterMarkerIndexes = Collections.emptyList();
        }
        for (int index = castTargetTypesOuterToInner.size() - 1; index >= 0; index--) {
            Optional<Comparable<?>> casted = PostgreSQLCastEvaluator.evaluate(value, castTargetTypesOuterToInner.get(index));
            if (!casted.isPresent()) {
                return Optional.empty();
            }
            value = casted.get();
        }
        if (!(value instanceof Comparable)) {
            return Optional.empty();
        }
        condition.getValues().add(new ListShardingConditionValue<>(shardingColumn, tableName, Collections.singletonList((Comparable<?>) value), parameterMarkerIndexes));
        return Optional.of(Boolean.TRUE);
    }
    
    private Object appendRoutedValueWithoutCast(final ExpressionSegment expressionSegment, final List<Object> params,
                                                final String shardingColumn, final String tableName, final ShardingCondition condition) {
        if (expressionSegment instanceof SimpleExpressionSegment) {
            List<Integer> parameterMarkerIndexes = expressionSegment instanceof ParameterMarkerExpressionSegment
                    ? Collections.singletonList(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex())
                    : Collections.emptyList();
            Object shardingValue = getShardingValue((SimpleExpressionSegment) expressionSegment, params);
            condition.getValues().add(new ListShardingConditionValue<>(shardingColumn, tableName, Collections.singletonList(shardingValue), parameterMarkerIndexes));
        } else if (expressionSegment instanceof CommonExpressionSegment) {
            generateShardingCondition((CommonExpressionSegment) expressionSegment, condition, shardingColumn, tableName);
        } else if (ExpressionConditionUtils.isNowExpression(expressionSegment)) {
            condition.getValues().add(new ListShardingConditionValue<>(shardingColumn, tableName, Collections.singletonList(timestampServiceRule.getTimestamp())));
        }
        return Boolean.TRUE;
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
        return new LinkedList<>(new WhereClauseShardingConditionEngine(database, rule, timestampServiceRule).createShardingConditions(selectStatementContext, params));
    }
    
    private void appendGeneratedKeyConditions(final InsertStatementContext sqlStatementContext, final List<ShardingCondition> shardingConditions) {
        Optional<GeneratedKeyContext> generatedKey = sqlStatementContext.getGeneratedKeyContext();
        String tableName = sqlStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue()).orElse("");
        if (generatedKey.isPresent() && generatedKey.get().isGenerated() && rule.findShardingTable(tableName).isPresent()) {
            String schemaName = sqlStatementContext.getTablesContext().getSchemaName()
                    .orElseGet(() -> new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName()));
            AlgorithmSQLContext algorithmSQLContext = new AlgorithmSQLContext(database.getName(), schemaName, tableName, generatedKey.get().getColumnName());
            generatedKey.get().getGeneratedValues().addAll(rule.generateKeys(algorithmSQLContext, sqlStatementContext.getValueListCount()));
            generatedKey.get().setSupportAutoIncrement(rule.isSupportAutoIncrement(tableName));
            if (rule.findShardingColumn(generatedKey.get().getColumnName(), tableName).isPresent()) {
                appendGeneratedKeyCondition(generatedKey.get(), tableName, shardingConditions);
            }
        }
    }
    
    private void appendGeneratedKeyCondition(final GeneratedKeyContext generatedKey, final String tableName, final List<ShardingCondition> shardingConditions) {
        Iterator<Comparable<?>> generatedValuesIterator = generatedKey.getGeneratedValues().iterator();
        for (ShardingCondition each : shardingConditions) {
            each.getValues().add(new ListShardingConditionValue<>(generatedKey.getColumnName(), tableName, Collections.<Comparable<?>>singletonList(generatedValuesIterator.next())));
        }
    }
}
