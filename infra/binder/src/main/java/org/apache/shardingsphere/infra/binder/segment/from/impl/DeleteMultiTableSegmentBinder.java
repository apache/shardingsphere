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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.segment.from.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;

import java.util.Collections;
import java.util.Map;

/**
 * Delete multi table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteMultiTableSegmentBinder {
    
    /**
     * Bind delete multi table segment with metadata.
     *
     * @param segment delete multi table segment
     * @param statementBinderContext statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bounded join table segment
     */
    public static DeleteMultiTableSegment bind(final DeleteMultiTableSegment segment, final SQLStatementBinderContext statementBinderContext,
                                               final Map<String, TableSegmentBinderContext> tableBinderContexts) {
        DeleteMultiTableSegment result = new DeleteMultiTableSegment();
        result.setStartIndex(segment.getStartIndex());
        result.setStopIndex(segment.getStopIndex());
        result.getActualDeleteTables().addAll(segment.getActualDeleteTables());
        result.setRelationTable(TableSegmentBinder.bind(segment.getRelationTable(), statementBinderContext, tableBinderContexts, Collections.emptyMap()));
        return result;
    }
}
