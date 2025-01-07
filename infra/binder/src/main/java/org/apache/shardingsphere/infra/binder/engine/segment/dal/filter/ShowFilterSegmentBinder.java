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

package org.apache.shardingsphere.infra.binder.engine.segment.dal.filter;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;

/**
 * Show filter segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowFilterSegmentBinder {
    
    /**
     * Bind show filter segment.
     *
     * @param segment show filter segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound show filter segment
     */
    public static ShowFilterSegment bind(final ShowFilterSegment segment, final SQLStatementBinderContext binderContext,
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        ShowFilterSegment result = new ShowFilterSegment(segment.getStartIndex(), segment.getStopIndex());
        segment.getLike().ifPresent(result::setLike);
        segment.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)));
        return result;
    }
}
