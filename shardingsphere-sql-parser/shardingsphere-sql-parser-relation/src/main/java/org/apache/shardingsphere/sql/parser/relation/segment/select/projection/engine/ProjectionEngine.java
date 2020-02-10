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
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
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
     * @param projectionSegment projection segment
     * @return projection
     */
    public Optional<Projection> createProjection(final String sql, final ProjectionSegment projectionSegment) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return Optional.<Projection>of(createProjection((ShorthandProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return Optional.<Projection>of(createProjection((ColumnProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            return Optional.<Projection>of(createProjection((ExpressionProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationDistinctProjectionSegment) {
            return Optional.<Projection>of(createProjection(sql, (AggregationDistinctProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationProjectionSegment) {
            return Optional.<Projection>of(createProjection(sql, (AggregationProjectionSegment) projectionSegment));
        }
        // TODO subquery
        return Optional.absent();
    }
    
    private ShorthandProjection createProjection(final ShorthandProjectionSegment projectionSegment) {
        Optional<TableSegment> owner = projectionSegment.getOwner();
        return new ShorthandProjection(owner.isPresent() ? owner.get().getTableName() : null);
    }
    
    private ColumnProjection createProjection(final ColumnProjectionSegment projectionSegment) {
        String owner = projectionSegment.getOwner().isPresent() ? projectionSegment.getOwner().get().getTableName() : null;
        return new ColumnProjection(owner, projectionSegment.getName(), projectionSegment.getAlias().orNull());
    }
    
    private ExpressionProjection createProjection(final ExpressionProjectionSegment projectionSegment) {
        return new ExpressionProjection(projectionSegment.getText(), projectionSegment.getAlias().orNull());
    }
    
    private AggregationDistinctProjection createProjection(final String sql, final AggregationDistinctProjectionSegment projectionSegment) {
        String innerExpression = sql.substring(projectionSegment.getInnerExpressionStartIndex(), projectionSegment.getStopIndex() + 1);
        String alias = projectionSegment.getAlias().or(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++));
        AggregationDistinctProjection result = new AggregationDistinctProjection(
                projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), projectionSegment.getType(), innerExpression, alias, projectionSegment.getDistinctExpression());
        if (AggregationType.AVG == result.getType()) {
            appendAverageDistinctDerivedProjection(result);
        }
        return result;
    }
    
    private AggregationProjection createProjection(final String sql, final AggregationProjectionSegment projectionSegment) {
        String innerExpression = sql.substring(projectionSegment.getInnerExpressionStartIndex(), projectionSegment.getStopIndex() + 1);
        AggregationProjection result = new AggregationProjection(projectionSegment.getType(), innerExpression, projectionSegment.getAlias().orNull());
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
