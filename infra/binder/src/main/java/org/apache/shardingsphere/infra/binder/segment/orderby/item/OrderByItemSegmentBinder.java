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

package org.apache.shardingsphere.infra.binder.segment.orderby.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.segment.orderby.item.impl.ColumnOrderByItemSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.orderby.item.impl.ExpressionOrderByItemSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;

import java.util.Map;

/**
 * Order by item segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderByItemSegmentBinder {
    
    /**
     * Bind order by item segment with metadata.
     *
     * @param segment order by item segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded order by item segment
     */
    public static OrderByItemSegment bind(final OrderByItemSegment segment, final SQLStatementBinderContext statementBinderContext,
                                          final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment instanceof ColumnOrderByItemSegment) {
            return ColumnOrderByItemSegmentBinder.bind((ColumnOrderByItemSegment) segment, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof ExpressionOrderByItemSegment) {
            return ExpressionOrderByItemSegmentBinder.bind((ExpressionOrderByItemSegment) segment, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        return segment;
    }
}
