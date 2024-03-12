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

package org.apache.shardingsphere.infra.binder.segment.with;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * With segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithSegmentBinder {
    
    /**
     * Bind with segment with metadata.
     *
     * @param segment with segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param externalTableBinderContexts external table binder contexts
     * @return bounded with segment
     */
    public static WithSegment bind(final WithSegment segment, final SQLStatementBinderContext statementBinderContext, final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                   final Map<String, TableSegmentBinderContext> externalTableBinderContexts) {
        Collection<CommonTableExpressionSegment> boundedCommonTableExpressions = new LinkedList<>();
        for (CommonTableExpressionSegment each : segment.getCommonTableExpressions()) {
            CommonTableExpressionSegment boundedCommonTableExpression = CommonTableExpressionSegmentBinder.bind(each, statementBinderContext, tableBinderContexts);
            boundedCommonTableExpressions.add(boundedCommonTableExpression);
            externalTableBinderContexts.put(each.getIdentifier().getValue(),
                    new SimpleTableSegmentBinderContext(boundedCommonTableExpression.getSubquery().getSelect().getProjections().getProjections()));
        }
        return new WithSegment(segment.getStartIndex(), segment.getStopIndex(), boundedCommonTableExpressions);
    }
}
