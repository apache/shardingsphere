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

package org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlPositionFunction;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Function converter.
 */
public class FunctionConverter implements SQLSegmentConverter<FunctionSegment, SqlBasicCall> {
    
    @Override
    public Optional<SqlBasicCall> convertToSQLNode(final FunctionSegment segment) {
        if ("POSITION".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlPositionFunction(), getPositionSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<FunctionSegment> convertToSQLSegment(final SqlBasicCall sqlNode) {
        if (null == sqlNode) {
            return Optional.empty();
        }
        return Optional.of(new FunctionSegment(getStartIndex(sqlNode), getStopIndex(sqlNode), sqlNode.getOperator().getName(), sqlNode.toString()));
    }
    
    private SqlNode[] getPositionSqlNodes(final Collection<ExpressionSegment> expressionSegments) {
        List<SqlNode> sqlNodes = new ArrayList<>();
        expressionSegments.forEach(expressionSegment -> {
            if (expressionSegment instanceof LiteralExpressionSegment) {
                sqlNodes.add(SqlLiteral.createCharString(((LiteralExpressionSegment) expressionSegment).getLiterals().toString(), SqlParserPos.ZERO));
            }
        });
        return sqlNodes.toArray(new SqlNode[0]);
    }
}
