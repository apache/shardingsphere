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

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.fun.SqlTrimFunction.Flag;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.validate.SqlNameMatchers;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Trim function converter.
 */
public final class TrimFunctionConverter extends FunctionConverter {
    
    @Override
    public Optional<SqlNode> convert(final FunctionSegment segment) {
        SqlIdentifier functionName = new SqlIdentifier(segment.getFunctionName(), SqlParserPos.ZERO);
        List<SqlOperator> functions = new LinkedList<>();
        SqlStdOperatorTable.instance().lookupOperatorOverloads(functionName, null, SqlSyntax.FUNCTION, functions, SqlNameMatchers.withCaseSensitive(false));
        return Optional.of(new SqlBasicCall(functions.iterator().next(), getTrimFunctionParameters(segment.getParameters()), SqlParserPos.ZERO));
    }
    
    private List<SqlNode> getTrimFunctionParameters(final Collection<ExpressionSegment> sqlSegments) {
        List<SqlNode> result = new LinkedList<>();
        if (1 == sqlSegments.size()) {
            result.add(Flag.BOTH.symbol(SqlParserPos.ZERO));
            result.add(SqlLiteral.createCharString(" ", SqlParserPos.ZERO));
        }
        if (2 == sqlSegments.size()) {
            result.add(Flag.BOTH.symbol(SqlParserPos.ZERO));
        }
        for (ExpressionSegment each : sqlSegments) {
            new ExpressionConverter().convert(each).ifPresent(result::add);
        }
        return result;
    }
}
