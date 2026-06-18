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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlPostfixOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;

import java.util.Collections;
import java.util.Optional;

/**
 *  Index order by item converter. 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexOrderByItemConverter {
    
    /**
     * Convert index order by item segment to SQL node.
     *
     * @param segment index order by item segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final IndexOrderByItemSegment segment) {
        SqlNode result = SqlLiteral.createExactNumeric(String.valueOf(segment.getColumnIndex()), SqlParserPos.ZERO);
        if (OrderDirection.DESC == segment.getOrderDirection()) {
            result = new SqlBasicCall(SqlStdOperatorTable.DESC, Collections.singletonList(result), SqlParserPos.ZERO);
        }
        if (segment.getNullsOrderType().isPresent()) {
            SqlPostfixOperator nullsOrderType = NullsOrderType.FIRST == segment.getNullsOrderType().get() ? SqlStdOperatorTable.NULLS_FIRST : SqlStdOperatorTable.NULLS_LAST;
            result = new SqlBasicCall(nullsOrderType, Collections.singletonList(result), SqlParserPos.ZERO);
        }
        return Optional.of(result);
    }
}
