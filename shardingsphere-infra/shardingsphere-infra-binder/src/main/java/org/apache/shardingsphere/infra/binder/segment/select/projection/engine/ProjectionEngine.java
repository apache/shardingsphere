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

package org.apache.shardingsphere.infra.binder.segment.select.projection.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Projection engine.
 */
@RequiredArgsConstructor
public final class ProjectionEngine {
    
    private final PhysicalSchemaMetaData schemaMetaData;
    
    private int aggregationAverageDerivedColumnCount;
    
    private int aggregationDistinctDerivedColumnCount;
    
    /**
     * Create projection.
     * 
     * @param tableSegments table segments
     * @param projectionSegment projection segment
     * @return projection
     */
    public Optional<Projection> createProjection(final Collection<SimpleTableSegment> tableSegments, final ProjectionSegment projectionSegment) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return Optional.of(createProjection(tableSegments, (ShorthandProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return Optional.of(createProjection((ColumnProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            return Optional.of(createProjection((ExpressionProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationDistinctProjectionSegment) {
            return Optional.of(createProjection((AggregationDistinctProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationProjectionSegment) {
            return Optional.of(createProjection((AggregationProjectionSegment) projectionSegment));
        }
        // TODO subquery
        return Optional.empty();
    }
    
    private ShorthandProjection createProjection(final Collection<SimpleTableSegment> tableSegments, final ShorthandProjectionSegment projectionSegment) {
        String owner = projectionSegment.getOwner().map(ownerSegment -> ownerSegment.getIdentifier().getValue()).orElse(null);
        Collection<ColumnProjection> columns = getShorthandColumns(tableSegments, owner);
        return new ShorthandProjection(owner, columns);
    }
    
    private ColumnProjection createProjection(final ColumnProjectionSegment projectionSegment) {
        String owner = projectionSegment.getColumn().getOwner().isPresent() ? projectionSegment.getColumn().getOwner().get().getIdentifier().getValue() : null;
        return new ColumnProjection(owner, projectionSegment.getColumn().getIdentifier().getValue(), projectionSegment.getAlias().orElse(null));
    }
    
    private ExpressionProjection createProjection(final ExpressionProjectionSegment projectionSegment) {
        return new ExpressionProjection(projectionSegment.getText(), projectionSegment.getAlias().orElse(null));
    }
    
    private AggregationDistinctProjection createProjection(final AggregationDistinctProjectionSegment projectionSegment) {
        String innerExpression = projectionSegment.getInnerExpression();
        String alias = projectionSegment.getAlias().orElse(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++));
        AggregationDistinctProjection result = new AggregationDistinctProjection(
                projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), projectionSegment.getType(), innerExpression, alias, projectionSegment.getDistinctExpression());
        if (AggregationType.AVG == result.getType()) {
            appendAverageDistinctDerivedProjection(result);
        }
        return result;
    }
    
    private AggregationProjection createProjection(final AggregationProjectionSegment projectionSegment) {
        String innerExpression = projectionSegment.getInnerExpression();
        AggregationProjection result = new AggregationProjection(projectionSegment.getType(), innerExpression, projectionSegment.getAlias().orElse(null));
        if (AggregationType.AVG == result.getType()) {
            appendAverageDerivedProjection(result);
            // TODO replace avg to constant, avoid calculate useless avg
        }
        return result;
    }
    
    private Collection<ColumnProjection> getShorthandColumns(final Collection<SimpleTableSegment> tables, final String owner) {
        return null == owner ? getUnqualifiedShorthandColumns(tables) : getQualifiedShorthandColumns(tables, owner);
    }
    
    private Collection<ColumnProjection> getUnqualifiedShorthandColumns(final Collection<SimpleTableSegment> tables) {
        Collection<ColumnProjection> result = new LinkedList<>();
        for (SimpleTableSegment each : tables) {
            result.addAll(schemaMetaData.getAllColumnNames(
                    each.getTableName().getIdentifier().getValue()).stream().map(columnName -> new ColumnProjection(null, columnName, null)).collect(Collectors.toList()));
        }
        return result;
    }
    
    private Collection<ColumnProjection> getQualifiedShorthandColumns(final Collection<SimpleTableSegment> tables, final String owner) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (owner.equalsIgnoreCase(each.getAlias().orElse(tableName))) {
                return schemaMetaData.getAllColumnNames(tableName).stream().map(columnName -> new ColumnProjection(owner, columnName, null)).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
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
