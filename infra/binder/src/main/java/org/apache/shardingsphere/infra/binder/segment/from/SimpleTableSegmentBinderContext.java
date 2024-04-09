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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Simple table segment binder context.
 */
@RequiredArgsConstructor
public final class SimpleTableSegmentBinderContext implements TableSegmentBinderContext {
    
    private final Map<String, ProjectionSegment> columnLabelProjectionSegments;
    
    public SimpleTableSegmentBinderContext(final Collection<ProjectionSegment> projectionSegments) {
        columnLabelProjectionSegments = new CaseInsensitiveMap<>(projectionSegments.size(), 1F);
        projectionSegments.forEach(each -> putColumnLabelProjectionSegments(each, columnLabelProjectionSegments));
    }
    
    private void putColumnLabelProjectionSegments(final ProjectionSegment projectionSegment, final Map<String, ProjectionSegment> columnLabelProjectionSegments) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            ((ShorthandProjectionSegment) projectionSegment).getActualProjectionSegments().forEach(each -> columnLabelProjectionSegments.put(each.getColumnLabel(), each));
        } else {
            columnLabelProjectionSegments.put(projectionSegment.getColumnLabel(), projectionSegment);
        }
    }
    
    @Override
    public Optional<ProjectionSegment> findProjectionSegmentByColumnLabel(final String columnLabel) {
        return Optional.ofNullable(columnLabelProjectionSegments.get(columnLabel));
    }
    
    @Override
    public Collection<ProjectionSegment> getProjectionSegments() {
        return columnLabelProjectionSegments.values();
    }
}
