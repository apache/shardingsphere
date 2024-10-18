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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.from.TableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.where.WhereSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;

/**
 * Delete statement binder.
 */
public final class DeleteStatementBinder implements SQLStatementBinder<DeleteStatement> {
    
    @Override
    public DeleteStatement bind(final DeleteStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        DeleteStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        result.setTable(TableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts, LinkedHashMultimap.create()));
        sqlStatement.getWhere().ifPresent(optional -> result.setWhere(WhereSegmentBinder.bind(optional, binderContext, tableBinderContexts, LinkedHashMultimap.create())));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private DeleteStatement copy(final DeleteStatement sqlStatement) {
        DeleteStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getOrderBy().ifPresent(result::setOrderBy);
        sqlStatement.getLimit().ifPresent(result::setLimit);
        sqlStatement.getWithSegment().ifPresent(result::setWithSegment);
        sqlStatement.getOutputSegment().ifPresent(result::setOutputSegment);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        return result;
    }
}
