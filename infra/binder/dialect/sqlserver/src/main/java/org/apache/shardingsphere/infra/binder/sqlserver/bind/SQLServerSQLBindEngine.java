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

package org.apache.shardingsphere.infra.binder.sqlserver.bind;

import org.apache.shardingsphere.infra.binder.engine.DialectSQLBindEngine;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.sqlserver.bind.type.SQLServerDenyUserStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.dcl.user.SQLServerDenyUserStatement;

import java.util.Optional;

/**
 * SQL bind engine for SQLServer.
 */
public final class SQLServerSQLBindEngine implements DialectSQLBindEngine {
    
    @Override
    public Optional<SQLStatement> bind(final SQLStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        if (sqlStatement instanceof SQLServerDenyUserStatement) {
            return Optional.of(new SQLServerDenyUserStatementBinder().bind((SQLServerDenyUserStatement) sqlStatement, binderContext));
        }
        return Optional.empty();
    }
    
    @Override
    public String getDatabaseType() {
        return "SQLServer";
    }
}
