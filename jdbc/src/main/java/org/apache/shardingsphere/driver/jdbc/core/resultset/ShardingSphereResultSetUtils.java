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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        int columnCount = resultSetMetaData.getColumnCount();
        Map<String, Integer> result = new CaseInsensitiveMap<>(columnCount, 1F);
        for (int columnIndex = columnCount; columnIndex > 0; columnIndex--) {
            String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
            if (!isDerivedColumn(sqlStatementContext, columnLabel)) {
                result.put(aggregationDistinctColumnLabels.getOrDefault(columnLabel, columnLabel), columnIndex);
            }
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
        List<String> returnedClientLabels = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
            if (!isAppendedDerivedColumnLabel(columnLabel)) {
                returnedClientLabels.add(columnLabel);
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
    
    private static boolean isDerivedColumn(final SQLStatementContext sqlStatementContext, final String columnLabel) {
        return mayAppendDerivedColumns(sqlStatementContext) && isAppendedDerivedColumnLabel(columnLabel);
    }
    
    static boolean mayAppendDerivedColumns(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections();
    }
    
    static boolean isAppendedDerivedColumnLabel(final String columnLabel) {
        return DerivedColumn.isDerivedColumn(columnLabel.toUpperCase(Locale.ROOT));
    }
}
