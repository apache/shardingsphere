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
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Not expression converter.
 */
public final class NotExpressionConverter implements SQLSegmentConverter<NotExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final NotExpression segment) {
        SqlNode expression = new ExpressionConverter().convert(segment.getExpression()).orElseThrow(IllegalStateException::new);
        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(expression);
        if (segment.getNotSign().equals(true)) {
            return Optional.of(new SqlBasicCall(SQLExtensionOperatorTable.NOT_SIGN, sqlNodes, SqlParserPos.ZERO));
        }
        return Optional.of(new SqlBasicCall(SqlStdOperatorTable.NOT, sqlNodes, SqlParserPos.ZERO));
    }
}
