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

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverterUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;

import java.util.Collection;
import java.util.Optional;

/**
 * Order by converter. 
 */
public final class OrderBySqlNodeConverter implements SqlNodeConverter<OrderBySegment, SqlNodeList> {
    
    @Override
    public Optional<SqlNodeList> convert(final OrderBySegment orderBy) {
        if (orderBy == null) {
            return Optional.empty();
        }
        Collection<SqlNode> orderBySqlNodes = SqlNodeConverterUtil.convertOrderByItems(orderBy.getOrderByItems());
        return Optional.of(new SqlNodeList(orderBySqlNodes, SqlParserPos.ZERO));
    }
}
