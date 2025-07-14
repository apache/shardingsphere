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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.order.OrderBySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.predicate.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.with.WithSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;

/**
 * Delete statement binder.
 */
public final class DeleteStatementBinder implements SQLStatementBinder<DeleteStatement> {
    
    @Override
    public DeleteStatement bind(final DeleteStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        WithSegment boundWith = sqlStatement.getWith().map(optional -> WithSegmentBinder.bind(optional, binderContext, binderContext.getExternalTableBinderContexts())).orElse(null);
        TableSegment boundTable = TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts, LinkedHashMultimap.create());
        WhereSegment boundWhere = sqlStatement.getWhere().map(optional -> WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, LinkedHashMultimap.create())).orElse(null);
        OrderBySegment boundOrderBy = sqlStatement.getOrderBy()
                .map(optional -> OrderBySegmentBinder.bind(optional, binderContext, LinkedHashMultimap.create(), tableBinderContexts, LinkedHashMultimap.create())).orElse(null);
        return copy(sqlStatement, boundWith, boundTable, boundWhere, boundOrderBy);
    }
    
    private DeleteStatement copy(final DeleteStatement sqlStatement, final WithSegment boundWith, final TableSegment boundTable, final WhereSegment boundWhere, final OrderBySegment boundOrderBy) {
        DeleteStatement result = new DeleteStatement(sqlStatement.getDatabaseType());
        result.setWith(boundWith);
        result.setTable(boundTable);
        result.setWhere(boundWhere);
        result.setOrderBy(boundOrderBy);
        sqlStatement.getLimit().ifPresent(result::setLimit);
        sqlStatement.getOutput().ifPresent(result::setOutput);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
