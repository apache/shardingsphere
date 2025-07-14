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
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.PivotSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;

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
     * @param fromWithSegment is from with segment
     * @return bound subquery table segment
     */
    public static SubqueryTableSegment bind(final SubqueryTableSegment segment, final SQLStatementBinderContext binderContext,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                            final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts, final boolean fromWithSegment) {
        binderContext.getPivotColumnNames().addAll(segment.getPivot().map(PivotSegment::getPivotColumnNames).orElse(Collections.emptyList()));
        SQLStatementBinderContext subqueryBinderContext = new SQLStatementBinderContext(
                binderContext.getMetaData(), binderContext.getCurrentDatabaseName(), binderContext.getHintValueContext(), segment.getSubquery().getSelect());
        subqueryBinderContext.getExternalTableBinderContexts().putAll(binderContext.getExternalTableBinderContexts());
        subqueryBinderContext.getCommonTableExpressionsSegmentsUniqueAliases().addAll(binderContext.getCommonTableExpressionsSegmentsUniqueAliases());
        SelectStatement boundSubSelect = new SelectStatementBinder(outerTableBinderContexts).bind(segment.getSubquery().getSelect(), subqueryBinderContext);
        binderContext.getCommonTableExpressionsSegmentsUniqueAliases().addAll(subqueryBinderContext.getCommonTableExpressionsSegmentsUniqueAliases());
        SubquerySegment boundSubquerySegment = new SubquerySegment(segment.getSubquery().getStartIndex(), segment.getSubquery().getStopIndex(), boundSubSelect, segment.getSubquery().getText());
        IdentifierValue subqueryTableName = segment.getAliasSegment().map(AliasSegment::getIdentifier).orElseGet(() -> new IdentifierValue(""));
        SubqueryTableSegment result = new SubqueryTableSegment(segment.getStartIndex(), segment.getStopIndex(), boundSubquerySegment);
        segment.getAliasSegment().ifPresent(result::setAlias);
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                boundSubSelect.getProjections().getProjections(), subqueryTableName, binderContext.getSqlStatement().getDatabaseType(), TableSourceType.TEMPORARY_TABLE);
        SimpleTableSegmentBinderContext tableBinderContext = new SimpleTableSegmentBinderContext(subqueryProjections, TableSourceType.TEMPORARY_TABLE);
        tableBinderContext.setFromWithSegment(fromWithSegment);
        tableBinderContexts.put(new CaseInsensitiveString(subqueryTableName.getValue()), tableBinderContext);
        return result;
    }
}
