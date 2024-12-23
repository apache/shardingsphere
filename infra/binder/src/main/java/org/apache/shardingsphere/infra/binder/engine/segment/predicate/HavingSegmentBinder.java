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

package org.apache.shardingsphere.infra.binder.engine.segment.predicate;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;

/**
 * Having segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HavingSegmentBinder {
    
    /**
     * Bind having segment.
     *
     * @param segment having segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound having segment
     */
    public static HavingSegment bind(final HavingSegment segment, final SQLStatementBinderContext binderContext,
                                     final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                     final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        return new HavingSegment(segment.getStartIndex(), segment.getStopIndex(),
                ExpressionSegmentBinder.bind(segment.getExpr(), SegmentType.PREDICATE, binderContext, tableBinderContexts, outerTableBinderContexts));
    }
}
