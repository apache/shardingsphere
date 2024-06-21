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

package org.apache.shardingsphere.infra.binder.segment.projection.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;

import java.util.Map;

/**
 * Subquery projection segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryProjectionSegmentBinder {
    
    /**
     * Bind subquery projection segment with metadata.
     *
     * @param segment subquery projection segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bounded subquery projection segment
     */
    public static SubqueryProjectionSegment bind(final SubqueryProjectionSegment segment, final SQLStatementBinderContext statementBinderContext,
                                                 final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        SubquerySegment boundedSubquerySegment = SubquerySegmentBinder.bind(segment.getSubquery(), statementBinderContext, tableBinderContexts);
        SubqueryProjectionSegment result = new SubqueryProjectionSegment(boundedSubquerySegment, segment.getText());
        segment.getAliasSegment().ifPresent(result::setAlias);
        return result;
    }
}
