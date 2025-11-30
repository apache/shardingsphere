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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.AggregationDistinctProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.AggregationProjectionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.BetweenExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.BinaryOperationExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.CaseWhenExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ExistsSubqueryExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.FunctionExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.InExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.NotExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.OuterJoinExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.QuantifySubqueryExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.RowExpressionBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.SubquerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;

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
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment instanceof BinaryOperationExpression) {
            return BinaryOperationExpressionBinder.bind((BinaryOperationExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof ExistsSubqueryExpression) {
            Multimap<CaseInsensitiveString, TableSegmentBinderContext> newOuterTableBinderContexts = LinkedHashMultimap.create();
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return ExistsSubqueryExpressionBinder.bind((ExistsSubqueryExpression) segment, binderContext, newOuterTableBinderContexts);
        }
        if (segment instanceof SubqueryExpressionSegment) {
            Multimap<CaseInsensitiveString, TableSegmentBinderContext> newOuterTableBinderContexts = LinkedHashMultimap.create();
            newOuterTableBinderContexts.putAll(outerTableBinderContexts);
            newOuterTableBinderContexts.putAll(tableBinderContexts);
            return new SubqueryExpressionSegment(SubquerySegmentBinder.bind(((SubqueryExpressionSegment) segment).getSubquery(), binderContext, newOuterTableBinderContexts));
        }
        if (segment instanceof InExpression) {
            return InExpressionBinder.bind((InExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof NotExpression) {
            return NotExpressionBinder.bind((NotExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof ColumnSegment) {
            return ColumnSegmentBinder.bind((ColumnSegment) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof FunctionSegment) {
            return FunctionExpressionSegmentBinder.bind((FunctionSegment) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof BetweenExpression) {
            return BetweenExpressionSegmentBinder.bind((BetweenExpression) segment, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof AggregationDistinctProjectionSegment) {
            return AggregationDistinctProjectionSegmentBinder.bind((AggregationDistinctProjectionSegment) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof AggregationProjectionSegment) {
            return AggregationProjectionSegmentBinder.bind((AggregationProjectionSegment) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof CaseWhenExpression) {
            return CaseWhenExpressionBinder.bind((CaseWhenExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof OuterJoinExpression) {
            return OuterJoinExpressionBinder.bind((OuterJoinExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof QuantifySubqueryExpression) {
            return QuantifySubqueryExpressionBinder.bind((QuantifySubqueryExpression) segment, binderContext, tableBinderContexts);
        }
        if (segment instanceof RowExpression) {
            return RowExpressionBinder.bind((RowExpression) segment, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        // TODO support more ExpressionSegment bound
        return segment;
    }
}
