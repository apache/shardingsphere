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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlNumericLiteral;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;

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
        register(SqlStdOperatorTable.IS_TRUE);
        register(SqlStdOperatorTable.IS_NOT_TRUE);
        register(SqlStdOperatorTable.CONCAT);
        register(SqlStdOperatorTable.PATTERN_ALTER);
        register(SqlStdOperatorTable.MOD);
        SqlStdOperatorTable.QUANTIFY_OPERATORS.forEach(BinaryOperationExpressionConverter::register);
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
        register(SQLExtensionOperatorTable.RLIKE);
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
     * Convert binary operation expression to SQL node.
     *
     * @param segment binary operation expression
     * @return SQL node
     */
    public static SqlBasicCall convert(final BinaryOperationExpression segment) {
        SqlOperator operator = convertOperator(segment);
        List<SqlNode> sqlNodes = convertSqlNodes(segment, operator);
        return new SqlBasicCall(operator, sqlNodes, SqlParserPos.ZERO);
    }
    
    private static SqlOperator convertOperator(final BinaryOperationExpression segment) {
        String operator = getOperator(segment);
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
    
    private static String getOperator(final BinaryOperationExpression segment) {
        String result = null;
        if ("IS".equalsIgnoreCase(segment.getOperator())) {
            result = findIsOperator(segment).orElse(null);
        }
        if (segment.getRight() instanceof QuantifySubqueryExpression) {
            result = findQuantifyOperator(segment.getOperator(), (QuantifySubqueryExpression) segment.getRight()).orElse(null);
        }
        return null == result ? segment.getOperator() : result;
    }
    
    private static Optional<String> findIsOperator(final BinaryOperationExpression segment) {
        String literals = String.valueOf(((LiteralExpressionSegment) segment.getRight()).getLiterals());
        if ("NULL".equalsIgnoreCase(literals)) {
            return Optional.of(SqlStdOperatorTable.IS_NULL.getName());
        } else if ("NOT NULL".equalsIgnoreCase(literals)) {
            return Optional.of(SqlStdOperatorTable.IS_NOT_NULL.getName());
        } else if ("FALSE".equalsIgnoreCase(literals)) {
            return Optional.of(SqlStdOperatorTable.IS_FALSE.getName());
        } else if ("NOT FALSE".equalsIgnoreCase(literals)) {
            return Optional.of(SqlStdOperatorTable.IS_NOT_FALSE.getName());
        } else if ("TRUE".equalsIgnoreCase(literals)) {
            return Optional.of(SqlStdOperatorTable.IS_TRUE.getName());
        } else if ("NOT TRUE".equalsIgnoreCase(literals)) {
            return Optional.of(SqlStdOperatorTable.IS_NOT_TRUE.getName());
        }
        return Optional.empty();
    }
    
    private static Optional<String> findQuantifyOperator(final String operator, final QuantifySubqueryExpression quantifySubquery) {
        if ("=".equals(operator)) {
            return "ALL".equalsIgnoreCase(quantifySubquery.getQuantifyOperator()) ? Optional.of(SqlStdOperatorTable.ALL_EQ.getName()) : Optional.of(SqlStdOperatorTable.SOME_EQ.getName());
        } else if (">".equals(operator)) {
            return "ALL".equalsIgnoreCase(quantifySubquery.getQuantifyOperator()) ? Optional.of(SqlStdOperatorTable.ALL_GT.getName()) : Optional.of(SqlStdOperatorTable.SOME_GT.getName());
        } else if (">=".equals(operator)) {
            return "ALL".equalsIgnoreCase(quantifySubquery.getQuantifyOperator()) ? Optional.of(SqlStdOperatorTable.ALL_GE.getName()) : Optional.of(SqlStdOperatorTable.SOME_GE.getName());
        } else if ("<".equals(operator)) {
            return "ALL".equalsIgnoreCase(quantifySubquery.getQuantifyOperator()) ? Optional.of(SqlStdOperatorTable.ALL_LT.getName()) : Optional.of(SqlStdOperatorTable.SOME_LT.getName());
        } else if ("<=".equals(operator)) {
            return "ALL".equalsIgnoreCase(quantifySubquery.getQuantifyOperator()) ? Optional.of(SqlStdOperatorTable.ALL_LE.getName()) : Optional.of(SqlStdOperatorTable.SOME_LE.getName());
        } else if ("!=".equals(operator) || "<>".equals(operator)) {
            return "ALL".equalsIgnoreCase(quantifySubquery.getQuantifyOperator()) ? Optional.of(SqlStdOperatorTable.ALL_NE.getName()) : Optional.of(SqlStdOperatorTable.SOME_NE.getName());
        }
        return Optional.empty();
    }
    
    private static List<SqlNode> convertSqlNodes(final BinaryOperationExpression segment, final SqlOperator operator) {
        SqlNode left = ExpressionConverter.convert(segment.getLeft()).orElseThrow(IllegalStateException::new);
        List<SqlNode> result = new LinkedList<>();
        result.add(left);
        if (SqlStdOperatorTable.IS_FALSE.equals(operator) || SqlStdOperatorTable.IS_NOT_FALSE.equals(operator) || SqlStdOperatorTable.IS_TRUE.equals(operator)
                || SqlStdOperatorTable.IS_NOT_TRUE.equals(operator)) {
            if (left instanceof SqlNumericLiteral) {
                result.remove(0);
                Long value = ((SqlNumericLiteral) left).getValueAs(Long.class);
                result.add(value == null || value == 0L ? SqlLiteral.createBoolean(false, left.getParserPosition()) : SqlLiteral.createBoolean(true, left.getParserPosition()));
            }
        } else if (!SqlStdOperatorTable.IS_NULL.equals(operator) && !SqlStdOperatorTable.IS_NOT_NULL.equals(operator)) {
            SqlNode right = ExpressionConverter.convert(segment.getRight()).orElseThrow(IllegalStateException::new);
            result.addAll(right instanceof SqlNodeList ? ((SqlNodeList) right).getList() : Collections.singletonList(right));
        }
        return result;
    }
}
