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

package org.apache.shardingsphere.infra.binder.segment.from;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Table segment binder context.
 */
public final class TableSegmentBinderContext {
    
    private final Map<String, ProjectionSegment> columnLabelProjectionSegments;
    
    public TableSegmentBinderContext(final Collection<ProjectionSegment> projectionSegments) {
        columnLabelProjectionSegments = new LinkedHashMap<>(projectionSegments.size(), 1F);
        projectionSegments.forEach(each -> putColumnLabelProjectionSegments(each, columnLabelProjectionSegments));
    }
    
    private void putColumnLabelProjectionSegments(final ProjectionSegment projectionSegment, final Map<String, ProjectionSegment> columnLabelProjectionSegments) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            ((ShorthandProjectionSegment) projectionSegment).getActualProjectionSegments().forEach(each -> columnLabelProjectionSegments.put(each.getColumnLabel().toLowerCase(), each));
        } else {
            columnLabelProjectionSegments.put(projectionSegment.getColumnLabel().toLowerCase(), projectionSegment);
        }
    }
    
    /**
     * Get projection segment by column label.
     * 
     * @param columnLabel column label
     * @return projection segment
     */
    public ProjectionSegment getProjectionSegmentByColumnLabel(final String columnLabel) {
        return columnLabelProjectionSegments.get(columnLabel.toLowerCase());
    }
    
    /**
     * Get projection segments.
     *
     * @return projection segments
     */
    public Collection<ProjectionSegment> getProjectionSegments() {
        return columnLabelProjectionSegments.values();
    }
}
