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

import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.UpdateStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

/**
 * Prepare statement binder.
 */
public final class PrepareStatementBinder implements SQLStatementBinder<PrepareStatement> {
    
    @Override
    public PrepareStatement bind(final PrepareStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        SelectStatement boundSelect = sqlStatement.getSelect().map(optional -> new SelectStatementBinder().bind(optional, binderContext)).orElse(null);
        InsertStatement boundInsert = sqlStatement.getInsert().map(optional -> new InsertStatementBinder().bind(optional, binderContext)).orElse(null);
        UpdateStatement boundUpdate = sqlStatement.getUpdate().map(optional -> new UpdateStatementBinder().bind(optional, binderContext)).orElse(null);
        DeleteStatement boundDelete = sqlStatement.getDelete().map(optional -> new DeleteStatementBinder().bind(optional, binderContext)).orElse(null);
        return copy(sqlStatement, boundSelect, boundInsert, boundUpdate, boundDelete);
    }
    
    private PrepareStatement copy(final PrepareStatement sqlStatement, final SelectStatement boundSelect, final InsertStatement boundInsert,
                                  final UpdateStatement boundUpdate, final DeleteStatement boundDelete) {
        PrepareStatement result = new PrepareStatement(sqlStatement.getDatabaseType());
        result.setSelect(boundSelect);
        result.setInsert(boundInsert);
        result.setUpdate(boundUpdate);
        result.setDelete(boundDelete);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
