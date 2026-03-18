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
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Cursor statement binder.
 */
public final class CursorStatementBinder implements SQLStatementBinder<CursorStatement> {
    
    @Override
    public CursorStatement bind(final CursorStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        return copy(sqlStatement, new SelectStatementBinder().bind(sqlStatement.getSelect(), binderContext));
    }
    
    private CursorStatement copy(final CursorStatement sqlStatement, final SelectStatement boundSelectStatement) {
        CursorStatement result = new CursorStatement(sqlStatement.getDatabaseType(), sqlStatement.getCursorName(), boundSelectStatement);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
