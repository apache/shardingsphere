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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.with;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DuplicateCommonTableExpressionAliasException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;

import java.util.stream.Collectors;

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
     * @param recursive recursive
     * @return bound common table expression segment
     */
    public static CommonTableExpressionSegment bind(final CommonTableExpressionSegment segment, final SQLStatementBinderContext binderContext, final boolean recursive) {
        if (segment.getAliasName().isPresent()) {
            ShardingSpherePreconditions.checkState(!binderContext.getCommonTableExpressionsSegmentsUniqueAliases().contains(segment.getAliasName().get()),
                    () -> new DuplicateCommonTableExpressionAliasException(segment.getAliasName().get()));
            binderContext.getCommonTableExpressionsSegmentsUniqueAliases().add(segment.getAliasName().get());
        }
        if (recursive && segment.getAliasName().isPresent()) {
            binderContext.getExternalTableBinderContexts().put(new CaseInsensitiveString(segment.getAliasName().get()),
                    new SimpleTableSegmentBinderContext(segment.getColumns().stream().map(ColumnProjectionSegment::new).collect(Collectors.toList())));
        }
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getSubquery());
        subqueryTableSegment.setAlias(segment.getAliasSegment());
        SubqueryTableSegment boundSubquerySegment =
                SubqueryTableSegmentBinder.bind(subqueryTableSegment, binderContext, LinkedHashMultimap.create(), binderContext.getExternalTableBinderContexts());
        CommonTableExpressionSegment result = new CommonTableExpressionSegment(
                segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment.getAliasSegment().orElse(null), boundSubquerySegment.getSubquery());
        // TODO bind with columns
        result.getColumns().addAll(segment.getColumns());
        return result;
    }
}
