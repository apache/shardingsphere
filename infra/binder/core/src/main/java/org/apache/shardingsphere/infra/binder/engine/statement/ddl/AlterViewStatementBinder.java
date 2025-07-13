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
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Alter view statement binder.
 */
public final class AlterViewStatementBinder implements SQLStatementBinder<AlterViewStatement> {
    
    @Override
    public AlterViewStatement bind(final AlterViewStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment boundView = SimpleTableSegmentBinder.bind(sqlStatement.getView(), binderContext, tableBinderContexts);
        SelectStatement boundSelect = sqlStatement.getSelect().map(optional -> new SelectStatementBinder().bind(optional, binderContext)).orElse(null);
        SimpleTableSegment boundRenameView = sqlStatement.getRenameView().map(optional -> SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts)).orElse(null);
        return copy(sqlStatement, boundView, boundSelect, boundRenameView);
    }
    
    private AlterViewStatement copy(final AlterViewStatement sqlStatement, final SimpleTableSegment boundView, final SelectStatement boundSelect, final SimpleTableSegment boundRenameView) {
        AlterViewStatement result = new AlterViewStatement(sqlStatement.getDatabaseType());
        result.setView(boundView);
        result.setSelect(boundSelect);
        result.setRenameView(boundRenameView);
        sqlStatement.getViewDefinition().ifPresent(result::setViewDefinition);
        sqlStatement.getConstraintDefinition().ifPresent(result::setConstraintDefinition);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
