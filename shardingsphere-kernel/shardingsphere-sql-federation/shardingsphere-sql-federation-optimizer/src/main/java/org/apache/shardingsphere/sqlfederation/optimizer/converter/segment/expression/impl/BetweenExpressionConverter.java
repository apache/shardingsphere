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

import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Between expression converter.
 */
@RequiredArgsConstructor
public final class BetweenExpressionConverter implements SQLSegmentConverter<BetweenExpression, SqlBasicCall> {
    
    private final boolean not;
    
    public BetweenExpressionConverter() {
        not = false;
    }
    
    @Override
    public Optional<SqlBasicCall> convert(final BetweenExpression expression) {
        if (null == expression) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        ExpressionConverter expressionConverter = new ExpressionConverter();
        expressionConverter.convert(expression.getLeft()).ifPresent(sqlNodes::add);
        expressionConverter.convert(expression.getBetweenExpr()).ifPresent(sqlNodes::add);
        expressionConverter.convert(expression.getAndExpr()).ifPresent(sqlNodes::add);
        SqlBasicCall sqlNode = new SqlBasicCall(SqlStdOperatorTable.BETWEEN, new ArrayList<>(sqlNodes), SqlParserPos.ZERO);
        return expression.isNot() ? Optional.of(new SqlBasicCall(SqlStdOperatorTable.NOT, Collections.singletonList(sqlNode), SqlParserPos.ZERO)) : Optional.of(sqlNode);
    }
}
