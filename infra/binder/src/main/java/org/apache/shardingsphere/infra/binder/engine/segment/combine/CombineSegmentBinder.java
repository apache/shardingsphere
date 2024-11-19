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

package org.apache.shardingsphere.infra.binder.engine.segment.combine;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;

/**
 * Combine segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CombineSegmentBinder {
    
    /**
     * Bind combine segment.
     *
     * @param segment table segment
     * @param binderContext SQL statement binder context
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound combine segment
     */
    public static CombineSegment bind(final CombineSegment segment, final SQLStatementBinderContext binderContext,
                                      final Multimap<CaseInsensitiveMap.CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        return new CombineSegment(segment.getStartIndex(), segment.getStopIndex(),
                bindSubquerySegment(segment.getLeft(), binderContext, outerTableBinderContexts), segment.getCombineType(),
                bindSubquerySegment(segment.getRight(), binderContext, outerTableBinderContexts));
    }
    
    private static SubquerySegment bindSubquerySegment(final SubquerySegment segment, final SQLStatementBinderContext binderContext,
                                                       final Multimap<CaseInsensitiveMap.CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        SubquerySegment result = new SubquerySegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText());
        SQLStatementBinderContext subqueryBinderContext = new SQLStatementBinderContext(segment.getSelect(), binderContext.getMetaData(), binderContext.getCurrentDatabaseName());
        subqueryBinderContext.getExternalTableBinderContexts().putAll(binderContext.getExternalTableBinderContexts());
        result.setSelect(new SelectStatementBinder(outerTableBinderContexts).bind(segment.getSelect(), subqueryBinderContext));
        return result;
    }
}
