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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.statement.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Optional;

/**
 * Exists subquery expression converter.
 */
@RequiredArgsConstructor
public final class ExistsSubqueryExpressionConverter implements SQLSegmentConverter<ExistsSubqueryExpression, SqlBasicCall> {
    
    private final boolean not;
    
    public ExistsSubqueryExpressionConverter() {
        not = false;
    }
    
    @Override
    public Optional<SqlBasicCall> convertToSQLNode(final ExistsSubqueryExpression expression) {
        if (null == expression) {
            return Optional.empty();
        }
        SqlBasicCall sqlNode = new SqlBasicCall(SqlStdOperatorTable.EXISTS, new SqlNode[]{new SelectStatementConverter().convertToSQLNode(expression.getSubquery().getSelect())}, SqlParserPos.ZERO);
        return expression.isNot() ? Optional.of(new SqlBasicCall(SqlStdOperatorTable.NOT, new SqlNode[]{sqlNode}, SqlParserPos.ZERO)) : Optional.of(sqlNode);
    }
    
    @Override
    public Optional<ExistsSubqueryExpression> convertToSQLSegment(final SqlBasicCall sqlBasicCall) {
        if (null == sqlBasicCall) {
            return Optional.empty();
        }
        SqlNode subquerySqlNode = sqlBasicCall.getOperandList().get(0);
        SelectStatement selectStatement = new SelectStatementConverter().convertToSQLStatement(subquerySqlNode);
        SubquerySegment subquery = new SubquerySegment(getStartIndex(subquerySqlNode) - 1, getStopIndex(subquerySqlNode) + 1, selectStatement);
        ExistsSubqueryExpression result = new ExistsSubqueryExpression(getStartIndex(sqlBasicCall), subquery.getStopIndex(), subquery);
        result.setNot(not);
        return Optional.of(result);
    }
}
