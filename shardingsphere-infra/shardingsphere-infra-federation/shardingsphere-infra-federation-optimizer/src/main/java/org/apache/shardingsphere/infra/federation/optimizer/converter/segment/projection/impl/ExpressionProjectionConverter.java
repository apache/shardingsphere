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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.projection.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNumericLiteral;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collections;
import java.util.Optional;

/**
 * Expression projection converter.
 */
public final class ExpressionProjectionConverter implements SQLSegmentConverter<ExpressionProjectionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final ExpressionProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        Optional<SqlNode> exprSqlNode = new ExpressionConverter().convertToSQLNode(segment.getExpr());
        if (exprSqlNode.isPresent() && segment.getAlias().isPresent()) {
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, new SqlNode[]{exprSqlNode.get(),
                    SqlIdentifier.star(Collections.singletonList(segment.getAlias().get()), SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))}, SqlParserPos.ZERO));
        }
        return exprSqlNode;
    }
    
    @Override
    public Optional<ExpressionProjectionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlBasicCall) {
            SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlNode;
            if (SqlKind.AS == sqlBasicCall.getOperator().getKind() && sqlBasicCall.getOperandList().get(0) instanceof SqlNumericLiteral) {
                SqlNode exprSqlNode = sqlBasicCall.getOperandList().get(0);
                SqlNode aliasSqlNode = sqlBasicCall.getOperandList().get(1);
                ExpressionSegment expressionSegment = new ExpressionConverter().convertToSQLSegment(exprSqlNode).orElse(null);
                ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(getStartIndex(sqlBasicCall),
                        getStopIndex(sqlBasicCall), exprSqlNode.toString(), expressionSegment);
                expressionProjectionSegment.setAlias(new AliasSegment(getStartIndex(aliasSqlNode), getStopIndex(aliasSqlNode), new IdentifierValue(aliasSqlNode.toString())));
                return Optional.of(expressionProjectionSegment);
            }
            ExpressionSegment expressionSegment = new ExpressionConverter().convertToSQLSegment(sqlNode).orElse(null);
            String text = expressionSegment instanceof FunctionSegment ? ((FunctionSegment) expressionSegment).getText() : sqlNode.toString();
            return Optional.of(new ExpressionProjectionSegment(getStartIndex(sqlNode), getStopIndex(sqlNode), text, expressionSegment));
        }
        return Optional.empty();
    }
}
