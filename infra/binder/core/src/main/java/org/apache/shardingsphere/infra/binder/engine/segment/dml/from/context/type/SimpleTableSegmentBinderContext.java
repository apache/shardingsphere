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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Simple table segment binder context.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SimpleTableSegmentBinderContext implements TableSegmentBinderContext {
    
    @Getter(AccessLevel.NONE)
    private final Map<String, ProjectionSegment> columnLabelProjectionSegments;
    
    private final TableSourceType tableSourceType;
    
    private boolean fromWithSegment;
    
    public SimpleTableSegmentBinderContext(final Collection<ProjectionSegment> projectionSegments, final TableSourceType tableSourceType) {
        columnLabelProjectionSegments = new CaseInsensitiveMap<>(projectionSegments.size(), 1F);
        projectionSegments.forEach(each -> putColumnLabelProjectionSegments(each, columnLabelProjectionSegments));
        this.tableSourceType = tableSourceType;
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
