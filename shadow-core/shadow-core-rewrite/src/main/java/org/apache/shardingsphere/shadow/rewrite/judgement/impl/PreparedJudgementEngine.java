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

package org.apache.shardingsphere.shadow.rewrite.judgement.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shadow.rewrite.judgement.ShadowJudgementEngine;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.WhereSegmentAvailable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Shadow judgement engine for prepared.
 *
 * @author zhyee
 */
@RequiredArgsConstructor
public final class PreparedJudgementEngine implements ShadowJudgementEngine {
    
    private final ShadowRule shadowRule;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final List<Object> parameters;
    
    @Override
    public boolean isShadowSQL() {
        if (sqlStatementContext.getSqlStatement() instanceof InsertStatement) {
            LinkedList<ColumnSegment> columnSegments = (LinkedList<ColumnSegment>) ((InsertStatement) sqlStatementContext.getSqlStatement()).getColumns();
            for (int i = 0; i < columnSegments.size(); i++) {
                if (columnSegments.get(i).getName().equals(shadowRule.getColumn())) {
                    final Object value = parameters.get(i);
                    return value instanceof Boolean && (Boolean) value;
                }
            }
            return false;
        }
        if (sqlStatementContext.getSqlStatement() instanceof WhereSegmentAvailable) {
            Optional<WhereSegment> whereSegment = ((WhereSegmentAvailable) sqlStatementContext.getSqlStatement()).getWhere();
            if (!whereSegment.isPresent()) {
                return false;
            }
            Collection<AndPredicate> andPredicates = whereSegment.get().getAndPredicates();
            for (AndPredicate andPredicate : andPredicates) {
                if (judgePredicateSegments(andPredicate.getPredicates())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean judgePredicateSegments(final Collection<PredicateSegment> predicates) {
        for (PredicateSegment each : predicates) {
            if (each.getColumn().getName().equals(shadowRule.getColumn())) {
                Preconditions.checkArgument(each.getRightValue() instanceof PredicateCompareRightValue, "must be PredicateCompareRightValue");
                PredicateCompareRightValue rightValue = (PredicateCompareRightValue) each.getRightValue();
                int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) rightValue.getExpression()).getParameterMarkerIndex();
                final Object value = parameters.get(parameterMarkerIndex);
                return value instanceof Boolean && (Boolean) value;
            }
        }
        return false;
    }
}
