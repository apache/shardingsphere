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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;

/**
 * Delete multi table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteMultiTableSegmentBinder {
    
    /**
     * Bind delete multi table segment.
     *
     * @param segment delete multi table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bound delete multi table segment
     */
    public static DeleteMultiTableSegment bind(final DeleteMultiTableSegment segment,
                                               final SQLStatementBinderContext binderContext, final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        DeleteMultiTableSegment result = new DeleteMultiTableSegment();
        result.setStartIndex(segment.getStartIndex());
        result.setStopIndex(segment.getStopIndex());
        result.getActualDeleteTables().addAll(segment.getActualDeleteTables());
        result.setRelationTable(TableSegmentBinder.bind(segment.getRelationTable(), binderContext, tableBinderContexts, LinkedHashMultimap.create()));
        return result;
    }
}
