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

package org.apache.shardingsphere.infra.binder.engine.segment.expression.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Function expression binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionExpressionSegmentBinder {
    
    /**
     * Bind function expression.
     *
     * @param segment function expression segment
     * @param parentSegmentType parent segment type
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound function segment
     */
    public static FunctionSegment bind(final FunctionSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext,
                                       final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        FunctionSegment result = new FunctionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getFunctionName(), segment.getText());
        result.setOwner(segment.getOwner());
        result.getParameters().addAll(segment.getParameters().stream()
                .map(each -> ExpressionSegmentBinder.bind(each, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts)).collect(Collectors.toList()));
        return result;
    }
}
