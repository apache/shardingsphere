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

import com.google.common.collect.Lists;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Converter for the collection of order by items.
 */
public abstract class AbstractOrderBySqlNodeConverter  {

    protected final List<SqlNode> convertOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        List<SqlNode> sqlNodes = Lists.newArrayList();
        for (OrderByItemSegment orderByItemSegment : orderByItems) {
            Optional<SqlNode> optional = Optional.empty();
            if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
                optional = new ColumnOrderByItemSqlNodeConverter().convert((ColumnOrderByItemSegment) orderByItemSegment);
            } else if (orderByItemSegment instanceof ExpressionOrderByItemSegment) {
                throw new UnsupportedOperationException("unsupported ExpressionOrderByItemSegment");
            } else if (orderByItemSegment instanceof IndexOrderByItemSegment) {
                throw new UnsupportedOperationException("unsupported IndexOrderByItemSegment");
            } else if (orderByItemSegment instanceof TextOrderByItemSegment) {
                throw new UnsupportedOperationException("unsupported TextOrderByItemSegment");
            }
            
            if(optional.isPresent()) {
                sqlNodes.add(optional.get());
            }
        }
        return sqlNodes;
    }
}
