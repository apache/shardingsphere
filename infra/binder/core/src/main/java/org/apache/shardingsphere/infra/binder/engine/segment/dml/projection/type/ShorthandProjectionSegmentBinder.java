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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Collection;

/**
 * Shorthand projection segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShorthandProjectionSegmentBinder {
    
    /**
     * Bind shorthand projection segment.
     *
     * @param segment table segment
     * @param boundTableSegment bound table segment
     * @param tableBinderContexts table binder contexts
     * @return bound shorthand projection segment
     */
    public static ShorthandProjectionSegment bind(final ShorthandProjectionSegment segment, final TableSegment boundTableSegment,
                                                  final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        ShorthandProjectionSegment result = copy(segment);
        if (segment.getOwner().isPresent()) {
            expandVisibleColumns(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, segment.getOwner().get().getIdentifier().getValue()), result);
        } else {
            expandNoOwnerProjections(boundTableSegment, tableBinderContexts, result);
        }
        return result;
    }
    
    private static ShorthandProjectionSegment copy(final ShorthandProjectionSegment segment) {
        ShorthandProjectionSegment result = new ShorthandProjectionSegment(segment.getStartIndex(), segment.getStopIndex());
        segment.getOwner().ifPresent(result::setOwner);
        segment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
    
    private static Collection<ProjectionSegment> getProjectionSegmentsByTableAliasOrName(final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                         final String tableAliasOrName) {
        ShardingSpherePreconditions.checkContains(tableBinderContexts.keySet(), new CaseInsensitiveString(tableAliasOrName),
                () -> new IllegalStateException(String.format("Can not find table binder context by table alias or name %s.", tableAliasOrName)));
        return tableBinderContexts.get(new CaseInsensitiveString(tableAliasOrName)).iterator().next().getProjectionSegments();
    }
    
    private static void expandVisibleColumns(final Collection<ProjectionSegment> projectionSegments, final ShorthandProjectionSegment segment) {
        for (ProjectionSegment each : projectionSegments) {
            if (each.isVisible()) {
                segment.getActualProjectionSegments().add(each);
            }
        }
    }
    
    private static void expandNoOwnerProjections(final TableSegment boundTableSegment, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                 final ShorthandProjectionSegment segment) {
        if (boundTableSegment instanceof SimpleTableSegment) {
            String tableAliasOrName = boundTableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) boundTableSegment).getTableName().getIdentifier().getValue());
            expandVisibleColumns(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, tableAliasOrName), segment);
        } else if (boundTableSegment instanceof JoinTableSegment) {
            expandVisibleColumns(((JoinTableSegment) boundTableSegment).getDerivedJoinTableProjectionSegments(), segment);
        } else if (boundTableSegment instanceof SubqueryTableSegment) {
            expandVisibleColumns(getProjectionSegmentsByTableAliasOrName(tableBinderContexts, boundTableSegment.getAliasName().orElse("")), segment);
        }
    }
}
