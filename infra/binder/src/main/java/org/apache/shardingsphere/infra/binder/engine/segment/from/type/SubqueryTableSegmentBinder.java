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

package org.apache.shardingsphere.infra.binder.engine.segment.from.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Map;

/**
 * Subquery table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryTableSegmentBinder {
    
    /**
     * Bind subquery table segment.
     *
     * @param segment join table segment
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound subquery table segment
     */
    public static SubqueryTableSegment bind(final SubqueryTableSegment segment, final SQLStatementBinderContext binderContext,
                                            final Map<String, TableSegmentBinderContext> tableBinderContexts, final Map<String, TableSegmentBinderContext> outerTableBinderContexts) {
        fillPivotColumnNamesInBinderContext(segment, binderContext);
        SQLStatementBinderContext subqueryBinderContext = new SQLStatementBinderContext(segment.getSubquery().getSelect(), binderContext.getMetaData(), binderContext.getCurrentDatabaseName());
        subqueryBinderContext.getExternalTableBinderContexts().putAll(binderContext.getExternalTableBinderContexts());
        SelectStatement boundSubSelect = new SelectStatementBinder(outerTableBinderContexts).bind(segment.getSubquery().getSelect(), subqueryBinderContext);
        SubquerySegment boundSubquerySegment = new SubquerySegment(segment.getSubquery().getStartIndex(), segment.getSubquery().getStopIndex(), boundSubSelect, segment.getSubquery().getText());
        boundSubquerySegment.setSubqueryType(segment.getSubquery().getSubqueryType());
        IdentifierValue subqueryTableName = segment.getAliasSegment().map(AliasSegment::getIdentifier).orElseGet(() -> new IdentifierValue(""));
        SubqueryTableSegment result = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment);
        segment.getAliasSegment().ifPresent(result::setAlias);
        tableBinderContexts.put(subqueryTableName.getValue().toLowerCase(), new SimpleTableSegmentBinderContext(
                SubqueryTableBindUtils.createSubqueryProjections(boundSubSelect.getProjections().getProjections(), subqueryTableName, binderContext.getDatabaseType())));
        return result;
    }
    
    private static void fillPivotColumnNamesInBinderContext(final SubqueryTableSegment segment, final SQLStatementBinderContext binderContext) {
        segment.getPivot().ifPresent(optional -> optional.getPivotColumns().forEach(each -> binderContext.getPivotColumnNames().add(each.getIdentifier().getValue().toLowerCase())));
    }
}
