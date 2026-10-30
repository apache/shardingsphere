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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.returning;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

/**
 * Returning segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReturningSegmentBinder {
    
    /**
     * Bind returning segment.
     *
     * @param segment returning segment
     * @param binderContext SQL statement binder context
     * @param boundTableSegment bound table segment
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound returning segment
     */
    @HighFrequencyInvocation
    public static ReturningSegment bind(final ReturningSegment segment, final SQLStatementBinderContext binderContext, final TableSegment boundTableSegment,
                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                        final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> returningOuterContexts =
                createReturningOuterContexts(segment, binderContext, boundTableSegment, tableBinderContexts, outerTableBinderContexts);
        ProjectionsSegment boundProjections = ProjectionsSegmentBinder.bind(segment.getProjections(), binderContext, boundTableSegment, tableBinderContexts, returningOuterContexts);
        ReturningSegment result = new ReturningSegment(segment.getStartIndex(), segment.getStopIndex(), boundProjections);
        result.setOldAlias(segment.getOldAlias());
        result.setNewAlias(segment.getNewAlias());
        return result;
    }
    
    private static Multimap<CaseInsensitiveString, TableSegmentBinderContext> createReturningOuterContexts(final ReturningSegment segment, final SQLStatementBinderContext binderContext,
                                                                                                           final TableSegment boundTableSegment,
                                                                                                           final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                                                                           final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        if (!(boundTableSegment instanceof SimpleTableSegment) || !"PostgreSQL".equals(binderContext.getSqlStatement().getDatabaseType().getType())) {
            return outerTableBinderContexts;
        }
        String targetName = boundTableSegment.getAliasName().orElseGet(() -> ((SimpleTableSegment) boundTableSegment).getTableName().getIdentifier().getValue());
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create(outerTableBinderContexts);
        CaseInsensitiveString oldName = CaseInsensitiveString.of(null == segment.getOldAlias() ? "old" : segment.getOldAlias().getIdentifier().getValue());
        CaseInsensitiveString newName = CaseInsensitiveString.of(null == segment.getNewAlias() ? "new" : segment.getNewAlias().getIdentifier().getValue());
        result.removeAll(oldName);
        result.removeAll(newName);
        result.putAll(oldName, tableBinderContexts.get(CaseInsensitiveString.of(targetName)));
        result.putAll(newName, tableBinderContexts.get(CaseInsensitiveString.of(targetName)));
        return result;
    }
}
