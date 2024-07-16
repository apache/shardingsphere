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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.combine.CombineSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.lock.LockSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.projection.ProjectionsSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Select statement binder.
 */
@RequiredArgsConstructor
public final class SelectStatementBinder implements SQLStatementBinder<SelectStatement> {
    
    private final Map<String, TableSegmentBinderContext> outerTableBinderContexts;
    
    public SelectStatementBinder() {
        outerTableBinderContexts = Collections.emptyMap();
    }
    
    @Override
    public SelectStatement bind(final SelectStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        SelectStatement result = copy(sqlStatement);
        Map<String, TableSegmentBinderContext> tableBinderContexts = new LinkedHashMap<>();
        Optional<TableSegment> boundTableSegment = sqlStatement.getFrom().map(optional -> TableSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts));
        boundTableSegment.ifPresent(result::setFrom);
        result.setProjections(ProjectionsSegmentBinder.bind(sqlStatement.getProjections(), binderContext, boundTableSegment.orElse(null), tableBinderContexts, outerTableBinderContexts));
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)));
        sqlStatement.getCombine().ifPresent(optional -> result.setCombine(CombineSegmentBinder.bind(optional, binderContext)));
        sqlStatement.getLock().ifPresent(optional -> result.setLock(LockSegmentBinder.bind(optional, binderContext, tableBinderContexts, outerTableBinderContexts)));
        // TODO support other segment bind in select statement
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private SelectStatement copy(final SelectStatement sqlStatement) {
        SelectStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getGroupBy().ifPresent(result::setGroupBy);
        sqlStatement.getHaving().ifPresent(result::setHaving);
        sqlStatement.getOrderBy().ifPresent(result::setOrderBy);
        sqlStatement.getLimit().ifPresent(result::setLimit);
        sqlStatement.getWindow().ifPresent(result::setWindow);
        sqlStatement.getModelSegment().ifPresent(result::setModelSegment);
        sqlStatement.getWithSegment().ifPresent(result::setWithSegment);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
