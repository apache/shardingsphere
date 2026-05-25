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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Sharding condition engine for insert clause.
 */
@RequiredArgsConstructor
public final class InsertClauseShardingConditionEngine {
    
    private static final Set<String> NUMERIC_CAST_TARGETS = unmodifiableUpperCaseSet(
            "INT", "INTEGER", "INT2", "INT4", "INT8", "BIGINT", "SMALLINT", "TINYINT",
            "NUMERIC", "DECIMAL", "DEC", "NUMBER",
            "FLOAT", "FLOAT4", "FLOAT8", "REAL", "DOUBLE", "DOUBLE PRECISION",
            "SERIAL", "BIGSERIAL", "SMALLSERIAL");
    
    private static final Set<String> TEXT_CAST_TARGETS = unmodifiableUpperCaseSet(
            "TEXT", "VARCHAR", "CHARACTER VARYING", "CHAR", "CHARACTER", "BPCHAR", "NAME", "NVARCHAR", "NCHAR");
    
    private static final Set<String> BOOLEAN_CAST_TARGETS = unmodifiableUpperCaseSet("BOOL", "BOOLEAN");
    
    private static final Set<String> TEMPORAL_CAST_TARGETS = unmodifiableUpperCaseSet(
            "DATE", "TIME", "TIMETZ", "TIMESTAMP", "TIMESTAMPTZ", "TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP WITH TIME ZONE");
    
    private final ShardingSphereDatabase database;
    
    private final ShardingRule rule;
    
    private final TimestampServiceRule timestampServiceRule;
    
    private static Set<String> unmodifiableUpperCaseSet(final String... values) {
        Set<String> result = new HashSet<>(values.length, 1F);
        result.addAll(Arrays.asList(values));
        return Collections.unmodifiableSet(result);
    }
    
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
            ExpressionSegment value = unwrapTypeCastForRouting(each, params);
            if (value instanceof SimpleExpressionSegment) {
                List<Integer> parameterMarkerIndexes = value instanceof ParameterMarkerExpressionSegment
                        ? Collections.singletonList(((ParameterMarkerExpressionSegment) value).getParameterMarkerIndex())
                        : Collections.emptyList();
                Object shardingValue = getShardingValue((SimpleExpressionSegment) value, params);
                result.getValues().add(new ListShardingConditionValue<>(shardingColumn.get(), tableName, Collections.singletonList(shardingValue),
                        parameterMarkerIndexes));
            } else if (value instanceof CommonExpressionSegment) {
                generateShardingCondition((CommonExpressionSegment) value, result, shardingColumn.get(), tableName);
            } else if (ExpressionConditionUtils.isNowExpression(value)) {
                result.getValues().add(new ListShardingConditionValue<>(shardingColumn.get(), tableName, Collections.singletonList(timestampServiceRule.getTimestamp())));
            }
        }
        return result;
    }
    
    /**
     * Unwrap nested {@link TypeCastExpression} layers for routing only when the cast is semantically safe, i.e. the bound
     * Java value belongs to the same category as the outermost cast target type. An {@code expression::type} cast on a
     * sharding key then reuses the underlying parameter marker or literal for routing without coercion.
     *
     * <p>Unwrap is refused, returning the original cast (which falls through the {@code instanceof} chain and adds no
     * sharding condition), in any of the following cases:</p>
     * <ul>
     * <li>the innermost expression is not a {@link ParameterMarkerExpressionSegment} or {@link LiteralExpressionSegment}
     * (e.g. {@link org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment}),
     * which would otherwise hit a {@link ClassCastException} at {@code getShardingValue};</li>
     * <li>the bound Java value is {@code null};</li>
     * <li>the bound Java value's class does not match the outermost cast target category, e.g. binding a {@code String}
     * for {@code ?::int4}. PostgreSQL would evaluate the SQL value as an integer while raw-value routing would still see
     * the {@code String}, so refusing to unwrap avoids routing to the wrong shard.</li>
     * </ul>
     *
     * @param expressionSegment expression segment that may be wrapped in one or more {@link TypeCastExpression} layers
     * @param params bound parameter values used to look up the runtime Java type of a parameter marker
     * @return the innermost {@link ParameterMarkerExpressionSegment} or {@link LiteralExpressionSegment} when the cast is
     *         safe; otherwise the original argument
     */
    private static ExpressionSegment unwrapTypeCastForRouting(final ExpressionSegment expressionSegment, final List<Object> params) {
        if (!(expressionSegment instanceof TypeCastExpression)) {
            return expressionSegment;
        }
        TypeCastExpression outermost = (TypeCastExpression) expressionSegment;
        ExpressionSegment inner = outermost;
        while (inner instanceof TypeCastExpression) {
            inner = ((TypeCastExpression) inner).getExpression();
        }
        if (!(inner instanceof ParameterMarkerExpressionSegment) && !(inner instanceof LiteralExpressionSegment)) {
            return expressionSegment;
        }
        Object routingValue;
        if (inner instanceof ParameterMarkerExpressionSegment) {
            int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) inner).getParameterMarkerIndex();
            if (parameterMarkerIndex < 0 || parameterMarkerIndex >= params.size()) {
                return expressionSegment;
            }
            routingValue = params.get(parameterMarkerIndex);
        } else {
            routingValue = ((LiteralExpressionSegment) inner).getLiterals();
        }
        return isCastSafeForRouting(routingValue, outermost.getDataType()) ? inner : expressionSegment;
    }
    
    private static boolean isCastSafeForRouting(final Object value, final String castTargetType) {
        if (null == value || null == castTargetType) {
            return false;
        }
        String normalized = castTargetType.toUpperCase(Locale.ROOT).trim();
        int parenIndex = normalized.indexOf('(');
        if (parenIndex > 0) {
            normalized = normalized.substring(0, parenIndex).trim();
        }
        if (value instanceof Number) {
            return NUMERIC_CAST_TARGETS.contains(normalized);
        }
        if (value instanceof String) {
            return TEXT_CAST_TARGETS.contains(normalized);
        }
        if (value instanceof Boolean) {
            return BOOLEAN_CAST_TARGETS.contains(normalized);
        }
        if (value instanceof Date) {
            return TEMPORAL_CAST_TARGETS.contains(normalized);
        }
        return false;
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
