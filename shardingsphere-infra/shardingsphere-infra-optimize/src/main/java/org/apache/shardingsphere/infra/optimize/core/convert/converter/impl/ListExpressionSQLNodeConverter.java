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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SQLNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;

import java.util.List;
import java.util.Optional;

/**
 * List expression converter.
 */
public final class ListExpressionSQLNodeConverter implements SQLNodeConverter<ListExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ListExpression expression) {
        List<ExpressionSegment> items = expression.getItems();
        SqlNode left = null;
        for (ExpressionSegment item : items) {
            Optional<SqlNode> optional = new ExpressionSQLNodeConverter().convert(item);
            if (!optional.isPresent()) {
                continue;
            }
            if (left == null) {
                left = optional.get();
                continue;
            }
            left = new SqlBasicCall(SqlStdOperatorTable.OR, new SqlNode[] {left, optional.get()}, SqlParserPos.ZERO);
        }
        return Optional.of(left);
    }
}
