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

package org.apache.shardingsphere.infra.binder.engine.statement.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.combine.CombineSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.lock.LockSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.GroupBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.HavingSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.util.SubqueryTableBindUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Optional;

/**
 * Select statement binder.
 */
@RequiredArgsConstructor
public final class SelectStatementBinder implements SQLStatementBinder<SelectStatement> {
    
    private final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts;
    
    public SelectStatementBinder() {
        outerTableBinderContexts = LinkedHashMultimap.create();
    }
    
    @Override
    public SelectStatement bind(final SelectStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        SelectStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        sqlStatement.getWithSegment().ifPresent(optional -> result.setWithSegment(WithSegmentBinder.bind(optional, binderContext, binderContext.getExternalTableBinderContexts())));
        Optional<TableSegment> boundTableSegment = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts));
        boundTableSegment.ifPresent(result::setFrom);
        result.setProjections(ProjectionsSegmentBinder.bind(sqlStatement.getProjections(), binderContext, boundTableSegment.orElse(null), tableBinderContexts, outerTableBinderContexts));
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)));
        sqlStatement.getCombine().ifPresent(optional -> result.setCombine(CombineSegmentBinder.bind(optional, binderContext, outerTableBinderContexts)));
        sqlStatement.getLock().ifPresent(optional -> result.setLock(LockSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)));
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = createCurrentTableBinderContexts(binderContext, result);
        sqlStatement.getGroupBy().ifPresent(optional -> result.setGroupBy(
                GroupBySegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)));
        sqlStatement.getOrderBy().ifPresent(optional -> result.setOrderBy(
                OrderBySegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)));
        sqlStatement.getHaving().ifPresent(optional -> result.setHaving(
                HavingSegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)));
        return result;
    }
    
    private Multimap<CaseInsensitiveString, TableSegmentBinderContext> createCurrentTableBinderContexts(final SQLStatementBinderContext binderContext, final SelectStatement selectStatement) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                selectStatement.getProjections().getProjections(), new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), TableSourceType.MIXED_TABLE);
        result.put(new CaseInsensitiveString(""), new SimpleTableSegmentBinderContext(subqueryProjections, TableSourceType.MIXED_TABLE));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private SelectStatement copy(final SelectStatement sqlStatement) {
        SelectStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getLimit().ifPresent(result::setLimit);
        sqlStatement.getWindow().ifPresent(result::setWindow);
        sqlStatement.getModelSegment().ifPresent(result::setModelSegment);
        sqlStatement.getSubqueryType().ifPresent(result::setSubqueryType);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
