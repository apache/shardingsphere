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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for {@link ShardingSphereResultSet}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereResultSetUtils {
    
    /**
     * Create column label and index map.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @return column label and index map
     * @throws SQLException SQL exception
     */
    public static Map<String, Integer> createColumnLabelAndIndexMap(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (useExpandedProjections(sqlStatementContext, resultSetMetaData)) {
            return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getColumnLabelAndIndexMap();
        }
        Map<String, String> aggregationDistinctColumnLabels = createAggregationDistinctColumnLabels(sqlStatementContext);
        Set<Integer> appendedDerivedColumnIndexes = getAppendedDerivedColumnIndexes(sqlStatementContext, resultSetMetaData);
        int columnCount = resultSetMetaData.getColumnCount();
        Map<String, Integer> result = new CaseInsensitiveMap<>(columnCount, 1F);
        for (int columnIndex = columnCount; columnIndex > 0; columnIndex--) {
            if (appendedDerivedColumnIndexes.contains(columnIndex)) {
                continue;
            }
            String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
            result.put(aggregationDistinctColumnLabels.getOrDefault(columnLabel, columnLabel), columnIndex);
        }
        return result;
    }
    
    static Map<String, String> createAggregationDistinctColumnLabels(final SQLStatementContext sqlStatementContext) {
        if (!mayAppendDerivedColumns(sqlStatementContext)) {
            return Collections.emptyMap();
        }
        Collection<AggregationDistinctProjection> projections = ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getAggregationDistinctProjections();
        Map<String, String> result = new CaseInsensitiveMap<>(projections.size(), 1F);
        for (AggregationDistinctProjection each : projections) {
            each.getAlias().ifPresent(alias -> result.put(alias.getValue(), each.getColumnLabel()));
        }
        return result;
    }
    
    static boolean useExpandedProjections(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext selectStatementContext = (SelectStatementContext) sqlStatementContext;
        if (!selectStatementContext.containsDerivedProjections()) {
            return false;
        }
        if (selectStatementContext.getProjectionsContext().getExpandProjections().size() == resultSetMetaData.getColumnCount()) {
            return true;
        }
        return isCountMismatchExplainedByDerivedColumns(selectStatementContext, resultSetMetaData);
    }
    
    private static boolean isCountMismatchExplainedByDerivedColumns(final SelectStatementContext selectStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        Set<Integer> appendedDerivedColumnIndexes = getAppendedDerivedColumnIndexes(selectStatementContext, resultSetMetaData);
        List<String> returnedClientLabels = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (!appendedDerivedColumnIndexes.contains(columnIndex)) {
                returnedClientLabels.add(resultSetMetaData.getColumnLabel(columnIndex));
            }
        }
        List<Projection> expandProjections = selectStatementContext.getProjectionsContext().getExpandProjections();
        if (returnedClientLabels.size() != expandProjections.size()) {
            return false;
        }
        for (int index = 0; index < expandProjections.size(); index++) {
            if (!matchesProjection(expandProjections.get(index), returnedClientLabels.get(index))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean matchesProjection(final Projection projection, final String returnedLabel) {
        if (projection instanceof AggregationProjection) {
            Optional<String> alias = projection.getAlias().map(each -> each.getValue());
            if (alias.isPresent() && DerivedColumn.isDerivedColumnName(alias.get())) {
                return alias.get().toUpperCase(Locale.ROOT).equals(returnedLabel.toUpperCase(Locale.ROOT));
            }
        }
        return projection.getColumnLabel().toUpperCase(Locale.ROOT).equals(returnedLabel.toUpperCase(Locale.ROOT));
    }
    
    static boolean mayAppendDerivedColumns(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections();
    }
    
    /**
     * Locate the trailing result set columns the multi-route rewrite appended for derived projections. Only labels the
     * projections context actually derives are eligible, and at most as many trailing columns as derived projections exist,
     * so a real selected column whose name carries a derived prefix is kept.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @return indexes of appended derived columns
     * @throws SQLException SQL exception
     */
    static Set<Integer> getAppendedDerivedColumnIndexes(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (!mayAppendDerivedColumns(sqlStatementContext)) {
            return Collections.emptySet();
        }
        Set<String> appendedDerivedColumnLabels = getAppendedDerivedColumnLabels((SelectStatementContext) sqlStatementContext);
        if (appendedDerivedColumnLabels.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> result = new HashSet<>(appendedDerivedColumnLabels.size(), 1F);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0 && result.size() < appendedDerivedColumnLabels.size(); columnIndex--) {
            if (!appendedDerivedColumnLabels.contains(resultSetMetaData.getColumnLabel(columnIndex).toUpperCase(Locale.ROOT))) {
                break;
            }
            result.add(columnIndex);
        }
        return result;
    }
    
    private static Set<String> getAppendedDerivedColumnLabels(final SelectStatementContext selectStatementContext) {
        Set<String> result = new HashSet<>();
        for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
            if (each instanceof DerivedProjection) {
                each.getAlias().ifPresent(alias -> result.add(alias.getValue().toUpperCase(Locale.ROOT)));
            } else if (each instanceof AggregationProjection) {
                for (AggregationProjection derived : ((AggregationProjection) each).getDerivedAggregationProjections()) {
                    derived.getAlias().ifPresent(alias -> result.add(alias.getValue().toUpperCase(Locale.ROOT)));
                }
            }
        }
        return result;
    }
}
