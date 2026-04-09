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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Window item segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WindowItemSegmentBinder {
    
    /**
     * Bind window item segment.
     *
     * @param segment window item segment
     * @param parentSegmentType parent segment type
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound window item segment
     */
    public static WindowItemSegment bind(final WindowItemSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext,
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                         final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        WindowItemSegment result = new WindowItemSegment(segment.getStartIndex(), segment.getStopIndex());
        result.setWindowName(segment.getWindowName());
        if (null != segment.getPartitionListSegments()) {
            Collection<ExpressionSegment> boundPartitionListSegments = new LinkedList<>();
            for (ExpressionSegment each : segment.getPartitionListSegments()) {
                boundPartitionListSegments.add(ExpressionSegmentBinder.bind(each, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts));
            }
            result.setPartitionListSegments(boundPartitionListSegments);
        }
        if (null != segment.getOrderBySegment()) {
            result.setOrderBySegment(OrderBySegmentBinder.bind(segment.getOrderBySegment(), binderContext, tableBinderContexts, tableBinderContexts, outerTableBinderContexts));
        }
        result.setFrameClause(segment.getFrameClause());
        return result;
    }
}
