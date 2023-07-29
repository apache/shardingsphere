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

package org.apache.shardingsphere.infra.binder.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Column segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnSegmentBinder {
    
    private static final Collection<String> EXCLUDE_BIND_COLUMNS = new LinkedHashSet<>(Arrays.asList("ROWNUM", "ROW_NUMBER"));
    
    /**
     * Bind column segment with metadata.
     *
     * @param segment table segment
     * @param tableBinderContexts table binder contexts
     * @return bounded column segment
     */
    public static ColumnSegment bind(final ColumnSegment segment, final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        if (EXCLUDE_BIND_COLUMNS.contains(segment.getIdentifier().getValue().toUpperCase())) {
            return segment;
        }
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        segment.getOwner().ifPresent(result::setOwner);
        Collection<TableSegmentBinderContext> tableBinderContextValues =
                segment.getOwner().isPresent() ? Collections.singleton(tableBinderContexts.get(segment.getOwner().get().getIdentifier().getValue())) : tableBinderContexts.values();
        ColumnSegment inputColumnSegment = findInputColumnSegment(segment.getIdentifier().getValue(), tableBinderContextValues);
        result.setOriginalDatabase(inputColumnSegment.getOriginalDatabase());
        result.setOriginalSchema(inputColumnSegment.getOriginalSchema());
        result.setOriginalTable(null == segment.getOriginalTable() ? inputColumnSegment.getOriginalTable() : segment.getOriginalTable());
        result.setOriginalColumn(null == segment.getOriginalColumn() ? segment.getIdentifier() : segment.getOriginalColumn());
        return result;
    }
    
    private static ColumnSegment findInputColumnSegment(final String columnName, final Collection<TableSegmentBinderContext> tableBinderContexts) {
        ColumnSegment result = null;
        for (TableSegmentBinderContext each : tableBinderContexts) {
            ProjectionSegment projectionSegment = each.getProjectionSegmentByColumnLabel(columnName);
            if (projectionSegment instanceof ColumnProjectionSegment) {
                ShardingSpherePreconditions.checkState(null == result, () -> new IllegalStateException(String.format("Column '%s' in field list is ambiguous.", columnName)));
                result = ((ColumnProjectionSegment) projectionSegment).getColumn();
            }
        }
        ShardingSpherePreconditions.checkNotNull(result, () -> new IllegalStateException(String.format("Unknown column '%s' in 'field list'.", columnName)));
        return result;
    }
}
