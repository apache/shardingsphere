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
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * In expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InExpressionConverter {
    
    /**
     * Convert in expression to SQL node.
     *
     * @param expression in expression
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final InExpression expression) {
        if (null == expression) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        ExpressionConverter.convert(expression.getLeft()).ifPresent(sqlNodes::add);
        ExpressionConverter.convert(expression.getRight())
                .ifPresent(optional -> sqlNodes.add(optional instanceof SqlBasicCall ? new SqlNodeList(((SqlBasicCall) optional).getOperandList(), SqlParserPos.ZERO) : optional));
        return Optional.of(new SqlBasicCall(expression.isNot() ? SqlStdOperatorTable.NOT_IN : SqlStdOperatorTable.IN, new ArrayList<>(sqlNodes), SqlParserPos.ZERO));
    }
}
