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

package org.apache.shardingsphere.infra.binder.engine.statement.dal;

import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.infra.binder.engine.type.DMLStatementBindEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

/**
 * Explain statement binder.
 */
public final class ExplainStatementBinder implements SQLStatementBinder<ExplainStatement> {
    
    @Override
    public ExplainStatement bind(final ExplainStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        SQLStatement boundSQLStatement = sqlStatement.getExplainableSQLStatement() instanceof DMLStatement
                ? new DMLStatementBindEngine().bind((DMLStatement) sqlStatement.getExplainableSQLStatement(), binderContext)
                : sqlStatement.getExplainableSQLStatement();
        return copy(sqlStatement, boundSQLStatement);
    }
    
    private ExplainStatement copy(final ExplainStatement sqlStatement, final SQLStatement boundSQLStatement) {
        ExplainStatement result = new ExplainStatement(sqlStatement.getDatabaseType(), boundSQLStatement);
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
