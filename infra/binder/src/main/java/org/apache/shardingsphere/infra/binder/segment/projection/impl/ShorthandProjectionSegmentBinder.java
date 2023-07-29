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

package org.apache.shardingsphere.infra.binder.segment.projection.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Map;

/**
 * Column projection segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShorthandProjectionSegmentBinder {
    
    /**
     * Bind column projection segment with metadata.
     *
     * @param segment table segment
     * @param boundedTableSegment bounded table segment
     * @param tableBinderContexts table binder contexts
     * @return bounded column projection segment
     */
    public static ShorthandProjectionSegment bind(final ShorthandProjectionSegment segment, final TableSegment boundedTableSegment,
                                                  final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        if (segment.getOwner().isPresent()) {
            expandVisibleColumn(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, segment.getOwner().get().getIdentifier().getValue()), segment);
        } else {
            bindNoOwnerProjections(segment, boundedTableSegment, tableBinderContexts);
        }
        return segment;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegmentsByTableAliasOrName(final Map<String, TableSegmentBinderContext> tableBinderContexts, final String tableAliasOrName) {
        ShardingSpherePreconditions.checkState(tableBinderContexts.containsKey(tableAliasOrName),
                () -> new IllegalStateException(String.format("Can not find table binder context by table alias or name %s.", tableAliasOrName)));
        return tableBinderContexts.get(tableAliasOrName).getProjectionSegments();
    }
    
    private static void expandVisibleColumn(final Collection<ProjectionSegment> projectionSegments, final ShorthandProjectionSegment shorthandProjectionSegment) {
        for (ProjectionSegment each : projectionSegments) {
            if (each.isVisible()) {
                shorthandProjectionSegment.getActualProjectionSegments().add(each);
            }
        }
    }
    
    private static void bindNoOwnerProjections(final ShorthandProjectionSegment segment, final TableSegment boundedTableSegment,
                                               final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        if (boundedTableSegment instanceof SimpleTableSegment) {
            String tableAliasOrName = boundedTableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) boundedTableSegment).getTableName().getIdentifier().getValue());
            expandVisibleColumn(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableAliasOrName), segment);
        } else if (boundedTableSegment instanceof JoinTableSegment) {
            expandVisibleColumn(((JoinTableSegment) boundedTableSegment).getJoinTableProjectionSegments(), segment);
        } else if (boundedTableSegment instanceof SubqueryTableSegment) {
            expandVisibleColumn(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, boundedTableSegment.getAliasName().orElse("")), segment);
        }
    }
}
