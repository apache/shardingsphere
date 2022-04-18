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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.orderby.item;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Objects;
import java.util.Optional;

/**
 *  Column of order by item converter. 
 */
public final class ColumnOrderByItemConverter implements SQLSegmentConverter<ColumnOrderByItemSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final ColumnOrderByItemSegment segment) {
        Optional<SqlNode> result = new ColumnConverter().convertToSQLNode(segment.getColumn()).map(optional -> optional);
        if (result.isPresent() && Objects.equals(OrderDirection.DESC, segment.getOrderDirection())) {
            result = Optional.of(new SqlBasicCall(SqlStdOperatorTable.DESC, new SqlNode[] {result.get()}, SqlParserPos.ZERO));
        }
        return result;
    }
    
    @Override
    public Optional<ColumnOrderByItemSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (!(sqlNode instanceof SqlIdentifier)) {
            return Optional.empty(); 
        }
        SqlIdentifier sqlIdentifier = (SqlIdentifier) sqlNode;
        if (sqlIdentifier.names.size() > 1) {
            SqlIdentifier column = sqlIdentifier.getComponent(1);
            SqlIdentifier owner = sqlIdentifier.getComponent(0);
            ColumnSegment columnSegment = new ColumnSegment(getStartIndex(sqlIdentifier), getStopIndex(sqlIdentifier), new IdentifierValue(column.toString()));
            columnSegment.setOwner(new OwnerSegment(getStartIndex(owner), getStopIndex(owner), new IdentifierValue(owner.toString())));
            return Optional.of(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC));
        }
        ColumnSegment columnSegment = new ColumnSegment(getStartIndex(sqlIdentifier), getStopIndex(sqlIdentifier), new IdentifierValue(sqlIdentifier.names.get(0)));
        return Optional.of(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC));
    }
}
