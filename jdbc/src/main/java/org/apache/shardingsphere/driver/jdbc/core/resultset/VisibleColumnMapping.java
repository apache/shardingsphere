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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Visible column mapping that resolves the authoritative result shape for a query.
 *
 * <p>This class is the single resolution point for visible column count, label-to-index mapping,
 * and per-column projection availability. JDBC and Proxy consumers that need visible column
 * shape should delegate to this mapping rather than computing it independently.</p>
 *
 * <p>Two resolution paths exist:</p>
 * <ul>
 *   <li>When a shorthand projection is unexpanded (binding was skipped), physical {@link ResultSetMetaData}
 *       is fully authoritative because the SQL was not rewritten and no internal columns were appended.</li>
 *   <li>When projections are fully expanded, logical {@code expandProjections} are authoritative.</li>
 * </ul>
 */
@HighFrequencyInvocation
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class VisibleColumnMapping {
    
    private final int columnCount;
    
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    /**
     * Expanded projections when available, or {@code null} when physical metadata is authoritative.
     */
    private final List<Projection> expandProjections;
    
    /**
     * Resolve visible column mapping for a SELECT statement with derived projections.
     *
     * @param selectStatementContext select statement context
     * @param physicalMetaData physical result set meta data from the underlying database
     * @return visible column mapping
     * @throws SQLException SQL exception
     */
    public static VisibleColumnMapping resolve(final SelectStatementContext selectStatementContext, final ResultSetMetaData physicalMetaData) throws SQLException {
        ProjectionsContext projectionsContext = selectStatementContext.getProjectionsContext();
        if (projectionsContext.containsUnexpandedShorthandProjection()) {
            int visibleCount = physicalMetaData.getColumnCount();
            Map<String, Integer> labelMap = new CaseInsensitiveMap<>(visibleCount, 1F);
            for (int columnIndex = visibleCount; columnIndex > 0; columnIndex--) {
                labelMap.put(physicalMetaData.getColumnLabel(columnIndex), columnIndex);
            }
            return new VisibleColumnMapping(visibleCount, labelMap, null);
        }
        List<Projection> expandProjections = projectionsContext.getExpandProjections();
        return new VisibleColumnMapping(expandProjections.size(), projectionsContext.getColumnLabelAndIndexMap(), expandProjections);
    }
    
    /**
     * Resolve visible column mapping when projections are known to be fully expanded.
     *
     * <p>Returns {@code null} when the shorthand projection is unexpanded, so callers that lack
     * physical {@link ResultSetMetaData} can fall back to metadata-based resolution.</p>
     *
     * @param selectStatementContext select statement context
     * @return visible column mapping, or {@code null} when shorthand is unexpanded
     */
    public static VisibleColumnMapping resolveIfExpanded(final SelectStatementContext selectStatementContext) {
        ProjectionsContext projectionsContext = selectStatementContext.getProjectionsContext();
        if (projectionsContext.containsUnexpandedShorthandProjection()) {
            return null;
        }
        List<Projection> expandProjections = projectionsContext.getExpandProjections();
        return new VisibleColumnMapping(expandProjections.size(), projectionsContext.getColumnLabelAndIndexMap(), expandProjections);
    }
    
    /**
     * Find the projection at the given visible column index.
     *
     * @param columnIndex 1-based visible column index
     * @return projection, or {@code null} when physical metadata is authoritative
     */
    public Projection findProjection(final int columnIndex) {
        return null == expandProjections ? null : expandProjections.get(columnIndex - 1);
    }
}
