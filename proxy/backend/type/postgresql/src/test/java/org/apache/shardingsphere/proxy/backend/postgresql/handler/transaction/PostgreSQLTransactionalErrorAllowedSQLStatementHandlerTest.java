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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.transaction;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.transaction.TransactionalErrorAllowedSQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLFeatureNotSupportedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PostgreSQLTransactionalErrorAllowedSQLStatementHandlerTest {
    
    private final TransactionalErrorAllowedSQLStatementHandler allowedSQLStatementHandler = DatabaseTypedSPILoader.getService(
            TransactionalErrorAllowedSQLStatementHandler.class, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
    
    @Test
    void assertJudgeContinueToExecuteWithCommitStatement() {
        assertDoesNotThrow(() -> allowedSQLStatementHandler.judgeContinueToExecute(mock(CommitStatement.class)));
    }
    
    @Test
    void assertJudgeContinueToExecuteWithRollbackStatement() {
        assertDoesNotThrow(() -> allowedSQLStatementHandler.judgeContinueToExecute(mock(RollbackStatement.class)));
    }
    
    @Test
    void assertJudgeContinueToExecuteWithNotAllowedStatement() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> allowedSQLStatementHandler.judgeContinueToExecute(mock(SelectStatement.class)));
    }
}
