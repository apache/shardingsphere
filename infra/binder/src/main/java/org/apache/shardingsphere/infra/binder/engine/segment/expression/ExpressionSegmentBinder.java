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

package org.apache.shardingsphere.infra.binder.engine.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.BinaryOperationExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.ExistsSubqueryExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.FunctionExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.InExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.NotExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expression segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionSegmentBinder {
    
    /**
     * Bind expression segment.
     *
     * @param segment expression segment
     * @param parentSegmentType parent segment type
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound expression segment
     */
    public static ExpressionSegment bind(final ExpressionSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext,
                                         final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment instanceof BinaryOperationExpression) {
            return BinaryOperationExpressionBinder.bind((BinaryOperationExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof ExistsSubqueryExpression) {
            return ExistsSubqueryExpressionBinder.bind((ExistsSubqueryExpression) segment, binderContext, tableBinderContexts);
        }
        if (segment instanceof SubqueryExpressionSegment) {
            Map<String, TableSegmentBinderContext> newOuterTableBinderContexts = new LinkedHashMap<>();
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return new SubqueryExpressionSegment(SubquerySegmentBinder.bind(((SubqueryExpressionSegment) segment).getSubquery(), binderContext, newOuterTableBinderContexts));
        }
        if (segment instanceof InExpression) {
            return InExpressionBinder.bind((InExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof NotExpression) {
            return NotExpressionBinder.bind((NotExpression) segment, parentSegmentType, binderContext, tableBinderContexts);
        }
        if (segment instanceof ColumnSegment) {
            return ColumnSegmentBinder.bind((ColumnSegment) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof FunctionSegment) {
            return FunctionExpressionSegmentBinder.bind((FunctionSegment) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        // TODO support more ExpressionSegment bound
        return segment;
    }
}
