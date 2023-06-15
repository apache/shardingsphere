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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.fun.SqlCase;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Case when expression converter.
 */
public final class CaseWhenExpressionConverter implements SQLSegmentConverter<CaseWhenExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final CaseWhenExpression segment) {
        Collection<SqlNode> whenExprs = convertWhenExprs(segment.getCaseExpr(), segment.getWhenExprs());
        Collection<SqlNode> thenExprs = new LinkedList<>();
        segment.getThenExprs().forEach(each -> new ExpressionConverter().convert(each).ifPresent(thenExprs::add));
        Optional<SqlNode> elseExpr = new ExpressionConverter().convert(segment.getElseExpr());
        return Optional.of(new SqlCase(SqlParserPos.ZERO, null, new SqlNodeList(whenExprs, SqlParserPos.ZERO), new SqlNodeList(thenExprs, SqlParserPos.ZERO),
                elseExpr.orElseGet(() -> SqlLiteral.createCharString("NULL", SqlParserPos.ZERO))));
    }
    
    private Collection<SqlNode> convertWhenExprs(final ExpressionSegment caseExpr, final Collection<ExpressionSegment> whenExprs) {
        Collection<SqlNode> result = new LinkedList<>();
        for (ExpressionSegment each : whenExprs) {
            if (null != caseExpr) {
                convertCaseExpr(caseExpr, each).ifPresent(result::add);
            } else {
                new ExpressionConverter().convert(each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<SqlNode> convertCaseExpr(final ExpressionSegment caseExpr, final ExpressionSegment whenExpr) {
        Optional<SqlNode> leftExpr = new ExpressionConverter().convert(caseExpr);
        Optional<SqlNode> rightExpr = new ExpressionConverter().convert(whenExpr);
        if (leftExpr.isPresent() && rightExpr.isPresent()) {
            return new ExpressionConverter().convert(whenExpr).map(optional -> new SqlBasicCall(SqlStdOperatorTable.EQUALS, Arrays.asList(leftExpr.get(), rightExpr.get()), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
}
