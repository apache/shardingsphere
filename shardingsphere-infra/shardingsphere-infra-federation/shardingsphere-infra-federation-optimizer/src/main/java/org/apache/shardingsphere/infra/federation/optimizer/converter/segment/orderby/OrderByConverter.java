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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.orderby;

import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.orderby.item.OrderByItemConverterUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;

import java.util.Collection;
import java.util.Optional;

/**
 * Order by converter. 
 */
public final class OrderByConverter implements SQLSegmentConverter<OrderBySegment, SqlNodeList> {
    
    @Override
    public Optional<SqlNodeList> convertToSQLNode(final OrderBySegment segment) {
        return null == segment ? Optional.empty() : Optional.of(new SqlNodeList(OrderByItemConverterUtil.convertToSQLNode(segment.getOrderByItems()), SqlParserPos.ZERO));
    }
    
    @Override
    public Optional<OrderBySegment> convertToSQLSegment(final SqlNodeList sqlNodeList) {
        if (null == sqlNodeList || 0 == sqlNodeList.size()) {
            return Optional.empty(); 
        }
        Collection<OrderByItemSegment> orderByItems = OrderByItemConverterUtil.convertToSQLSegment(sqlNodeList);
        return Optional.of(new OrderBySegment(getStartIndex(sqlNodeList), getStopIndex(sqlNodeList), orderByItems));
    }
}
