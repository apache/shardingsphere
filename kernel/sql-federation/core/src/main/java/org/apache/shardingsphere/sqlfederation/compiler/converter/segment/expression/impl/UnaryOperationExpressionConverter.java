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

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sqlfederation.compiler.converter.function.dialect.mysql.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unary operation expression converter.
 */
public final class UnaryOperationExpressionConverter implements SQLSegmentConverter<UnaryOperationExpression, SqlNode> {
    
    private static final Map<String, SqlOperator> REGISTRY = new CaseInsensitiveMap<>();
    
    static {
        register();
    }
    
    private static void register() {
        register(SqlStdOperatorTable.UNARY_PLUS);
        register(SqlStdOperatorTable.UNARY_MINUS);
        register(SQLExtensionOperatorTable.TILDE);
    }
    
    private static void register(final SqlOperator sqlOperator) {
        REGISTRY.put(sqlOperator.getName(), sqlOperator);
    }
    
    @Override
    public Optional<SqlNode> convert(final UnaryOperationExpression segment) {
        SqlOperator operator = convertOperator(segment);
        List<SqlNode> sqlNodes = convertSqlNodes(segment);
        return Optional.of(new SqlBasicCall(operator, sqlNodes, SqlParserPos.ZERO));
    }
    
    private SqlOperator convertOperator(final UnaryOperationExpression segment) {
        String operator = segment.getOperator();
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: %s", operator);
        return REGISTRY.get(operator);
    }
    
    private List<SqlNode> convertSqlNodes(final UnaryOperationExpression segment) {
        SqlNode expression = new ExpressionConverter().convert(segment.getExpression()).orElseThrow(IllegalStateException::new);
        List<SqlNode> result = new LinkedList<>();
        result.add(expression);
        return result;
    }
}
