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
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateViewStatement;

/**
 * Create view statement binder.
 */
public final class CreateViewStatementBinder implements SQLStatementBinder<CreateViewStatement> {
    
    @Override
    public CreateViewStatement bind(final CreateViewStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        CreateViewStatement result = copy(sqlStatement);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        result.setReplaceView(sqlStatement.isReplaceView());
        result.setView(SimpleTableSegmentBinder.bind(sqlStatement.getView(), binderContext, tableBinderContexts));
        result.setSelect(new SelectStatementBinder().bind(sqlStatement.getSelect(), binderContext));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static CreateViewStatement copy(final CreateViewStatement sqlStatement) {
        CreateViewStatement result = sqlStatement.getClass().getDeclaredConstructor().newInstance();
        result.setViewDefinition(sqlStatement.getViewDefinition());
        result.addParameterMarkerSegments(sqlStatement.getParameterMarkerSegments());
        result.getCommentSegments().addAll(sqlStatement.getCommentSegments());
        result.getVariableNames().addAll(sqlStatement.getVariableNames());
        return result;
    }
}
