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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
        if (sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections()) {
            ProjectionsContext projectionsContext = ((SelectStatementContext) sqlStatementContext).getProjectionsContext();
            if (projectionsContext.containsUnexpandedShorthandProjection()) {
                return buildFromPhysicalMetaData(resultSetMetaData, projectionsContext);
            }
            return projectionsContext.getColumnLabelAndIndexMap();
        }
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount(), 1F);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    /**
     * Get visible column count.
     *
     * <p>When a shorthand projection is unexpanded, visible columns are the physical columns
     * minus internally generated derived columns (which are always appended at the end).</p>
     *
     * @param selectStatementContext select statement context
     * @param resultSetMetaData physical result set meta data
     * @return visible column count
     * @throws SQLException SQL exception
     */
    public static int getVisibleColumnCount(final SelectStatementContext selectStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        ProjectionsContext projectionsContext = selectStatementContext.getProjectionsContext();
        if (projectionsContext.containsUnexpandedShorthandProjection()) {
            return resultSetMetaData.getColumnCount() - countInternalDerivedColumns(projectionsContext);
        }
        if (selectStatementContext.containsDerivedProjections()) {
            return projectionsContext.getExpandProjections().size();
        }
        return resultSetMetaData.getColumnCount();
    }
    
    private static Map<String, Integer> buildFromPhysicalMetaData(final ResultSetMetaData resultSetMetaData, final ProjectionsContext projectionsContext) throws SQLException {
        int visibleCount = resultSetMetaData.getColumnCount() - countInternalDerivedColumns(projectionsContext);
        Map<String, Integer> result = new CaseInsensitiveMap<>(visibleCount, 1F);
        for (int columnIndex = visibleCount; columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    /**
     * Count internally generated derived columns (ORDER_BY_DERIVED, GROUP_BY_DERIVED, AVG_DERIVED_COUNT, AVG_DERIVED_SUM).
     *
     * <p>These columns are appended at the end of the physical result set by the SQL rewriter
     * and must not be visible to the user.</p>
     *
     * @param projectionsContext projections context
     * @return count of internal derived columns
     */
    static int countInternalDerivedColumns(final ProjectionsContext projectionsContext) {
        int count = 0;
        for (Projection each : projectionsContext.getProjections()) {
            if (each instanceof DerivedProjection) {
                count++;
            }
            if (each instanceof AggregationProjection) {
                count += ((AggregationProjection) each).getDerivedAggregationProjections().size();
            }
        }
        return count;
    }
}
