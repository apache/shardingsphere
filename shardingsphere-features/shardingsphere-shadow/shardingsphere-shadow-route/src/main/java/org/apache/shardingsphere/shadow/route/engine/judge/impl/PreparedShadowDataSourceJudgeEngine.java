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

package org.apache.shardingsphere.shadow.route.engine.judge.impl;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.route.engine.judge.util.ShadowValueJudgeUtil;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.route.engine.judge.ShadowDataSourceJudgeEngine;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.value.PredicateCompareRightValue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Prepared shadow data source judge engine.
 */
@RequiredArgsConstructor
public final class PreparedShadowDataSourceJudgeEngine implements ShadowDataSourceJudgeEngine {
    
    private final ShadowRule shadowRule;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final List<Object> parameters;
    
    @Override
    public boolean isShadow() {
        if (sqlStatementContext instanceof InsertStatementContext) {
            Collection<ColumnSegment> columnSegments = (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getColumns();
            int count = 0;
            for (ColumnSegment each : columnSegments) {
                if (each.getIdentifier().getValue().equals(shadowRule.getColumn())) {
                    return ShadowValueJudgeUtil.isShadowValue(parameters.get(count));
                }
                count++;
            }
            return false;
        }
        if (sqlStatementContext instanceof WhereAvailable) {
            Optional<WhereSegment> whereSegment = ((WhereAvailable) sqlStatementContext).getWhere();
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
            if (each.getColumn().getIdentifier().getValue().equals(shadowRule.getColumn())) {
                Preconditions.checkArgument(each.getRightValue() instanceof PredicateCompareRightValue, "must be PredicateCompareRightValue");
                PredicateCompareRightValue rightValue = (PredicateCompareRightValue) each.getRightValue();
                int parameterMarkerIndex = ((ParameterMarkerExpressionSegment) rightValue.getExpression()).getParameterMarkerIndex();
                return ShadowValueJudgeUtil.isShadowValue(parameters.get(parameterMarkerIndex));
            }
        }
        return false;
    }
}
