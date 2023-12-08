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

package org.apache.shardingsphere.infra.binder.segment.from;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.impl.DeleteMultiTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.impl.JoinTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.impl.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.impl.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Map;

/**
 * Table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableSegmentBinder {
    
    /**
     * Bind table segment with metadata.
     *
     * @param segment table segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bounded table segment
     */
    public static TableSegment bind(final TableSegment segment, final SQLStatementBinderContext statementBinderContext, final Map<String, TableSegmentBinderContext> tableBinderContexts,
                                    final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment instanceof SimpleTableSegment) {
            return SimpleTableSegmentBinder.bind((SimpleTableSegment) segment, statementBinderContext, tableBinderContexts);
        }
        if (segment instanceof JoinTableSegment) {
            return JoinTableSegmentBinder.bind((JoinTableSegment) segment, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof SubqueryTableSegment) {
            return SubqueryTableSegmentBinder.bind((SubqueryTableSegment) segment, statementBinderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof DeleteMultiTableSegment) {
            return DeleteMultiTableSegmentBinder.bind((DeleteMultiTableSegment) segment, statementBinderContext, tableBinderContexts);
        }
        if (segment instanceof FunctionTableSegment) {
            tableBinderContexts.put(segment.getAliasName().orElseGet(() -> ((FunctionTableSegment) segment).getTableFunction().getText()).toLowerCase(), new FunctionTableSegmentBinderContext());
            return segment;
        }
        if (segment instanceof CollectionTableSegment) {
            tableBinderContexts.put(segment.getAliasName().orElseGet(() -> ((CollectionTableSegment) segment).getExpressionSegment().getText()).toLowerCase(), new FunctionTableSegmentBinderContext());
            return segment;
        }
        return segment;
    }
}
