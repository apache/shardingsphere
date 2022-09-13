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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.impl;

import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Join converter.
 */
public final class JoinTableConverter implements SQLSegmentConverter<JoinTableSegment, SqlJoin> {
    
    @Override
    public Optional<SqlJoin> convert(final JoinTableSegment segment) {
        SqlNode left = new TableConverter().convert(segment.getLeft()).orElseThrow(IllegalStateException::new);
        SqlNode right = new TableConverter().convert(segment.getRight()).orElseThrow(IllegalStateException::new);
        Optional<SqlNode> condition = convertJoinCondition(segment);
        SqlLiteral conditionType = convertConditionType(segment);
        SqlLiteral joinType = JoinType.valueOf(segment.getJoinType()).symbol(SqlParserPos.ZERO);
        return Optional.of(new SqlJoin(SqlParserPos.ZERO, left, SqlLiteral.createBoolean(false, SqlParserPos.ZERO), joinType, right, conditionType, condition.orElse(null)));
    }
    
    private static SqlLiteral convertConditionType(final JoinTableSegment segment) {
        if (!segment.getUsing().isEmpty()) {
            return JoinConditionType.USING.symbol(SqlParserPos.ZERO);
        }
        return null != segment.getCondition() ? JoinConditionType.ON.symbol(SqlParserPos.ZERO) : JoinConditionType.NONE.symbol(SqlParserPos.ZERO);
    }
    
    private static Optional<SqlNode> convertJoinCondition(final JoinTableSegment segment) {
        if (null != segment.getCondition()) {
            return new ExpressionConverter().convert(segment.getCondition());
        }
        if (!segment.getUsing().isEmpty()) {
            Collection<SqlNode> sqlNodes = new LinkedList<>();
            ColumnConverter columnConverter = new ColumnConverter();
            for (ColumnSegment each : segment.getUsing()) {
                columnConverter.convert(each).ifPresent(sqlNodes::add);
            }
            return Optional.of(new SqlNodeList(sqlNodes, SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
}
