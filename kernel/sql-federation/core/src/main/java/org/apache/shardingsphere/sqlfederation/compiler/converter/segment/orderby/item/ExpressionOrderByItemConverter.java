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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.orderby.item;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlPostfixOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.Collections;
import java.util.Optional;

/**
 * Expression of order by item converter.
 */
public final class ExpressionOrderByItemConverter implements SQLSegmentConverter<ExpressionOrderByItemSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final ExpressionOrderByItemSegment segment) {
        Optional<SqlNode> result = null == segment ? Optional.empty() : new ExpressionConverter().convert(segment.getExpr());
        if (!result.isPresent()) {
            return Optional.empty();
        }
        if (segment.getNullsOrderType().isPresent()) {
            SqlPostfixOperator nullsOrderType = NullsOrderType.FIRST == segment.getNullsOrderType().get() ? SqlStdOperatorTable.NULLS_FIRST : SqlStdOperatorTable.NULLS_LAST;
            result = Optional.of(new SqlBasicCall(nullsOrderType, Collections.singletonList(result.get()), SqlParserPos.ZERO));
        }
        return result;
    }
}
