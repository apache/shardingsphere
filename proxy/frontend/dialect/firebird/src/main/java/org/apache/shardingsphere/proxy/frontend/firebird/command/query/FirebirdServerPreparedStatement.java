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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ExecuteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

/**
 * Prepared statement for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdServerPreparedStatement implements ServerPreparedStatement {
    
    private static final int FLAG_HAS_CURSOR = 1;
    
    private static final int FLAG_REPEAT_EXECUTE = 2;
    
    private final String sql;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext;
    
    @Setter
    private long affectedRows;
    
    /**
     * Get Firebird statement flags.
     *
     * @return statement flags
     */
    @HighFrequencyInvocation
    public int getStatementFlags() {
        SQLStatement statement = sqlStatementContext.getSqlStatement();
        if (statement instanceof DCLStatement || statement instanceof DDLStatement && !(statement instanceof ExecuteStatement)) {
            return 0;
        }
        return statement instanceof SelectStatement || hasReturningCursor(statement) ? FLAG_REPEAT_EXECUTE | FLAG_HAS_CURSOR : FLAG_REPEAT_EXECUTE;
    }
    
    private boolean hasReturningCursor(final SQLStatement statement) {
        if (statement instanceof ExecuteStatement) {
            return ((ExecuteStatement) statement).isReturnsRows();
        }
        if (statement instanceof InsertStatement) {
            return ((InsertStatement) statement).getInsertSelect().isPresent() && ((InsertStatement) statement).getReturning().isPresent();
        }
        if (statement instanceof UpdateStatement) {
            return ((UpdateStatement) statement).getReturning().isPresent();
        }
        return statement instanceof DeleteStatement && ((DeleteStatement) statement).getReturning().isPresent();
    }
}
