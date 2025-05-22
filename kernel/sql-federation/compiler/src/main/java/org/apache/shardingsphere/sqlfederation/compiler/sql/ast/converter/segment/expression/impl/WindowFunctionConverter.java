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
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.SqlWindow;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.validate.SqlNameMatchers;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Window function converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowFunctionConverter {
    
    /**
     * Convert function segment to sql node.
     *
     * @param segment function segment
     * @return sql node
     */
    public static Optional<SqlNode> convert(final FunctionSegment segment) {
        SqlIdentifier functionName = new SqlIdentifier(segment.getFunctionName(), SqlParserPos.ZERO);
        List<SqlOperator> functions = new LinkedList<>();
        SqlStdOperatorTable.instance().lookupOperatorOverloads(functionName, null, SqlSyntax.BINARY, functions, SqlNameMatchers.withCaseSensitive(false));
        return Optional.of(new SqlBasicCall(functions.iterator().next(), getWindowFunctionParameters(segment.getParameters()), SqlParserPos.ZERO));
    }
    
    private static List<SqlNode> getWindowFunctionParameters(final Collection<ExpressionSegment> sqlSegments) {
        List<SqlNode> result = new LinkedList<>();
        for (ExpressionSegment each : sqlSegments) {
            ExpressionConverter.convert(each).ifPresent(result::add);
        }
        if (1 == result.size()) {
            result.add(new SqlWindow(SqlParserPos.ZERO, null, null, new SqlNodeList(SqlParserPos.ZERO), new SqlNodeList(SqlParserPos.ZERO), SqlLiteral.createBoolean(false, SqlParserPos.ZERO), null,
                    null, null));
        }
        return result;
    }
}
