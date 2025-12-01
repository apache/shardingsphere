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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;

/**
 * Column definition segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnDefinitionSegmentBinder {
    
    /**
     * Bind column definition segment.
     *
     * @param segment column definition segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @return bound column definition segment
     */
    public static ColumnDefinitionSegment bind(final ColumnDefinitionSegment segment, final SQLStatementBinderContext binderContext,
                                               final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts) {
        ColumnSegment boundColumnSegment = ColumnSegmentBinder.bind(segment.getColumnName(), SegmentType.DEFINITION_COLUMNS, binderContext, tableBinderContexts, LinkedHashMultimap.create());
        ColumnDefinitionSegment result =
                new ColumnDefinitionSegment(segment.getStartIndex(), segment.getStopIndex(), boundColumnSegment, segment.getDataType(), segment.isPrimaryKey(), segment.isNotNull(), segment.getText());
        copy(result, segment);
        segment.getReferencedTables().forEach(each -> result.getReferencedTables().add(SimpleTableSegmentBinder.bind(each, binderContext, tableBinderContexts)));
        return result;
    }
    
    private static void copy(final ColumnDefinitionSegment result, final ColumnDefinitionSegment segment) {
        result.setAutoIncrement(segment.isAutoIncrement());
        result.setRef(segment.isRef());
        result.setCharsetName(segment.getCharsetName());
        result.setCollateName(segment.getCollateName());
    }
}
