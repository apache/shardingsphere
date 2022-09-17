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

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Binary operation expression converter.
 */
public final class BinaryOperationExpressionConverter implements SQLSegmentConverter<BinaryOperationExpression, SqlNode> {
    
    private static final Map<String, SqlOperator> REGISTRY = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        register();
        registerAlias();
    }
    
    private static void register() {
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
        register(SqlStdOperatorTable.LIKE);
    }
    
    private static void register(final SqlOperator sqlOperator) {
        REGISTRY.put(sqlOperator.getName(), sqlOperator);
    }
    
    private static void registerAlias() {
        REGISTRY.put("!=", SqlStdOperatorTable.NOT_EQUALS);
    }
    
    @Override
    public Optional<SqlNode> convert(final BinaryOperationExpression segment) {
        SqlOperator operator = convertOperator(segment.getOperator());
        List<SqlNode> sqlNodes = convertSqlNodes(segment);
        return Optional.of(new SqlBasicCall(operator, sqlNodes, SqlParserPos.ZERO));
    }
    
    private List<SqlNode> convertSqlNodes(final BinaryOperationExpression segment) {
        SqlNode left = new ExpressionConverter().convert(segment.getLeft()).orElseThrow(IllegalStateException::new);
        SqlNode right = new ExpressionConverter().convert(segment.getRight()).orElseThrow(IllegalStateException::new);
        List<SqlNode> result = new LinkedList<>();
        result.add(left);
        result.addAll(right instanceof SqlNodeList ? ((SqlNodeList) right).getList() : Collections.singletonList(right));
        return result;
    }
    
    private SqlOperator convertOperator(final String operator) {
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
}
