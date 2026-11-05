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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Table segment binder contexts of enclosing scopes, which keep every context for unqualified lookup and the scope of each context for owner lookup.
 */
@HighFrequencyInvocation
public final class ScopedTableSegmentBinderContexts extends ForwardingMultimap<CaseInsensitiveString, TableSegmentBinderContext> {
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
    
    private final List<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> scopeTableBinderContexts;
    
    public ScopedTableSegmentBinderContexts(final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts) {
        tableBinderContexts.putAll(outerTableBinderContexts);
        tableBinderContexts.putAll(currentTableBinderContexts);
        Collection<Multimap<CaseInsensitiveString, TableSegmentBinderContext>> outerScopeTableBinderContexts = outerTableBinderContexts instanceof ScopedTableSegmentBinderContexts
                ? ((ScopedTableSegmentBinderContexts) outerTableBinderContexts).scopeTableBinderContexts
                : Collections.singletonList(outerTableBinderContexts);
        scopeTableBinderContexts = new ArrayList<>(outerScopeTableBinderContexts.size() + 1);
        scopeTableBinderContexts.addAll(outerScopeTableBinderContexts);
        scopeTableBinderContexts.add(currentTableBinderContexts);
    }
    
    @Override
    protected Multimap<CaseInsensitiveString, TableSegmentBinderContext> delegate() {
        return tableBinderContexts;
    }
    
    /**
     * Get table segment binder contexts of the nearest scope which declares the owner.
     *
     * <p>When the owner is qualified, only a scope which declares the owner table in the qualified database or schema is taken into account.</p>
     *
     * @param owner owner
     * @return table segment binder contexts
     */
    public Collection<TableSegmentBinderContext> getNearestScopeTableBinderContexts(final OwnerSegment owner) {
        CaseInsensitiveString tableName = CaseInsensitiveString.of(owner.getIdentifier().getValue());
        for (int i = scopeTableBinderContexts.size() - 1; i >= 0; i--) {
            Collection<TableSegmentBinderContext> result = scopeTableBinderContexts.get(i).get(tableName);
            if (!result.isEmpty() && isDeclaredBy(result, owner)) {
                return result;
            }
        }
        return Collections.emptyList();
    }
    
    private boolean isDeclaredBy(final Collection<TableSegmentBinderContext> ownerTableBinderContexts, final OwnerSegment owner) {
        if (!owner.getOwner().isPresent()) {
            return true;
        }
        String qualifier = owner.getOwner().get().getIdentifier().getValue();
        for (TableSegmentBinderContext each : ownerTableBinderContexts) {
            if (isQualifiedBy(each, qualifier)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isQualifiedBy(final TableSegmentBinderContext tableBinderContext, final String qualifier) {
        for (ProjectionSegment each : tableBinderContext.getProjectionSegments()) {
            if (each instanceof ColumnProjectionSegment) {
                ColumnSegmentBoundInfo columnBoundInfo = ((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo();
                return qualifier.equalsIgnoreCase(columnBoundInfo.getOriginalDatabase().getValue()) || qualifier.equalsIgnoreCase(columnBoundInfo.getOriginalSchema().getValue());
            }
        }
        return false;
    }
}
