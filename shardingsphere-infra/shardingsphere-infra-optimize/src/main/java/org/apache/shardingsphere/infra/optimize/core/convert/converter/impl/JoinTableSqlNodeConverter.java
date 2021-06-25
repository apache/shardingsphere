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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;

import java.util.Optional;

/**
 * Join converter.
 */
public final class JoinTableSqlNodeConverter implements SqlNodeConverter<JoinTableSegment, SqlNode> {

    private static final String JOIN_TYPE_INNER = "INNER";

    private static final String JOIN_TYPE_LEFT = "LEFT";

    private static final String JOIN_TYPE_RIGHT = "RIGHT";

    private static final String JOIN_TYPE_FULL = "FULL";
    
    @Override
    public Optional<SqlNode> convert(final JoinTableSegment join) {
        SqlNode left = new TableSqlNodeConverter().convert(join.getLeft()).get();
        SqlNode right = new TableSqlNodeConverter().convert(join.getRight()).get();
        ExpressionSegment expressionSegment = join.getCondition();
        Optional<SqlNode> condition = new ExpressionSqlNodeConverter().convert(expressionSegment);
        SqlLiteral conditionType = condition.isPresent() ? JoinConditionType.NONE.symbol(SqlParserPos.ZERO)
                : JoinConditionType.ON.symbol(SqlParserPos.ZERO);
        SqlLiteral joinTypeSqlNode = convertJoinType(join.getJoinType());
        SqlNode sqlNode = new SqlJoin(SqlParserPos.ZERO, left,
                SqlLiteral.createBoolean(false, SqlParserPos.ZERO),
                joinTypeSqlNode, right, conditionType, condition.orElse(null));
        return Optional.of(sqlNode);
    }
    
    private SqlLiteral convertJoinType(final String joinType) {
        if (joinType == null) {
            return JoinType.COMMA.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_INNER.equals(joinType)) {
            return JoinType.INNER.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_LEFT.equals(joinType)) {
            return JoinType.LEFT.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_RIGHT.equals(joinType)) {
            return JoinType.RIGHT.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_FULL.equals(joinType)) {
            return JoinType.FULL.symbol(SqlParserPos.ZERO);
        } else {
            throw new UnsupportedOperationException("unsupported join type " + joinType);
        }
    }
}
