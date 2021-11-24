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

package org.apache.shardingsphere.infra.federation.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlPositionFunction;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Function converter.
 */
public final class FunctionConverter implements SQLSegmentConverter<FunctionSegment, SqlBasicCall> {
    
    @Override
    public Optional<SqlBasicCall> convertToSQLNode(final FunctionSegment segment) {
        if ("POSITION".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlPositionFunction(), getPositionSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<FunctionSegment> convertToSQLSegment(final SqlBasicCall sqlBasicCall) {
        if (null == sqlBasicCall) {
            return Optional.empty();
        }
        FunctionSegment functionSegment = new FunctionSegment(getStartIndex(sqlBasicCall), getStopIndex(sqlBasicCall), sqlBasicCall.getOperator().getName(), sqlBasicCall.toString());
        functionSegment.getParameters().addAll(getParameters(sqlBasicCall));
        return Optional.of(functionSegment);
    }
    
    private List<ExpressionSegment> getParameters(final SqlBasicCall sqlBasicCall) {
        return sqlBasicCall.getOperandList().stream()
                .map(operand -> new LiteralExpressionSegment(getStartIndex(operand), getStopIndex(operand), operand.toString().replace("'", ""))).collect(Collectors.toList());
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
