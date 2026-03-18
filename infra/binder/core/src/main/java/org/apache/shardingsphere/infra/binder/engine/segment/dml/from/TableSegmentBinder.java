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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.FunctionTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.DeleteMultiTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.JoinTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SubqueryTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

/**
 * Table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableSegmentBinder {
    
    /**
     * Bind table segment.
     *
     * @param segment table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound table segment
     */
    public static TableSegment bind(final TableSegment segment, final SQLStatementBinderContext binderContext, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        if (segment instanceof SimpleTableSegment) {
            return SimpleTableSegmentBinder.bind((SimpleTableSegment) segment, binderContext, tableBinderContexts);
        }
        if (segment instanceof JoinTableSegment) {
            return JoinTableSegmentBinder.bind((JoinTableSegment) segment, binderContext, tableBinderContexts, outerTableBinderContexts);
        }
        if (segment instanceof SubqueryTableSegment) {
            return SubqueryTableSegmentBinder.bind((SubqueryTableSegment) segment, binderContext, tableBinderContexts, outerTableBinderContexts, false);
        }
        if (segment instanceof DeleteMultiTableSegment) {
            return DeleteMultiTableSegmentBinder.bind((DeleteMultiTableSegment) segment, binderContext, tableBinderContexts);
        }
        if (segment instanceof FunctionTableSegment) {
            String name = segment.getAliasName().orElseGet(() -> ((FunctionTableSegment) segment).getTableFunction().getText());
            if (null != name) {
                tableBinderContexts.put(CaseInsensitiveString.of(name), new FunctionTableSegmentBinderContext());
            }
            return segment;
        }
        if (segment instanceof CollectionTableSegment) {
            String name = segment.getAliasName().orElseGet(() -> ((CollectionTableSegment) segment).getExpressionSegment().getText());
            if (null != name) {
                tableBinderContexts.put(CaseInsensitiveString.of(name), new FunctionTableSegmentBinderContext());
            }
            return segment;
        }
        return segment;
    }
}
