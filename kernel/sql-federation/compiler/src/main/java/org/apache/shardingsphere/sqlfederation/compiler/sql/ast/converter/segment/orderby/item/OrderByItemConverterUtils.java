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
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.TextOrderByItemSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Order by item converter utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderByItemConverterUtils {
    
    /**
     * Convert order by items to sql node.
     *
     * @param orderByItems order by items
     * @return SQL nodes converted by order by item
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    public static Collection<SqlNode> convert(final Collection<OrderByItemSegment> orderByItems) {
        Collection<SqlNode> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                ColumnOrderByItemConverter.convert((ColumnOrderByItemSegment) each).ifPresent(result::add);
            } else if (each instanceof ExpressionOrderByItemSegment) {
                ExpressionOrderByItemConverter.convert((ExpressionOrderByItemSegment) each).ifPresent(result::add);
            } else if (each instanceof IndexOrderByItemSegment) {
                IndexOrderByItemConverter.convert((IndexOrderByItemSegment) each).ifPresent(result::add);
            } else if (each instanceof TextOrderByItemSegment) {
                throw new UnsupportedSQLOperationException("unsupported TextOrderByItemSegment");
            }
        }
        return result;
    }
}
