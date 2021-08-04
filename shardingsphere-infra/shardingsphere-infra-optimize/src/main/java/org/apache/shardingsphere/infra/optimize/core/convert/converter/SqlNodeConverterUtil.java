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

package org.apache.shardingsphere.infra.optimize.core.convert.converter;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.impl.ColumnOrderByItemSqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility for sql node converter.
 */
public final class SqlNodeConverterUtil {
    
    /**
     * Convert order by items.
     * @param orderByItems order by item list.
     * @return a collection of order by item <code>SqlNode</code>
     */
    public static Collection<SqlNode> convertOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        Collection<SqlNode> result = new ArrayList<>(orderByItems.size());
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                new ColumnOrderByItemSqlNodeConverter().convert((ColumnOrderByItemSegment) each).ifPresent(result::add);
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
}
