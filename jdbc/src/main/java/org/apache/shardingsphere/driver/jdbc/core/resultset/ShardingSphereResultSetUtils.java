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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
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
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount(), 1F);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            if (!isDerivedColumn(sqlStatementContext, resultSetMetaData.getColumnLabel(columnIndex))) {
                result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
            }
        }
        return result;
    }
    
    /**
     * Whether to use expanded projections of select statement to describe the result set.
     *
     * <p>The expanded projections describe the result set delivered to the client. The count check is skipped only when
     * the count mismatch is fully explained by the derived columns the rewrite appends: the SUM and COUNT columns of an
     * AVG rewrite and the derived order-by and group-by columns are appended only by a multi-route rewrite (the
     * projection token generators are ignored for single routes). An in-place aggregation-distinct stand-in, e.g.
     * {@code user_id AS AGGREGATION_DISTINCT_DERIVED_0} for {@code COUNT(DISTINCT user_id)}, keeps the position of its
     * client column and is matched by its alias. After removing the appended derived columns, the returned metadata
     * still has to name the expanded projections position by position, because backend schema drift and appended
     * derived columns can occur together. Any other mismatch is genuine metadata drift, and the returned metadata
     * stays authoritative for the client-facing columns, with the appended derived columns hidden.</p>
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @return use expanded projections or not
     * @throws SQLException SQL exception
     */
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
