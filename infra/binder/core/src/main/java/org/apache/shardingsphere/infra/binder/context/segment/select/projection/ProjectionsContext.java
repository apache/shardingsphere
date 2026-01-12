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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Projections context.
 */
@Getter
@ToString
public final class ProjectionsContext {
    
    private static final String LAST_INSERT_ID_FUNCTION_EXPRESSION = "LAST_INSERT_ID()";
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final boolean distinctRow;
    
    private final Collection<Projection> projections;
    
    private final Collection<AggregationDistinctProjection> aggregationDistinctProjections;
    
    private final List<Projection> expandProjections;
    
    private final boolean containsLastInsertIdProjection;
    
    public ProjectionsContext(final int startIndex, final int stopIndex, final boolean distinctRow, final Collection<Projection> projections) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.distinctRow = distinctRow;
        this.projections = projections;
        aggregationDistinctProjections = createAggregationDistinctProjections();
        expandProjections = createExpandProjections();
        containsLastInsertIdProjection = isContainsLastInsertIdProjection(projections);
    }
    
    private Collection<AggregationDistinctProjection> createAggregationDistinctProjections() {
        Collection<AggregationDistinctProjection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof AggregationDistinctProjection) {
                result.add((AggregationDistinctProjection) each);
            }
        }
        return result;
    }
    
    private List<Projection> createExpandProjections() {
        List<Projection> result = new ArrayList<>();
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection) {
                result.addAll(((ShorthandProjection) each).getActualColumns());
            } else if (!(each instanceof DerivedProjection)) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Judge is unqualified shorthand projection or not.
     *
     * @return is unqualified shorthand projection or not
     */
    public boolean isUnqualifiedShorthandProjection() {
        if (1 != projections.size()) {
            return false;
        }
        Projection projection = projections.iterator().next();
        return projection instanceof ShorthandProjection && !((ShorthandProjection) projection).getOwner().isPresent();
    }
    
    /**
     * Find alias.
     *
     * @param projectionName projection name
     * @return projection alias
     */
    public Optional<String> findAlias(final String projectionName) {
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection) {
                Optional<Projection> projection =
                        ((ShorthandProjection) each).getActualColumns().stream().filter(optional -> projectionName.equalsIgnoreCase(getOriginalColumnName(optional))).findFirst();
                if (projection.isPresent()) {
                    return projection.map(Projection::getExpression);
                }
            }
            if (projectionName.equalsIgnoreCase(SQLUtils.getExactlyValue(each.getExpression()))) {
                return each.getAlias().map(IdentifierValue::getValue);
            }
        }
        return Optional.empty();
    }
    
    private String getOriginalColumnName(final Projection projection) {
        return projection instanceof ColumnProjection ? ((ColumnProjection) projection).getOriginalColumn().getValue() : projection.getExpression();
    }
    
    /**
     * Find projection index.
     *
     * @param projectionName projection name
     * @return projection index
     */
    public Optional<Integer> findProjectionIndex(final String projectionName) {
        int result = 1;
        for (Projection each : projections) {
            if (projectionName.equalsIgnoreCase(SQLUtils.getExactlyValue(each.getExpression()))) {
                return Optional.of(result);
            }
            result++;
        }
        return Optional.empty();
    }
    
    /**
     * Get aggregation projections.
     *
     * @return aggregation projections
     */
    public List<AggregationProjection> getAggregationProjections() {
        List<AggregationProjection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof AggregationProjection) {
                AggregationProjection aggregationProjection = (AggregationProjection) each;
                result.add(aggregationProjection);
                result.addAll(aggregationProjection.getDerivedAggregationProjections());
            }
        }
        return result;
    }
    public List<AggregationProjection> getExpandAggregationProjections() {
        List<AggregationProjection> result = new LinkedList<>();
        int columnIndex = 1;
        for (Projection each : projections) {
            if (each instanceof AggregationProjection) {
                AggregationProjection aggregationProjection = (AggregationProjection) each;
                result.add(aggregationProjection);
                result.addAll(aggregationProjection.getDerivedAggregationProjections());
            }
            else if (each instanceof ExpressionProjection) {
                ExpressionProjection expressionProjection = (ExpressionProjection) each;
                for (AggregationProjectionSegment eachSegment : expressionProjection.getExpressionSegment().getAggregationProjectionSegments()) {
                    AggregationProjection nested = new AggregationProjection(
                            eachSegment.getType(), eachSegment,
                            eachSegment.getAliasName().map(IdentifierValue::new).orElse(null),
                            expressionProjection.getDatabaseType());
                    nested.setIndex(columnIndex);
                    result.add(nested);
                }
            }
            columnIndex++;
        }
        return result;
    }
    private boolean isContainsLastInsertIdProjection(final Collection<Projection> projections) {
        for (Projection each : projections) {
            if (LAST_INSERT_ID_FUNCTION_EXPRESSION.equalsIgnoreCase(SQLUtils.getExactlyExpression(each.getExpression()))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Find column projection.
     *
     * @param columnIndex column index
     * @return found column projection
     */
    public Optional<ColumnProjection> findColumnProjection(final int columnIndex) {
        if (expandProjections.size() < columnIndex) {
            return Optional.empty();
        }
        Projection projection = expandProjections.get(columnIndex - 1);
        return projection instanceof ColumnProjection ? Optional.of((ColumnProjection) projection) : Optional.empty();
    }
}
