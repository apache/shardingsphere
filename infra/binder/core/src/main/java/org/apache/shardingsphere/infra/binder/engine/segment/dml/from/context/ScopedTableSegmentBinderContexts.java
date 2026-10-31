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

import java.util.Collection;

/**
 * Table segment binder contexts of enclosing scopes, which keep every context for unqualified lookup and the nearest scope contexts for owner lookup.
 */
public final class ScopedTableSegmentBinderContexts extends ForwardingMultimap<CaseInsensitiveString, TableSegmentBinderContext> {
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> nearestScopeTableBinderContexts = LinkedHashMultimap.create();
    
    public ScopedTableSegmentBinderContexts(final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts) {
        tableBinderContexts.putAll(outerTableBinderContexts);
        tableBinderContexts.putAll(currentTableBinderContexts);
        nearestScopeTableBinderContexts.putAll(outerTableBinderContexts instanceof ScopedTableSegmentBinderContexts
                ? ((ScopedTableSegmentBinderContexts) outerTableBinderContexts).nearestScopeTableBinderContexts
                : outerTableBinderContexts);
        for (CaseInsensitiveString each : currentTableBinderContexts.keySet()) {
            nearestScopeTableBinderContexts.replaceValues(each, currentTableBinderContexts.get(each));
        }
    }
    
    @Override
    protected Multimap<CaseInsensitiveString, TableSegmentBinderContext> delegate() {
        return tableBinderContexts;
    }
    
    /**
     * Get table segment binder contexts of the nearest scope which declares the owner.
     *
     * @param owner owner
     * @return table segment binder contexts
     */
    public Collection<TableSegmentBinderContext> getNearestScopeTableBinderContexts(final CaseInsensitiveString owner) {
        return nearestScopeTableBinderContexts.get(owner);
    }
}
