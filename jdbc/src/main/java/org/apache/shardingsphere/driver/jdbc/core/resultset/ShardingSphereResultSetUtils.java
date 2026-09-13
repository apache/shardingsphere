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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.syntax.ColumnIndexOutOfRangeException;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
            // The derived columns a rewrite appends are internal plumbing; they never belong to the client-facing view.
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
     * projection token generators are ignored for single routes). After removing those derived columns, the returned
     * metadata still has to name the expanded projections, because backend schema drift and appended derived columns
     * can occur together. Any other mismatch is genuine metadata drift, and the returned metadata stays authoritative
     * for the client-facing columns, with the derived columns hidden.</p>
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @return use expanded projections or not
     * @throws SQLException SQL exception
     */
    public static boolean useExpandedProjections(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
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
    
    /**
     * Get the count of the columns of the returned metadata which are visible to the client.
     *
     * <p>The derived columns a rewrite appends are hidden when the returned metadata is authoritative.</p>
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @return count of client-visible columns
     * @throws SQLException SQL exception
     */
    public static int getVisibleColumnCount(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (!mayAppendDerivedColumns(sqlStatementContext)) {
            return resultSetMetaData.getColumnCount();
        }
        int result = 0;
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            if (!isDerivedColumnLabel(resultSetMetaData.getColumnLabel(columnIndex))) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Get the index in the returned metadata of the client-visible column.
     *
     * @param sqlStatementContext SQL statement context
     * @param resultSetMetaData meta data of result set
     * @param column client-visible column ordinal, starting from 1
     * @return index in the returned metadata, starting from 1
     * @throws SQLException SQL exception
     */
    public static int getVisibleColumnIndex(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData, final int column) throws SQLException {
        if (!mayAppendDerivedColumns(sqlStatementContext)) {
            return column;
        }
        int visibleColumnCount = 0;
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            if (!isDerivedColumnLabel(resultSetMetaData.getColumnLabel(columnIndex))) {
                visibleColumnCount++;
                if (visibleColumnCount == column) {
                    return columnIndex;
                }
            }
        }
        throw new ColumnIndexOutOfRangeException(column).toSQLException();
    }
    
    private static boolean isCountMismatchExplainedByDerivedColumns(final SelectStatementContext selectStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        List<String> returnedLabels = new ArrayList<>(resultSetMetaData.getColumnCount());
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            String columnLabel = resultSetMetaData.getColumnLabel(columnIndex);
            if (!isDerivedColumnLabel(columnLabel)) {
                returnedLabels.add(columnLabel.toUpperCase(Locale.ROOT));
            }
        }
        List<String> expandedLabels = new ArrayList<>(selectStatementContext.getProjectionsContext().getExpandProjections().size());
        for (Projection each : selectStatementContext.getProjectionsContext().getExpandProjections()) {
            String columnLabel = each.getColumnLabel();
            if (!isDerivedColumnLabel(columnLabel)) {
                expandedLabels.add(columnLabel.toUpperCase(Locale.ROOT));
            }
        }
        Collections.sort(returnedLabels);
        Collections.sort(expandedLabels);
        return returnedLabels.equals(expandedLabels);
    }
    
    private static boolean isDerivedColumn(final SQLStatementContext sqlStatementContext, final String columnLabel) {
        return mayAppendDerivedColumns(sqlStatementContext) && isDerivedColumnLabel(columnLabel);
    }
    
    private static boolean mayAppendDerivedColumns(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections();
    }
    
    private static boolean isDerivedColumnLabel(final String columnLabel) {
        // Backends fold unquoted aliases to lowercase (e.g. PostgreSQL), while the derived alias patterns are upper case.
        return DerivedColumn.isDerivedColumnName(columnLabel.toUpperCase(Locale.ROOT));
    }
}
