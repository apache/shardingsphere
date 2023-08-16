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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.exception.AmbiguousColumnException;
import org.apache.shardingsphere.infra.exception.UnknownColumnException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
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
    
    private static final Collection<String> EXCLUDE_BIND_COLUMNS = new LinkedHashSet<>(Arrays.asList("ROWNUM", "ROW_NUMBER", "ROWNUM_"));
    
    /**
     * Bind column segment with metadata.
     *
     * @param segment table segment
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded column segment
     */
    public static ColumnSegment bind(final ColumnSegment segment, final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                     final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (EXCLUDE_BIND_COLUMNS.contains(segment.getIdentifier().getValue().toUpperCase())) {
            return segment;
        }
        ColumnSegment result = new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier());
        segment.getOwner().ifPresent(result::setOwner);
        Collection<TableSegmentBinderContext> tableBinderContextValues =
                segment.getOwner().isPresent() ? getTableBinderContextByOwner(segment.getOwner().get().getIdentifier().getValue(), tableBinderContexts, outerTableBinderContexts)
                        : tableBinderContexts.values();
        ColumnSegment inputColumnSegment = findInputColumnSegment(segment.getIdentifier().getValue(), tableBinderContextValues);
        result.setOriginalDatabase(inputColumnSegment.getOriginalDatabase());
        result.setOriginalSchema(inputColumnSegment.getOriginalSchema());
        result.setOriginalTable(null == segment.getOriginalTable() ? inputColumnSegment.getOriginalTable() : segment.getOriginalTable());
        result.setOriginalColumn(null == segment.getOriginalColumn() ? segment.getIdentifier() : segment.getOriginalColumn());
        return result;
    }
    
    private static Collection<TableSegmentBinderContext> getTableBinderContextByOwner(final String owner, final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                                                                      final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (tableBinderContexts.containsKey(owner)) {
            return Collections.singleton(tableBinderContexts.get(owner));
        }
        if (outerTableBinderContexts.containsKey(owner)) {
            return Collections.singleton(outerTableBinderContexts.get(owner));
        }
        return Collections.emptyList();
    }
    
    private static ColumnSegment findInputColumnSegment(final String columnName, final Collection<TableSegmentBinderContext> tableBinderContexts) {
        ColumnSegment result = null;
        for (TableSegmentBinderContext each : tableBinderContexts) {
            ProjectionSegment projectionSegment = each.getProjectionSegmentByColumnLabel(columnName);
            if (projectionSegment instanceof ColumnProjectionSegment) {
                ShardingSpherePreconditions.checkState(null == result, () -> new AmbiguousColumnException(columnName));
                result = ((ColumnProjectionSegment) projectionSegment).getColumn();
            }
        }
        // TODO optimize exception message according to different segment
        ShardingSpherePreconditions.checkNotNull(result, () -> new UnknownColumnException(columnName));
        return result;
    }
}
