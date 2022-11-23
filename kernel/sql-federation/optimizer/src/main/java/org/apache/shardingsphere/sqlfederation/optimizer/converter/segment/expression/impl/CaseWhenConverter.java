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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.fun.SqlCase;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CaseWhenSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Case when converter.
 */
public final class CaseWhenConverter implements SQLSegmentConverter<CaseWhenSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final CaseWhenSegment segment) {
        Collection<SqlNode> whenList = convertWhenList(segment.getCaseArg(), segment.getWhenList());
        Collection<SqlNode> thenList = new LinkedList<>();
        segment.getThenList().forEach(each -> new ExpressionConverter().convert(each).ifPresent(thenList::add));
        Optional<SqlNode> elseExpr = new ExpressionConverter().convert(segment.getElseExpression());
        return Optional.of(new SqlCase(SqlParserPos.ZERO, null, new SqlNodeList(whenList, SqlParserPos.ZERO), new SqlNodeList(thenList, SqlParserPos.ZERO),
                elseExpr.orElseGet(() -> SqlLiteral.createCharString("NULL", SqlParserPos.ZERO))));
    }
    
    private Collection<SqlNode> convertWhenList(final ExpressionSegment caseArg, final Collection<ExpressionSegment> whenList) {
        Collection<SqlNode> result = new LinkedList<>();
        for (ExpressionSegment each : whenList) {
            if (null != caseArg) {
                convertWithCaseArg(caseArg, each, result);
            } else {
                new ExpressionConverter().convert(each).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private void convertWithCaseArg(final ExpressionSegment caseArg, final ExpressionSegment expressionSegment, final Collection<SqlNode> result) {
        Optional<SqlNode> leftExpr = new ExpressionConverter().convert(caseArg);
        Optional<SqlNode> rightExpr = new ExpressionConverter().convert(expressionSegment);
        if (leftExpr.isPresent() && rightExpr.isPresent()) {
            new ExpressionConverter().convert(expressionSegment).ifPresent(optional -> result.add(
                    new SqlBasicCall(SqlStdOperatorTable.EQUALS, Arrays.asList(leftExpr.get(), rightExpr.get()), SqlParserPos.ZERO)));
        }
    }
}
