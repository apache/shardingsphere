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

package org.apache.shardingsphere.infra.binder.engine.segment.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

/**
 * Returning projection binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReturningSegmentBinder {
    
    /**
     * Bind returning projections in the visible table scope.
     *
     * @param segment returning segment
     * @param binderContext statement binder context
     * @param boundTable visible table scope
     * @param tableContexts visible table contexts
     * @param outerContexts outer table contexts
     * @return bound returning segment
     */
    public static ReturningSegment bind(final ReturningSegment segment, final SQLStatementBinderContext binderContext, final TableSegment boundTable,
                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts,
                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerContexts) {
        TableSegment scope = boundTable instanceof DeleteMultiTableSegment ? ((DeleteMultiTableSegment) boundTable).getRelationTable() : boundTable;
        ReturningSegment result = new ReturningSegment(segment.getStartIndex(), segment.getStopIndex(),
                ProjectionsSegmentBinder.bind(segment.getProjections(), binderContext, scope, getReturningTableContexts(segment, scope, tableContexts), outerContexts));
        result.setOldAlias(segment.getOldAlias());
        result.setNewAlias(segment.getNewAlias());
        return result;
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> getReturningTableContexts(final ReturningSegment segment, final TableSegment table,
                                                                                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableContexts) {
        if (!(table instanceof SimpleTableSegment)) {
            return tableContexts;
        }
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = tableContexts;
        for (ProjectionSegment each : segment.getProjections().getProjections()) {
            if (!(each instanceof ShorthandProjectionSegment) || !((ShorthandProjectionSegment) each).getOwner().isPresent()) {
                continue;
            }
            String owner = ((ShorthandProjectionSegment) each).getOwner().get().getIdentifier().getValue();
            if (!isReturningRowAlias(owner, segment) || result.containsKey(CaseInsensitiveString.of(owner))) {
                continue;
            }
            if (result == tableContexts) {
                result = LinkedHashMultimap.create(tableContexts);
            }
            SimpleTableSegment target = (SimpleTableSegment) table;
            String targetName = target.getAliasName().orElseGet(() -> target.getTableName().getIdentifier().getValue());
            result.putAll(CaseInsensitiveString.of(owner), tableContexts.get(CaseInsensitiveString.of(targetName)));
        }
        return result;
    }
    
    private static boolean isReturningRowAlias(final String owner, final ReturningSegment segment) {
        return owner.equalsIgnoreCase(null == segment.getOldAlias() ? "OLD" : segment.getOldAlias().getIdentifier().getValue())
                || owner.equalsIgnoreCase(null == segment.getNewAlias() ? "NEW" : segment.getNewAlias().getIdentifier().getValue());
    }
}
