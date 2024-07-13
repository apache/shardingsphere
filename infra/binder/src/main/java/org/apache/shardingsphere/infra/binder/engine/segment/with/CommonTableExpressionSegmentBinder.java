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

package org.apache.shardingsphere.infra.binder.engine.segment.with;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;

import java.util.Map;

/**
 * Common table expression segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonTableExpressionSegmentBinder {
    
    /**
     * Bind common table expression segment.
     *
     * @param segment common table expression segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bound common table expression segment
     */
    public static CommonTableExpressionSegment bind(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext,
                                                    final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        SubquerySegment boundSubquerySegment = SubquerySegmentBinder.bind(segment.getSubquery(), binderContext, tableBinderContexts);
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getAliasSegment(), boundSubquerySegment);
        // TODO bind with columns
        result.getColumns().addAll(segment.getColumns());
        return result;
    }
}
