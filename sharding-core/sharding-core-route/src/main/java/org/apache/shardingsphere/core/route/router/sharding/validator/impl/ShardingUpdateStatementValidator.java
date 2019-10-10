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

package org.apache.shardingsphere.core.route.router.sharding.validator.impl;

import java.util.Collection;
import java.util.List;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.segment.table.TablesContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.route.router.sharding.validator.ShardingStatementValidator;
import org.apache.shardingsphere.core.rule.ShardingRule;

import com.google.common.base.Optional;

/**
 * Sharding update statement validator.
 *
 * @author zhangliang
 */
public final class ShardingUpdateStatementValidator implements ShardingStatementValidator<UpdateStatement> {

    private Object getShardingColumnWhereValue(final WhereSegment whereSegment, final List<Object> parameters, final String shardingColumn) {
        if (null == whereSegment.getAndPredicates() || null == parameters) {
            return null;
        }
        for (AndPredicate andPredicate : whereSegment.getAndPredicates()) {
            if (null == andPredicate.getPredicates()) {
                continue;
            }
            return getShardingColumnWhereValue(andPredicate, parameters, shardingColumn);
        }
        return null;
    }

    private Object getShardingColumnWhereValue(final AndPredicate andPredicate, final List<Object> parameters, final String shardingColumn) {
        for (PredicateSegment preSeg : andPredicate.getPredicates()) {
            if (null == preSeg) {
                return null;
            }
            // find the set segment's sharding column value
            if (!shardingColumn.equalsIgnoreCase(preSeg.getColumn().getName())) {
                continue;
            }
            PredicateRightValue rightValue = preSeg.getRightValue();
            int shardingColumnWhereIndex = -1;
            // =
            if (rightValue instanceof PredicateCompareRightValue) {
                ExpressionSegment segment = ((PredicateCompareRightValue) rightValue).getExpression();
                if (segment instanceof ParameterMarkerExpressionSegment) {
                    shardingColumnWhereIndex = ((ParameterMarkerExpressionSegment) segment).getParameterMarkerIndex();
                }
                if (segment instanceof LiteralExpressionSegment) {
                    return ((LiteralExpressionSegment) segment).getLiterals();
                }
            }
            // in
            if (rightValue instanceof PredicateInRightValue) {
                Collection<ExpressionSegment> segments = ((PredicateInRightValue) rightValue).getSqlExpressions();
                return handlePredicateInRightValue(segments, parameters, shardingColumn);
            }
            if (-1 == shardingColumnWhereIndex || shardingColumnWhereIndex > parameters.size() - 1) {
                continue;
            }
            return parameters.get(shardingColumnWhereIndex);
        }
        return null;
    }

    private Object handlePredicateInRightValue(final Collection<ExpressionSegment> segments, final List<Object> parameters, final String shardingColumn) {
        int shardingColumnWhereIndex = -1;
        for (ExpressionSegment segment : segments) {
            if (null == segment) {
                return null;
            }
            if (segment instanceof ParameterMarkerExpressionSegment) {
                shardingColumnWhereIndex = ((ParameterMarkerExpressionSegment) segment).getParameterMarkerIndex();
                if (-1 == shardingColumnWhereIndex || shardingColumnWhereIndex > parameters.size() - 1) {
                    continue;
                }
                return parameters.get(shardingColumnWhereIndex);
            }
            if (segment instanceof LiteralExpressionSegment) {
                return ((LiteralExpressionSegment) segment).getLiterals();
            }
        }
        return null;
    }

    private Object getShardingColumnSetAssignValue(final AssignmentSegment each, final List<Object> parameters) {
        if (null == each || null == each.getValue() || null == parameters) {
            return null;
        }
        ExpressionSegment segment = each.getValue();
        if (null == segment) {
            return null;
        }
        int shardingSetAssignIndex = -1;
        if (segment instanceof ParameterMarkerExpressionSegment) {
            shardingSetAssignIndex = ((ParameterMarkerExpressionSegment) segment).getParameterMarkerIndex();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) segment).getLiterals();
        }
        if (-1 == shardingSetAssignIndex || shardingSetAssignIndex > parameters.size() - 1) {
            return null;
        }
        return parameters.get(shardingSetAssignIndex);
    }

    @Override
    public void validate(final ShardingRule shardingRule, final UpdateStatement sqlStatement, final List<Object> parameters) {
        String tableName = new TablesContext(sqlStatement).getSingleTableName();
        Object shardingColumnWhereValue = null;
        for (AssignmentSegment each : sqlStatement.getSetAssignment().getAssignments()) {
            String shardingColumn = each.getColumn().getName();
            if (shardingRule.isShardingColumn(shardingColumn, tableName)) {
                // get the sharding column value in the set segment
                Object shardingColumnSetAssignValue = getShardingColumnSetAssignValue(each, parameters);
                Optional<WhereSegment> whereSegmentOptional = sqlStatement.getWhere();
                if (whereSegmentOptional.isPresent()) {
                    // get the sharding column value in the where segment
                    shardingColumnWhereValue = getShardingColumnWhereValue(whereSegmentOptional.get(), parameters, shardingColumn);
                }
                // if shardingColumnWhereValue equal to
                // shardingColumnSetAssignValue, do not judge "Can not update
                // sharding key"
                if (null != shardingColumnWhereValue && null != shardingColumnSetAssignValue
                        && shardingColumnSetAssignValue.toString().equals(shardingColumnWhereValue.toString())) {
                    continue;
                }
                throw new ShardingException("Can not update sharding key, logic table: [%s], column: [%s].", tableName, each);
            }
        }
    }
}
