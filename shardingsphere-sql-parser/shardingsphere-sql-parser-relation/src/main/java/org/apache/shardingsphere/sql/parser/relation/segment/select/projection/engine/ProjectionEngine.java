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

package org.apache.shardingsphere.sql.parser.relation.segment.select.projection.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

/**
 * Projection engine.
 *
 * @author zhangliang
 */
public final class ProjectionEngine {
    
    private int aggregationAverageDerivedColumnCount;
    
    private int aggregationDistinctDerivedColumnCount;
    
    /**
     * Create projection.
     * 
     * @param sql SQL
     * @param selectItemSegment select item segment
     * @return projection
     */
    public Optional<Projection> createProjection(final String sql, final SelectItemSegment selectItemSegment) {
        if (selectItemSegment instanceof ShorthandSelectItemSegment) {
            return Optional.<Projection>of(createProjection((ShorthandSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof ColumnSelectItemSegment) {
            return Optional.<Projection>of(createProjection((ColumnSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof ExpressionSelectItemSegment) {
            return Optional.<Projection>of(createProjection((ExpressionSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof AggregationDistinctSelectItemSegment) {
            return Optional.<Projection>of(createProjection(sql, (AggregationDistinctSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof AggregationSelectItemSegment) {
            return Optional.<Projection>of(createProjection(sql, (AggregationSelectItemSegment) selectItemSegment));
        }
        // TODO subquery
        return Optional.absent();
    }
    
    private ShorthandProjection createProjection(final ShorthandSelectItemSegment selectItemSegment) {
        Optional<TableSegment> owner = selectItemSegment.getOwner();
        return new ShorthandProjection(owner.isPresent() ? owner.get().getTableName() : null);
    }
    
    private ColumnProjection createProjection(final ColumnSelectItemSegment selectItemSegment) {
        String owner = selectItemSegment.getOwner().isPresent() ? selectItemSegment.getOwner().get().getTableName() : null;
        return new ColumnProjection(owner, selectItemSegment.getName(), selectItemSegment.getAlias().orNull());
    }
    
    private ExpressionProjection createProjection(final ExpressionSelectItemSegment selectItemSegment) {
        return new ExpressionProjection(selectItemSegment.getText(), selectItemSegment.getAlias().orNull());
    }
    
    private AggregationDistinctProjection createProjection(final String sql, final AggregationDistinctSelectItemSegment selectItemSegment) {
        String innerExpression = sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1);
        String alias = selectItemSegment.getAlias().or(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++));
        AggregationDistinctProjection result = new AggregationDistinctProjection(
                selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), selectItemSegment.getType(), innerExpression, alias, selectItemSegment.getDistinctExpression());
        if (AggregationType.AVG == result.getType()) {
            appendAverageDistinctDerivedProjection(result);
        }
        return result;
    }
    
    private AggregationProjection createProjection(final String sql, final AggregationSelectItemSegment selectItemSegment) {
        String innerExpression = sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1);
        AggregationProjection result = new AggregationProjection(selectItemSegment.getType(), innerExpression, selectItemSegment.getAlias().orNull());
        if (AggregationType.AVG == result.getType()) {
            appendAverageDerivedProjection(result);
            // TODO replace avg to constant, avoid calculate useless avg
        }
        return result;
    }
    
    private void appendAverageDistinctDerivedProjection(final AggregationDistinctProjection averageDistinctProjection) {
        String innerExpression = averageDistinctProjection.getInnerExpression();
        String distinctInnerExpression = averageDistinctProjection.getDistinctInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctProjection countDistinctProjection = new AggregationDistinctProjection(0, 0, AggregationType.COUNT, innerExpression, countAlias, distinctInnerExpression);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctProjection sumDistinctProjection = new AggregationDistinctProjection(0, 0, AggregationType.SUM, innerExpression, sumAlias, distinctInnerExpression);
        averageDistinctProjection.getDerivedAggregationProjections().add(countDistinctProjection);
        averageDistinctProjection.getDerivedAggregationProjections().add(sumDistinctProjection);
        aggregationAverageDerivedColumnCount++;
    }
    
    private void appendAverageDerivedProjection(final AggregationProjection averageProjection) {
        String innerExpression = averageProjection.getInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjection countProjection = new AggregationProjection(AggregationType.COUNT, innerExpression, countAlias);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjection sumProjection = new AggregationProjection(AggregationType.SUM, innerExpression, sumAlias);
        averageProjection.getDerivedAggregationProjections().add(countProjection);
        averageProjection.getDerivedAggregationProjections().add(sumProjection);
        aggregationAverageDerivedColumnCount++;
    }
}
