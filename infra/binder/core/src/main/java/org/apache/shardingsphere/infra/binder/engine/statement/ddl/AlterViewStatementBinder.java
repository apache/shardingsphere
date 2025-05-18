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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;

/**
 * Alter view statement binder.
 */
public final class AlterViewStatementBinder implements SQLStatementBinder<AlterViewStatement> {
    
    @Override
    public AlterViewStatement bind(final AlterViewStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        AlterViewStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        result.setView(SimpleTableSegmentBinder.bind(sqlStatement.getView(), binderContext, tableBinderContexts));
        sqlStatement.getSelect().ifPresent(optional -> result.setSelect(new SelectStatementBinder().bind(optional, binderContext)));
        sqlStatement.getRenameView().ifPresent(optional -> result.setRenameView(SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts)));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static AlterViewStatement copy(final AlterViewStatement sqlStatement) {
        AlterViewStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        sqlStatement.getViewDefinition().ifPresent(result::setViewDefinition);
        sqlStatement.getConstraintDefinition().ifPresent(result::setConstraintDefinition);
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
