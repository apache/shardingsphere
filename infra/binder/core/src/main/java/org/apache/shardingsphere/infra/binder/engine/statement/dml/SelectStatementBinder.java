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
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
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
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        WithSegment boundWith = sqlStatement.getWith().map(optional -> WithSegmentBinder.bind(optional, binderContext, binderContext.getExternalTableBinderContexts())).orElse(null);
        Optional<TableSegment> boundFrom = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts));
        ProjectionsSegment boundProjections = ProjectionsSegmentBinder.bind(sqlStatement.getProjections(), binderContext, boundFrom.orElse(null), tableBinderContexts, outerTableBinderContexts);
        WhereSegment boundWhere = sqlStatement.getWhere().map(optional -> WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        CombineSegment boundCombine = sqlStatement.getCombine().map(optional -> CombineSegmentBinder.bind(optional, binderContext, outerTableBinderContexts)).orElse(null);
        LockSegment boundLock = sqlStatement.getLock().map(optional -> LockSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> currentTableBinderContexts = createCurrentTableBinderContexts(binderContext, boundProjections);
        GroupBySegment boundGroupBy =
                sqlStatement.getGroupBy().map(optional -> GroupBySegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        OrderBySegment boundOrderBy =
                sqlStatement.getOrderBy().map(optional -> OrderBySegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        HavingSegment boundHaving =
                sqlStatement.getHaving().map(optional -> HavingSegmentBinder.bind(optional, binderContext, currentTableBinderContexts, tableBinderContexts, outerTableBinderContexts)).orElse(null);
        return copy(sqlStatement, boundWith, boundFrom.orElse(null), boundProjections, boundWhere, boundCombine, boundLock, boundGroupBy, boundOrderBy, boundHaving);
    }
    
    private Multimap<CaseInsensitiveString, TableSegmentBinderContext> createCurrentTableBinderContexts(final SQLStatementBinderContext binderContext, final ProjectionsSegment boundProjections) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> result = LinkedHashMultimap.create();
        Collection<ProjectionSegment> subqueryProjections = SubqueryTableBindUtils.createSubqueryProjections(
                boundProjections.getProjections(), new IdentifierValue(""), binderContext.getSqlStatement().getDatabaseType(), TableSourceType.MIXED_TABLE);
        result.put(new CaseInsensitiveString(""), new SimpleTableSegmentBinderContext(subqueryProjections, TableSourceType.MIXED_TABLE));
        return result;
    }
    
    private SelectStatement copy(final SelectStatement sqlStatement, final WithSegment boundWith, final TableSegment boundFrom, final ProjectionsSegment boundProjections,
                                 final WhereSegment boundWhere, final CombineSegment boundCombine, final LockSegment boundLock,
                                 final GroupBySegment boundGroupBy, final OrderBySegment boundOrderBy, final HavingSegment boundHaving) {
        SelectStatement result = new SelectStatement(sqlStatement.getDatabaseType());
        result.setWith(boundWith);
        result.setFrom(boundFrom);
        result.setProjections(boundProjections);
        result.setWhere(boundWhere);
        result.setCombine(boundCombine);
        result.setLock(boundLock);
        result.setGroupBy(boundGroupBy);
        result.setOrderBy(boundOrderBy);
        result.setHaving(boundHaving);
        sqlStatement.getLimit().ifPresent(result::setLimit);
        sqlStatement.getWindow().ifPresent(result::setWindow);
        sqlStatement.getModel().ifPresent(result::setModel);
        sqlStatement.getSubqueryType().ifPresent(result::setSubqueryType);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
