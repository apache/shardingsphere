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
        if (useExpandedProjections(sqlStatementContext, resultSetMetaData)) {
            return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getColumnLabelAndIndexMap();
        }
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount(), 1F);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    /**
     * Whether to use expanded projections of select statement to describe the result set.
     *
     * <p>The expanded projections describe the result set delivered to the client. Rewritten backend SQL returns more
     * columns than the expanded projections only when the rewrite appends projections, which happens for aggregations
     * owning derived aggregation projections (AVG is rewritten to SUM and COUNT) and for derived order-by and group-by
     * projections. Only those projections exempt the count check for metadata drift; a non-rewriting aggregation such
     * as COUNT or SUM keeps the check.</p>
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
        return selectStatementContext.containsDerivedProjections() && (mayRewriteColumnCount(selectStatementContext)
                || selectStatementContext.getProjectionsContext().getExpandProjections().size() == resultSetMetaData.getColumnCount());
    }
    
    private static boolean mayRewriteColumnCount(final SelectStatementContext selectStatementContext) {
        return selectStatementContext.getProjectionsContext().getProjections().stream().anyMatch(ShardingSphereResultSetUtils::isColumnCountRewriteProjection);
    }
    
    private static boolean isColumnCountRewriteProjection(final Projection projection) {
        return projection instanceof DerivedProjection
                || projection instanceof AggregationProjection && !((AggregationProjection) projection).getDerivedAggregationProjections().isEmpty();
    }
}
