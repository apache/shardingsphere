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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.SqlWindow;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.validate.SqlNameMatchers;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.generic.OwnerConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.window.WindowConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Function converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionConverter {
    
    /**
     * Convert function segment to SQL node.
     *
     * @param segment function segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final FunctionSegment segment) {
        SqlIdentifier functionName = new SqlIdentifier(getQualifiedFunctionNames(segment), SqlParserPos.ZERO);
        // TODO optimize sql parse logic for select current_user.
        if ("CURRENT_USER".equalsIgnoreCase(functionName.getSimple())) {
            return Optional.of(functionName);
        }
        if ("TRIM".equalsIgnoreCase(functionName.getSimple())) {
            return Optional.of(TrimFunctionConverter.convert(segment));
        }
        if ("OVER".equalsIgnoreCase(functionName.getSimple())) {
            return WindowFunctionConverter.convert(segment);
        }
        List<SqlOperator> functions = new LinkedList<>();
        SqlStdOperatorTable.instance().lookupOperatorOverloads(functionName, null, SqlSyntax.FUNCTION, functions, SqlNameMatchers.withCaseSensitive(false));
        if (!functions.isEmpty() && segment.getWindow().isPresent()) {
            SqlBasicCall functionCall = new SqlBasicCall(functions.iterator().next(), getFunctionParameters(segment.getParameters()), SqlParserPos.ZERO);
            SqlWindow sqlWindow = WindowConverter.convertWindowItem(segment.getWindow().get());
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.OVER, new SqlNode[]{functionCall, sqlWindow}, SqlParserPos.ZERO));
        }
        return Optional.of(functions.isEmpty()
                ? new SqlBasicCall(new SqlUnresolvedFunction(functionName, null, null, null, null, SqlFunctionCategory.USER_DEFINED_FUNCTION), getFunctionParameters(segment.getParameters()),
                        SqlParserPos.ZERO)
                : new SqlBasicCall(functions.iterator().next(), getFunctionParameters(segment.getParameters()), SqlParserPos.ZERO));
    }
    
    private static List<String> getQualifiedFunctionNames(final FunctionSegment segment) {
        List<String> result = null == segment.getOwner() ? new LinkedList<>() : OwnerConverter.convert(segment.getOwner());
        result.add(segment.getFunctionName());
        return result;
    }
    
    private static List<SqlNode> getFunctionParameters(final Collection<ExpressionSegment> sqlSegments) {
        List<SqlNode> result = new LinkedList<>();
        for (ExpressionSegment each : sqlSegments) {
            ExpressionConverter.convert(each).ifPresent(optional -> result.addAll(optional instanceof SqlNodeList ? ((SqlNodeList) optional).getList() : Collections.singleton(optional)));
        }
        return result;
    }
}
