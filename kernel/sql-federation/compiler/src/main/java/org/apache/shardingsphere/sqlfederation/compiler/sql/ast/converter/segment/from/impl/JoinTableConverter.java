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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Join converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JoinTableConverter {
    
    /**
     * Convert join table segment to SQL node.
     *
     * @param segment join table segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final JoinTableSegment segment) {
        SqlNode left = TableConverter.convert(segment.getLeft()).orElseThrow(IllegalStateException::new);
        SqlNode right = TableConverter.convert(segment.getRight()).orElseThrow(IllegalStateException::new);
        Optional<SqlNode> condition = convertJoinCondition(segment);
        SqlLiteral conditionType = convertConditionType(segment);
        SqlLiteral joinType = convertJoinType(segment);
        return Optional.of(new SqlJoin(SqlParserPos.ZERO, left, SqlLiteral.createBoolean(segment.isNatural(), SqlParserPos.ZERO), joinType, right, conditionType, condition.orElse(null)));
    }
    
    private static SqlLiteral convertJoinType(final JoinTableSegment segment) {
        String joinTypeName = null == segment.getJoinType() ? JoinType.INNER.name() : segment.getJoinType();
        return JoinType.INNER.name().equals(joinTypeName) && !segment.isNatural() && null == segment.getCondition() && segment.getUsing().isEmpty()
                ? JoinType.COMMA.symbol(SqlParserPos.ZERO)
                : JoinType.valueOf(joinTypeName).symbol(SqlParserPos.ZERO);
    }
    
    private static SqlLiteral convertConditionType(final JoinTableSegment segment) {
        if (!segment.getUsing().isEmpty()) {
            return JoinConditionType.USING.symbol(SqlParserPos.ZERO);
        }
        return null == segment.getCondition() ? JoinConditionType.NONE.symbol(SqlParserPos.ZERO) : JoinConditionType.ON.symbol(SqlParserPos.ZERO);
    }
    
    private static Optional<SqlNode> convertJoinCondition(final JoinTableSegment segment) {
        if (null != segment.getCondition()) {
            return ExpressionConverter.convert(segment.getCondition());
        }
        if (segment.getUsing().isEmpty()) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = segment.getUsing().stream().map(ColumnConverter::convert).collect(Collectors.toList());
        return Optional.of(new SqlNodeList(sqlNodes, SqlParserPos.ZERO));
    }
}
