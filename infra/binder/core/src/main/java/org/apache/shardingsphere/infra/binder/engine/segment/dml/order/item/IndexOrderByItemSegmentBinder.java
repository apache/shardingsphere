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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.order.item;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;

import java.util.Optional;

/**
 * Index order by item segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexOrderByItemSegmentBinder {
    
    /**
     * Bind index order by item segment.
     *
     * @param segment index order by item segment
     * @param tableBinderContexts table binder contexts
     * @return bound index order by item segment
     */
    public static IndexOrderByItemSegment bind(final IndexOrderByItemSegment segment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        IndexOrderByItemSegment result = new IndexOrderByItemSegment(segment.getStartIndex(),
                segment.getStopIndex(), segment.getColumnIndex(), segment.getOrderDirection(), segment.getNullsOrderType().orElse(null));
        findBoundColumn(segment.getColumnIndex(), tableBinderContexts).ifPresent(result::setBoundColumn);
        return result;
    }
    
    private static Optional<ColumnSegment> findBoundColumn(final int columnIndex, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        for (TableSegmentBinderContext each : tableBinderContexts.values()) {
            int index = 1;
            for (ProjectionSegment projectionSegment : each.getProjectionSegments()) {
                if (index == columnIndex) {
                    return projectionSegment instanceof ColumnProjectionSegment ? Optional.of(((ColumnProjectionSegment) projectionSegment).getColumn()) : Optional.empty();
                }
                index++;
            }
        }
        return Optional.empty();
    }
}
