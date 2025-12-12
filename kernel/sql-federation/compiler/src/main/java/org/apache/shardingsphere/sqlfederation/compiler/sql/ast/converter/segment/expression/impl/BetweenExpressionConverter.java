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
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Between expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BetweenExpressionConverter {
    
    /**
     * Convert between expression to SQL node.
     *
     * @param expression between expression
     * @return SQL node
     */
    public static SqlBasicCall convert(final BetweenExpression expression) {
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        ExpressionConverter.convert(expression.getLeft()).ifPresent(sqlNodes::add);
        ExpressionConverter.convert(expression.getBetweenExpr()).ifPresent(sqlNodes::add);
        ExpressionConverter.convert(expression.getAndExpr()).ifPresent(sqlNodes::add);
        return new SqlBasicCall(expression.isNot() ? SqlStdOperatorTable.NOT_BETWEEN : SqlStdOperatorTable.BETWEEN, new ArrayList<>(sqlNodes), SqlParserPos.ZERO);
    }
}
