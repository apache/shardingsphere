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
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.prepare.PrepareStatementQuerySegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CopyStatement;

/**
 * Copy statement binder.
 */
public final class CopyStatementBinder implements SQLStatementBinder<CopyStatement> {
    
    @Override
    public CopyStatement bind(final CopyStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        CopyStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        sqlStatement.getTable().ifPresent(optional -> result.setTable(SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts)));
        sqlStatement.getPrepareStatementQuery().ifPresent(optional -> result.setPrepareStatementQuery(PrepareStatementQuerySegmentBinder.bind(optional, binderContext)));
        sqlStatement.getColumns().forEach(each -> result.getColumns().add(ColumnSegmentBinder.bind(each, SegmentType.COPY, binderContext, tableBinderContexts, LinkedHashMultimap.create())));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private CopyStatement copy(final CopyStatement sqlStatement) {
        CopyStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
