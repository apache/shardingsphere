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

package org.apache.shardingsphere.infra.binder.engine.segment.ddl.column;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;

/**
 * Rename table definition segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenameTableDefinitionSegmentBinder {
    
    /**
     * Bind rename table definition segment.
     *
     * @param segment rename table definition segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bound rename table definition segment
     */
    public static RenameTableDefinitionSegment bind(final RenameTableDefinitionSegment segment, final SQLStatementBinderContext binderContext,
                                                    final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        RenameTableDefinitionSegment result = new RenameTableDefinitionSegment(segment.getStartIndex(), segment.getStopIndex());
        result.setTable(SimpleTableSegmentBinder.bind(segment.getTable(), binderContext, tableBinderContexts));
        result.setRenameTable(SimpleTableSegmentBinder.bind(segment.getRenameTable(), binderContext, tableBinderContexts));
        return result;
    }
}
