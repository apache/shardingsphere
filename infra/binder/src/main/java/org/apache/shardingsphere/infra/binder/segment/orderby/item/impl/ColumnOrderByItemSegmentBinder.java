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

package org.apache.shardingsphere.infra.binder.segment.orderby.item.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.segment.expression.impl.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;

import java.util.Map;

/**
 * Column order by item segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnOrderByItemSegmentBinder {
    
    /**
     * Bind column order by item segment with metadata.
     *
     * @param segment column order by item segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded column order by item segment
     */
    public static ColumnOrderByItemSegment bind(final ColumnOrderByItemSegment segment, final SQLStatementBinderContext statementBinderContext,
                                                final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        ColumnSegment boundedColumnSegment = ColumnSegmentBinder.bind(segment.getColumn(), SegmentType.ORDER_BY, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        return new ColumnOrderByItemSegment(boundedColumnSegment, segment.getOrderDirection(), segment.getNullsOrderType(statementBinderContext.getDatabaseType()));
    }
}
