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

package org.apache.shardingsphere.infra.binder.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.BinaryOperationExpressionBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.ExistsSubqueryExpressionBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.FunctionExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.InExpressionBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.NotExpressionBinder;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.SubqueryExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expression segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionSegmentBinder {
    
    /**
     * Bind expression segment with metadata.
     *
     * @param segment expression segment
     * @param parentSegmentType parent segment type
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded expression segment
     */
    public static ExpressionSegment bind(final ExpressionSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext statementBinderContext,
                                         final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment instanceof BinaryOperationExpression) {
            return BinaryOperationExpressionBinder.bind((BinaryOperationExpression) segment, parentSegmentType, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof ExistsSubqueryExpression) {
            return ExistsSubqueryExpressionBinder.bind((ExistsSubqueryExpression) segment, statementBinderContext, tableBinderContexts);
        }
        if (segment instanceof SubqueryExpressionSegment) {
            Map<String, TableSegmentBinderContext> newOuterTableBinderContexts = new LinkedHashMap<>();
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return SubqueryExpressionSegmentBinder.bind((SubqueryExpressionSegment) segment, statementBinderContext, newOuterTableBinderContexts);
        }
        if (segment instanceof InExpression) {
            return InExpressionBinder.bind((InExpression) segment, parentSegmentType, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof NotExpression) {
            return NotExpressionBinder.bind((NotExpression) segment, parentSegmentType, statementBinderContext, tableBinderContexts);
        }
        if (segment instanceof ColumnSegment) {
            return ColumnSegmentBinder.bind((ColumnSegment) segment, parentSegmentType, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof FunctionSegment) {
            return FunctionExpressionSegmentBinder.bind((FunctionSegment) segment, parentSegmentType, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        // TODO support more ExpressionSegment bind
        return segment;
    }
}
