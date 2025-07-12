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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;

/**
 * Alter table statement binder.
 */
public final class AlterTableStatementBinder implements SQLStatementBinder<AlterTableStatement> {
    
    @Override
    public AlterTableStatement bind(final AlterTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SimpleTableSegment boundTable = SimpleTableSegmentBinder.bind(sqlStatement.getTable(), binderContext, tableBinderContexts);
        SimpleTableSegment boundRenameTable = sqlStatement.getRenameTable().map(optional -> SimpleTableSegmentBinder.bind(optional, binderContext, tableBinderContexts)).orElse(null);
        return copy(sqlStatement, boundTable, boundRenameTable);
    }
    
    private AlterTableStatement copy(final AlterTableStatement sqlStatement, final SimpleTableSegment boundTable, final SimpleTableSegment boundRenameTable) {
        AlterTableStatement result = new AlterTableStatement(sqlStatement.getDatabaseType());
        result.setTable(boundTable);
        result.setRenameTable(boundRenameTable);
        // TODO bind column and reference table if kernel need use them
        sqlStatement.getConvertTableDefinition().ifPresent(result::setConvertTableDefinition);
        result.getAddColumnDefinitions().addAll(sqlStatement.getAddColumnDefinitions());
        result.getModifyColumnDefinitions().addAll(sqlStatement.getModifyColumnDefinitions());
        result.getChangeColumnDefinitions().addAll(sqlStatement.getChangeColumnDefinitions());
        result.getDropColumnDefinitions().addAll(sqlStatement.getDropColumnDefinitions());
        result.getAddConstraintDefinitions().addAll(sqlStatement.getAddConstraintDefinitions());
        result.getValidateConstraintDefinitions().addAll(sqlStatement.getValidateConstraintDefinitions());
        result.getModifyConstraintDefinitions().addAll(sqlStatement.getModifyConstraintDefinitions());
        result.getDropConstraintDefinitions().addAll(sqlStatement.getDropConstraintDefinitions());
        result.getDropIndexDefinitions().addAll(sqlStatement.getDropIndexDefinitions());
        result.getRenameColumnDefinitions().addAll(sqlStatement.getRenameColumnDefinitions());
        result.getRenameIndexDefinitions().addAll(sqlStatement.getRenameIndexDefinitions());
        sqlStatement.getModifyCollectionRetrieval().ifPresent(result::setModifyCollectionRetrieval);
        sqlStatement.getDropPrimaryKeyDefinition().ifPresent(result::setDropPrimaryKeyDefinition);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
