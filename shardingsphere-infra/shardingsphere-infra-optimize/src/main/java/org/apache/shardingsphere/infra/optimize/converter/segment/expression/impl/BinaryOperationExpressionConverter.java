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

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Binary operation expression converter.
 */
public final class BinaryOperationExpressionConverter implements SQLSegmentConverter<BinaryOperationExpression, SqlNode> {
    
    private static final Map<String, SqlBinaryOperator> REGISTRY = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        register(SqlStdOperatorTable.EQUALS);
        register(SqlStdOperatorTable.NOT_EQUALS);
        register(SqlStdOperatorTable.GREATER_THAN);
        register(SqlStdOperatorTable.GREATER_THAN_OR_EQUAL);
        register(SqlStdOperatorTable.LESS_THAN);
        register(SqlStdOperatorTable.LESS_THAN_OR_EQUAL);
        register(SqlStdOperatorTable.OR);
        register(SqlStdOperatorTable.AND);
        register(SqlStdOperatorTable.PLUS);
        register(SqlStdOperatorTable.MINUS);
        register(SqlStdOperatorTable.MULTIPLY);
        register(SqlStdOperatorTable.DIVIDE);
    }
    
    private static void register(final SqlBinaryOperator sqlBinaryOperator) {
        REGISTRY.put(sqlBinaryOperator.getName(), sqlBinaryOperator);
    }
    
    @Override
    public Optional<SqlNode> convert(final BinaryOperationExpression segment) {
        SqlBinaryOperator operator = convertOperator(segment.getOperator());
        SqlNode left = convertExpression(segment.getLeft());
        SqlNode right = convertExpression(segment.getRight());
        return Optional.of(new SqlBasicCall(operator, new SqlNode[] {left, right}, SqlParserPos.ZERO));
    }
    
    private SqlBinaryOperator convertOperator(final String operator) {
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
    
    private SqlNode convertExpression(final ExpressionSegment segment) {
        Optional<SqlNode> result = new ExpressionConverter().convert(segment);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
}
