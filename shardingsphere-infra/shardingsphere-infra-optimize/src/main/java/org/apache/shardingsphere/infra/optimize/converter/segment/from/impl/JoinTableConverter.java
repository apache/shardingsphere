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

package org.apache.shardingsphere.infra.optimize.converter.segment.from.impl;

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Optional;

/**
 * Join converter.
 */
public final class JoinTableConverter implements SQLSegmentConverter<JoinTableSegment, SqlNode> {
    
    private static final String JOIN_TYPE_INNER = "INNER";
    
    private static final String JOIN_TYPE_LEFT = "LEFT";
    
    private static final String JOIN_TYPE_RIGHT = "RIGHT";
    
    private static final String JOIN_TYPE_FULL = "FULL";
    
    @Override
    public Optional<SqlNode> convert(final JoinTableSegment segment) {
        SqlNode left = convertJoinedTable(segment.getLeft());
        SqlNode right = convertJoinedTable(segment.getRight());
        Optional<SqlNode> condition = new ExpressionConverter().convert(segment.getCondition());
        SqlLiteral conditionType = condition.isPresent() ? JoinConditionType.ON.symbol(SqlParserPos.ZERO) : JoinConditionType.NONE.symbol(SqlParserPos.ZERO);
        return Optional.of(
                new SqlJoin(SqlParserPos.ZERO, left, SqlLiteral.createBoolean(false, SqlParserPos.ZERO), convertJoinType(segment.getJoinType()), right, conditionType, condition.orElse(null)));
    }
    
    private SqlNode convertJoinedTable(final TableSegment segment) {
        Optional<SqlNode> result = new TableConverter().convert(segment);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
    
    private SqlLiteral convertJoinType(final String joinType) {
        if (null == joinType) {
            return JoinType.COMMA.symbol(SqlParserPos.ZERO);
        }
        if (JOIN_TYPE_INNER.equals(joinType)) {
            return JoinType.INNER.symbol(SqlParserPos.ZERO);
        }
        if (JOIN_TYPE_LEFT.equals(joinType)) {
            return JoinType.LEFT.symbol(SqlParserPos.ZERO);
        }
        if (JOIN_TYPE_RIGHT.equals(joinType)) {
            return JoinType.RIGHT.symbol(SqlParserPos.ZERO);
        }
        if (JOIN_TYPE_FULL.equals(joinType)) {
            return JoinType.FULL.symbol(SqlParserPos.ZERO);
        }
        throw new UnsupportedOperationException("unsupported join type " + joinType);
    }
}
