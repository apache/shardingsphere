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
import org.apache.shardingsphere.infra.binder.engine.segment.ddl.column.ColumnDefinitionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Create table statement binder.
 */
public final class CreateTableStatementBinder implements SQLStatementBinder<CreateTableStatement> {
    
    @Override
    public CreateTableStatement bind(final CreateTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment boundTable = SimpleTableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts);
        SelectStatement boundSelectStatement = sqlStatement.getSelectStatement().map(optional -> new SelectStatementBinder().bind(optional, binderContext)).orElse(null);
        Collection<ColumnDefinitionSegment> boundColumnDefinitions = sqlStatement.getColumnDefinitions().stream()
                .map(each -> ColumnDefinitionSegmentBinder.bind(each, binderContext, tableBinderContexts)).collect(Collectors.toList());
        return copy(sqlStatement, boundTable, boundSelectStatement, boundColumnDefinitions);
    }
    
    private CreateTableStatement copy(final CreateTableStatement sqlStatement,
                                      final SimpleTableSegment boundTable, final SelectStatement boundSelectStatement, final Collection<ColumnDefinitionSegment> boundColumnDefinitions) {
        CreateTableStatement result = new CreateTableStatement(sqlStatement.getDatabaseType());
        result.setTable(boundTable);
        result.setSelectStatement(boundSelectStatement);
        result.getColumnDefinitions().addAll(boundColumnDefinitions);
        result.getConstraintDefinitions().addAll(sqlStatement.getConstraintDefinitions());
        // Remove duplicate addParameterMarkers call to avoid adding parameters twice
        // result.addParameterMarkers(sqlStatement.getParameterMarkers());
        result.setIfNotExists(sqlStatement.isIfNotExists());
        result.getColumns().addAll(sqlStatement.getColumns());
        sqlStatement.getLikeTable().ifPresent(result::setLikeTable);
        sqlStatement.getCreateTableOption().ifPresent(result::setCreateTableOption);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
