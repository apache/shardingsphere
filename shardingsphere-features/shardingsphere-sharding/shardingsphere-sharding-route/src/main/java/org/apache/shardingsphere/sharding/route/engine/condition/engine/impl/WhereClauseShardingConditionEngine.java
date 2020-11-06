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
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.route.engine.condition.value.AlwaysFalseShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.AlwaysFalseShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGeneratorFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereSegmentExtractUtils;

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
    
    private final PhysicalSchemaMetaData schemaMetaData;
    
    @Override
    public List<ShardingCondition> createShardingConditions(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Collections.emptyList();
        }
        List<ShardingCondition> result = new ArrayList<>();
        ((WhereAvailable) sqlStatementContext).getWhere().ifPresent(segment -> result.addAll(createShardingConditions(sqlStatementContext, segment.getExpr(), parameters)));
        Collection<WhereSegment> subqueryWhereSegments = sqlStatementContext.getSqlStatement() instanceof SelectStatement
                ? WhereSegmentExtractUtils.getSubqueryWhereSegments((SelectStatement) sqlStatementContext.getSqlStatement()) : Collections.emptyList();
        for (WhereSegment each : subqueryWhereSegments) {
            Collection<ShardingCondition> subqueryShardingConditions = createShardingConditions(sqlStatementContext, each.getExpr(), parameters);
            if (!result.containsAll(subqueryShardingConditions)) {
                result.addAll(subqueryShardingConditions);
            }
        }
        return result;
    }
    
    private Collection<ShardingCondition> createShardingConditions(final SQLStatementContext<?> sqlStatementContext, final ExpressionSegment expressionSegment, final List<Object> parameters) {
        Collection<ShardingCondition> result = new LinkedList<>();
        for (AndPredicate each : new ExpressionBuilder(expressionSegment).extractAndPredicates().getAndPredicates()) {
            Map<Column, Collection<ShardingConditionValue>> shardingConditionValues = createShardingConditionValueMap(sqlStatementContext, each, parameters);
            if (shardingConditionValues.isEmpty()) {
                return Collections.emptyList();
            }
            result.add(createShardingCondition(shardingConditionValues));
        }
        return result;
    }
    
    private Map<Column, Collection<ShardingConditionValue>> createShardingConditionValueMap(final SQLStatementContext<?> sqlStatementContext, 
                                                                                            final AndPredicate andPredicate, final List<Object> parameters) {
        Map<Column, Collection<ShardingConditionValue>> result = new HashMap<>(andPredicate.getPredicates().size(), 1);
        for (ExpressionSegment each : andPredicate.getPredicates()) {
            Optional<ColumnSegment> columnSegment = ColumnExtractor.extract(each);
            if (!columnSegment.isPresent()) {
                continue;
            }
            Optional<String> tableName = sqlStatementContext.getTablesContext().findTableName(columnSegment.get(), schemaMetaData);
            if (!(tableName.isPresent() && shardingRule.isShardingColumn(columnSegment.get().getIdentifier().getValue(), tableName.get()))) {
                continue;
            }
            Column column = new Column(columnSegment.get().getIdentifier().getValue(), tableName.get());
            Optional<ShardingConditionValue> shardingConditionValue = ConditionValueGeneratorFactory.generate(each, column, parameters);
            if (shardingConditionValue.isPresent()) {
                if (!result.containsKey(column)) {
                    Collection<ShardingConditionValue> shardingConditionValues = new LinkedList<>();
                    shardingConditionValues.add(shardingConditionValue.get());
                    result.put(column, shardingConditionValues);
                } else {
                    result.get(column).add(shardingConditionValue.get());
                }
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
    
    @SuppressWarnings("unchecked")
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
