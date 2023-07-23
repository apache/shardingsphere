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

import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.exception.data.ShardingValueDataTypeException;
import org.apache.shardingsphere.sharding.route.engine.condition.AlwaysFalseShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGeneratorFactory;
import org.apache.shardingsphere.sharding.route.engine.condition.value.AlwaysFalseShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Sharding condition engine for where clause.
 */
@RequiredArgsConstructor
public final class WhereClauseShardingConditionEngine {
    
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
    public List<ShardingCondition> createShardingConditions(final SQLStatementContext sqlStatementContext, final List<Object> params) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        Collection<ColumnSegment> columnSegments = ((WhereAvailable) sqlStatementContext).getColumnSegments();
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), database.getName());
        ShardingSphereSchema schema = sqlStatementContext.getTablesContext().getSchemaName()
                .map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchemaName));
        Map<String, String> columnExpressionTableNames = sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(columnSegments, schema);
        List<ShardingCondition> result = new ArrayList<>();
        for (WhereSegment each : ((WhereAvailable) sqlStatementContext).getWhereSegments()) {
            result.addAll(createShardingConditions(each.getExpr(), params, columnExpressionTableNames));
        }
        return result;
    }
    
    private Collection<ShardingCondition> createShardingConditions(final ExpressionSegment expression, final List<Object> params, final Map<String, String> columnExpressionTableNames) {
        Collection<AndPredicate> andPredicates = ExpressionExtractUtils.getAndPredicates(expression);
        Collection<ShardingCondition> result = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            Map<Column, Collection<ShardingConditionValue>> shardingConditionValues = createShardingConditionValueMap(each.getPredicates(), params, columnExpressionTableNames);
            if (shardingConditionValues.isEmpty()) {
                return Collections.emptyList();
            }
            ShardingCondition shardingCondition = createShardingCondition(shardingConditionValues);
            // TODO remove startIndex when federation has perfect support for subquery
            shardingCondition.setStartIndex(expression.getStartIndex());
            result.add(shardingCondition);
        }
        return result;
    }
    
    private Map<Column, Collection<ShardingConditionValue>> createShardingConditionValueMap(final Collection<ExpressionSegment> predicates,
                                                                                            final List<Object> params, final Map<String, String> columnTableNames) {
        Map<Column, Collection<ShardingConditionValue>> result = new HashMap<>(predicates.size(), 1F);
        for (ExpressionSegment each : predicates) {
            for (ColumnSegment columnSegment : ColumnExtractor.extract(each)) {
                Optional<String> tableName = Optional.ofNullable(columnTableNames.get(columnSegment.getExpression()));
                Optional<String> shardingColumn = tableName.flatMap(optional -> shardingRule.findShardingColumn(columnSegment.getIdentifier().getValue(), optional));
                if (!tableName.isPresent() || !shardingColumn.isPresent()) {
                    continue;
                }
                Column column = new Column(shardingColumn.get(), tableName.get());
                Optional<ShardingConditionValue> shardingConditionValue = ConditionValueGeneratorFactory.generate(each, column, params, timestampServiceRule);
                if (!shardingConditionValue.isPresent()) {
                    continue;
                }
                result.computeIfAbsent(column, unused -> new LinkedList<>()).add(shardingConditionValue.get());
            }
        }
        return result;
    }
    
    private ShardingCondition createShardingCondition(final Map<Column, Collection<ShardingConditionValue>> shardingConditionValues) {
        ShardingCondition result = new ShardingCondition();
        for (Entry<Column, Collection<ShardingConditionValue>> entry : shardingConditionValues.entrySet()) {
            try {
                ShardingConditionValue shardingConditionValue = mergeShardingConditionValues(entry.getKey(), entry.getValue());
                if (shardingConditionValue instanceof AlwaysFalseShardingConditionValue) {
                    return new AlwaysFalseShardingCondition();
                }
                result.getValues().add(shardingConditionValue);
            } catch (final ClassCastException ignored) {
                throw new ShardingValueDataTypeException(entry.getKey());
            }
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ShardingConditionValue mergeShardingConditionValues(final Column column, final Collection<ShardingConditionValue> shardingConditionValues) {
        Collection<Comparable<?>> listValue = null;
        Range<Comparable<?>> rangeValue = null;
        Set<Integer> parameterMarkerIndexes = new HashSet<>();
        for (ShardingConditionValue each : shardingConditionValues) {
            parameterMarkerIndexes.addAll(each.getParameterMarkerIndexes());
            if (each instanceof ListShardingConditionValue) {
                listValue = mergeListShardingValues(((ListShardingConditionValue) each).getValues(), listValue);
                if (listValue.isEmpty()) {
                    return new AlwaysFalseShardingConditionValue();
                }
            } else if (each instanceof RangeShardingConditionValue) {
                try {
                    rangeValue = mergeRangeShardingValues(((RangeShardingConditionValue) each).getValueRange(), rangeValue);
                } catch (final IllegalArgumentException ex) {
                    return new AlwaysFalseShardingConditionValue();
                }
            }
        }
        if (null == listValue) {
            return new RangeShardingConditionValue<>(column.getName(), column.getTableName(), rangeValue, new ArrayList<>(parameterMarkerIndexes));
        }
        if (null == rangeValue) {
            return new ListShardingConditionValue<>(column.getName(), column.getTableName(), listValue, new ArrayList<>(parameterMarkerIndexes));
        }
        listValue = mergeListAndRangeShardingValues(listValue, rangeValue);
        return listValue.isEmpty() ? new AlwaysFalseShardingConditionValue()
                : new ListShardingConditionValue<>(column.getName(), column.getTableName(), listValue, new ArrayList<>(parameterMarkerIndexes));
    }
    
    private Collection<Comparable<?>> mergeListShardingValues(final Collection<Comparable<?>> value1, final Collection<Comparable<?>> value2) {
        if (null == value2) {
            return value1;
        }
        value1.retainAll(value2);
        return value1;
    }
    
    private Range<Comparable<?>> mergeRangeShardingValues(final Range<Comparable<?>> value1, final Range<Comparable<?>> value2) {
        return null == value2 ? value1 : SafeNumberOperationUtils.safeIntersection(value1, value2);
    }
    
    private Collection<Comparable<?>> mergeListAndRangeShardingValues(final Collection<Comparable<?>> listValue, final Range<Comparable<?>> rangeValue) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (SafeNumberOperationUtils.safeContains(rangeValue, each)) {
                result.add(each);
            }
        }
        return result;
    }
}
