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

package org.apache.shardingsphere.infra.federation.converter.segment.orderby.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Order by item converter utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderByItemConverterUtil {
    
    /**
     * Convert order by items to sql node.
     * 
     * @param orderByItems order by item list
     * @return SQL nodes converted by order by item
     */
    public static Collection<SqlNode> convertToSQLNode(final Collection<OrderByItemSegment> orderByItems) {
        Collection<SqlNode> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                new ColumnOrderByItemConverter().convertToSQLNode((ColumnOrderByItemSegment) each).ifPresent(result::add);
            } else if (each instanceof ExpressionOrderByItemSegment) {
                throw new UnsupportedOperationException("unsupported ExpressionOrderByItemSegment");
            } else if (each instanceof IndexOrderByItemSegment) {
                throw new UnsupportedOperationException("unsupported IndexOrderByItemSegment");
            } else if (each instanceof TextOrderByItemSegment) {
                throw new UnsupportedOperationException("unsupported TextOrderByItemSegment");
            }
        }
        return result;
    }
    
    /**
     * Convert sql node list to order by items.
     *
     * @param sqlNodeList sql node list
     * @return order by items converted by sql node list
     */
    public static Collection<OrderByItemSegment> convertToSQLSegment(final SqlNodeList sqlNodeList) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        for (SqlNode each : sqlNodeList) {
            if (each instanceof SqlIdentifier) {
                new ColumnOrderByItemConverter().convertToSQLSegment(each).ifPresent(result::add);
            }
        }
        return result;
    }
}
