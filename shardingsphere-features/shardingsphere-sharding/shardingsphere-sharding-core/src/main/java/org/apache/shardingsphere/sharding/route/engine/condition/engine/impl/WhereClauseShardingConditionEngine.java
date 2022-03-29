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

package org.apache.shardingsphere.sharding.route.engine.condition.engine.impl;

import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.condition.AlwaysFalseShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
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
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Sharding condition engine for where clause.
 */
@RequiredArgsConstructor
public final class WhereClauseShardingConditionEngine implements ShardingConditionEngine<SQLStatementContext<?>> {
    
    private final ShardingRule shardingRule;
    
    private final ShardingSphereSchema schema;
    
    @Override
    public List<ShardingCondition> createShardingConditions(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        Collection<ColumnSegment> columnSegments = ((WhereAvailable) sqlStatementContext).getColumnSegments();
        Map<String, String> columnExpressionTableNames = sqlStatementContext.getTablesContext().findTableNamesByColumnSegment(columnSegments, schema);
        List<ShardingCondition> result = new ArrayList<>();
        for (WhereSegment each : ((WhereAvailable) sqlStatementContext).getWhereSegments()) {
            result.addAll(createShardingConditions(each.getExpr(), parameters, columnExpressionTableNames));
        }
        return result;
    }
    
    private Collection<ShardingCondition> createShardingConditions(final ExpressionSegment expression, final List<Object> parameters, final Map<String, String> columnExpressionTableNames) {
        Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(expression);
        Collection<ShardingCondition> result = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            Map<Column, Collection<ShardingConditionValue>> shardingConditionValues = createShardingConditionValueMap(each.getPredicates(), parameters, columnExpressionTableNames);
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
                                                                                            final List<Object> parameters, final Map<String, String> columnTableNames) {
        Map<Column, Collection<ShardingConditionValue>> result = new HashMap<>(predicates.size(), 1);
        for (ExpressionSegment each : predicates) {
            for (ColumnSegment columnSegment : ColumnExtractor.extract(each)) {
                Optional<String> tableName = Optional.ofNullable(columnTableNames.get(columnSegment.getExpression()));
                Optional<String> shardingColumn = tableName.flatMap(optional -> shardingRule.findShardingColumn(columnSegment.getIdentifier().getValue(), optional));
                if (!tableName.isPresent() || !shardingColumn.isPresent()) {
                    continue;
                }
                Column column = new Column(shardingColumn.get(), tableName.get());
                Optional<ShardingConditionValue> shardingConditionValue = ConditionValueGeneratorFactory.generate(each, column, parameters);
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
            } catch (final ClassCastException ex) {
                throw new ShardingSphereException("Found different types for sharding value `%s`.", entry.getKey());
            }
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ShardingConditionValue mergeShardingConditionValues(final Column column, final Collection<ShardingConditionValue> shardingConditionValues) {
        Collection<Comparable<?>> listValue = null;
        Range<Comparable<?>> rangeValue = null;
        for (ShardingConditionValue each : shardingConditionValues) {
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
            return new RangeShardingConditionValue<>(column.getName(), column.getTableName(), rangeValue);
        }
        if (null == rangeValue) {
            return new ListShardingConditionValue<>(column.getName(), column.getTableName(), listValue);
        }
        listValue = mergeListAndRangeShardingValues(listValue, rangeValue);
        return listValue.isEmpty() ? new AlwaysFalseShardingConditionValue() : new ListShardingConditionValue<>(column.getName(), column.getTableName(), listValue);
    }
    
    private Collection<Comparable<?>> mergeListShardingValues(final Collection<Comparable<?>> value1, final Collection<Comparable<?>> value2) {
        if (null == value2) {
            return value1;
        }
        value1.retainAll(value2);
        return value1;
    }
    
    private Range<Comparable<?>> mergeRangeShardingValues(final Range<Comparable<?>> value1, final Range<Comparable<?>> value2) {
        return null == value2 ? value1 : SafeNumberOperationUtil.safeIntersection(value1, value2);
    }
    
    private Collection<Comparable<?>> mergeListAndRangeShardingValues(final Collection<Comparable<?>> listValue, final Range<Comparable<?>> rangeValue) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (SafeNumberOperationUtil.safeContains(rangeValue, each)) {
                result.add(each);
            }
        }
        return result;
    }
}
