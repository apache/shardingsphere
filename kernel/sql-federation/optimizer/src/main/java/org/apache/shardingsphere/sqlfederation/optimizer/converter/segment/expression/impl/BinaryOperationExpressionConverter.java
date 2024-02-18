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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Binary operation expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BinaryOperationExpressionConverter {
    
    private static final Map<String, SqlOperator> REGISTRY = new CaseInsensitiveMap<>();
    
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
        register(SqlStdOperatorTable.NOT_LIKE);
        register(SqlStdOperatorTable.PERCENT_REMAINDER);
        register(SqlStdOperatorTable.IS_NULL);
        register(SqlStdOperatorTable.IS_NOT_NULL);
        register(SqlStdOperatorTable.ALL_GT);
        register(SqlStdOperatorTable.IS_FALSE);
        register(SqlStdOperatorTable.IS_NOT_FALSE);
        register(SqlStdOperatorTable.CONCAT);
        register(SqlStdOperatorTable.PATTERN_ALTER);
        register(SqlStdOperatorTable.MOD);
        register(SQLExtensionOperatorTable.DIV);
        register(SQLExtensionOperatorTable.CARET);
        register(SQLExtensionOperatorTable.AMPERSAND);
        register(SQLExtensionOperatorTable.SIGNED_RIGHT_SHIFT);
        register(SQLExtensionOperatorTable.SIGNED_LEFT_SHIFT);
        register(SQLExtensionOperatorTable.XOR);
        register(SQLExtensionOperatorTable.LOGICAL_AND);
        register(SQLExtensionOperatorTable.REGEXP);
        register(SQLExtensionOperatorTable.NOT_REGEXP);
        register(SQLExtensionOperatorTable.SOUNDS_LIKE);
        register(SQLExtensionOperatorTable.NULL_SAFE);
        register(SQLExtensionOperatorTable.ASSIGNMENT);
    }
    
    private static void register(final SqlOperator sqlOperator) {
        REGISTRY.put(sqlOperator.getName(), sqlOperator);
    }
    
    private static void registerAlias() {
        REGISTRY.put("!=", SqlStdOperatorTable.NOT_EQUALS);
        REGISTRY.put("~", SqlStdOperatorTable.POSIX_REGEX_CASE_SENSITIVE);
        REGISTRY.put("~*", SqlStdOperatorTable.NEGATED_POSIX_REGEX_CASE_SENSITIVE);
        REGISTRY.put("!~", SqlStdOperatorTable.NEGATED_POSIX_REGEX_CASE_SENSITIVE);
        REGISTRY.put("!~*", SqlStdOperatorTable.NEGATED_POSIX_REGEX_CASE_INSENSITIVE);
    }
    
    /**
     * Convert binary operation expression to sql node.
     * 
     * @param segment binary operation expression
     * @return sql node
     */
    public static Optional<SqlNode> convert(final BinaryOperationExpression segment) {
        SqlOperator operator = convertOperator(segment);
        List<SqlNode> sqlNodes = convertSqlNodes(segment, operator);
        return Optional.of(new SqlBasicCall(operator, sqlNodes, SqlParserPos.ZERO));
    }
    
    private static SqlOperator convertOperator(final BinaryOperationExpression segment) {
        String operator = segment.getOperator();
        if ("IS".equalsIgnoreCase(operator)) {
            String literals = String.valueOf(((LiteralExpressionSegment) segment.getRight()).getLiterals());
            if ("NULL".equalsIgnoreCase(literals)) {
                operator = "IS NULL";
            } else if ("NOT NULL".equalsIgnoreCase(literals)) {
                operator = "IS NOT NULL";
            } else if ("FALSE".equalsIgnoreCase(literals)) {
                operator = "IS FALSE";
            } else if ("NOT FALSE".equalsIgnoreCase(literals)) {
                operator = "IS NOT FALSE";
            }
        }
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
    
    private static List<SqlNode> convertSqlNodes(final BinaryOperationExpression segment, final SqlOperator operator) {
        SqlNode left = ExpressionConverter.convert(segment.getLeft()).orElseThrow(IllegalStateException::new);
        List<SqlNode> result = new LinkedList<>();
        result.add(left);
        if (!SqlStdOperatorTable.IS_NULL.equals(operator) && !SqlStdOperatorTable.IS_NOT_NULL.equals(operator) && !SqlStdOperatorTable.IS_FALSE.equals(operator)
                && !SqlStdOperatorTable.IS_NOT_FALSE.equals(operator)) {
            SqlNode right = ExpressionConverter.convert(segment.getRight()).orElseThrow(IllegalStateException::new);
            result.addAll(right instanceof SqlNodeList ? ((SqlNodeList) right).getList() : Collections.singletonList(right));
        }
        return result;
    }
}
