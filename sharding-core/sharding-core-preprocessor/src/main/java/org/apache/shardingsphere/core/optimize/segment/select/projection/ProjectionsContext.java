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

package org.apache.shardingsphere.core.optimize.segment.select.projection;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.core.optimize.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Projections context.
 *
 * @author zhangliang
 * @author sunbufu
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ProjectionsContext {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final boolean distinctRow;
    
    private final Collection<Projection> projections;
    
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
            if (projectionName.equalsIgnoreCase(each.getExpression())) {
                return each.getAlias();
            }
        }
        return Optional.absent();
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
            if (projectionName.equalsIgnoreCase(each.getExpression())) {
                return Optional.of(result);
            }
            result++;
        }
        return Optional.absent();
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
    
    /**
     * Get aggregation distinct projections.
     *
     * @return aggregation distinct projections
     */
    public List<AggregationDistinctProjection> getAggregationDistinctProjections() {
        List<AggregationDistinctProjection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof AggregationDistinctProjection) {
                result.add((AggregationDistinctProjection) each);
            }
        }
        return result;
    }
    
    /**
     * Get column labels.
     * 
     * @param tableMetas table metas
     * @param tables tables
     * @return column labels
     */
    public List<String> getColumnLabels(final TableMetas tableMetas, final Collection<TableSegment> tables) {
        List<String> result = new ArrayList<>(projections.size());
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection) {
                result.addAll(getShorthandColumnLabels(tableMetas, tables, (ShorthandProjection) each));
            } else {
                result.add(each.getColumnLabel());
            }
        }
        return result;
    }
    
    private Collection<String> getShorthandColumnLabels(final TableMetas tableMetas, final Collection<TableSegment> tables, final ShorthandProjection shorthandProjection) {
        return shorthandProjection.getOwner().isPresent()
                ? getQualifiedShorthandColumnLabels(tableMetas, tables, shorthandProjection.getOwner().get()) : getUnqualifiedShorthandColumnLabels(tableMetas, tables);
    }
    
    private Collection<String> getQualifiedShorthandColumnLabels(final TableMetas tableMetas, final Collection<TableSegment> tables, final String owner) {
        for (TableSegment each : tables) {
            if (owner.equalsIgnoreCase(each.getAlias().or(each.getTableName()))) {
                return tableMetas.get(each.getTableName()).getColumns().keySet();
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<String> getUnqualifiedShorthandColumnLabels(final TableMetas tableMetas, final Collection<TableSegment> tables) {
        Collection<String> result = new LinkedList<>();
        for (TableSegment each : tables) {
            result.addAll(tableMetas.get(each.getTableName()).getColumns().keySet());
        }
        return result;
    }
}
