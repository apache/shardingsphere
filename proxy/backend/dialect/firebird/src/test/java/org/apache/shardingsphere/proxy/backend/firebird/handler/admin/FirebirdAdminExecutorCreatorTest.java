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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.FirebirdSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.FirebirdShowVariableExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdAdminExecutorCreatorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Test
    void assertCreateWithSelectNonSystem() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(new SelectStatement(databaseType));
        assertThat(new FirebirdAdminExecutorCreator().create(selectStatementContext, "SELECT 1", "", Collections.emptyList()), is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithSetStatement() {
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new SetStatement(databaseType, Collections.emptyList()));
        Optional<DatabaseAdminExecutor> actual = new FirebirdAdminExecutorCreator().create(sqlStatementContext, "SET NAMES utf8", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(FirebirdSetVariableAdminExecutor.class));
    }
    
    @Test
    void assertCreateWithShowSQLStatement() {
        Optional<DatabaseAdminExecutor> actual = new FirebirdAdminExecutorCreator().create(
                new CommonSQLStatementContext(new ShowStatement(databaseType, "server_version")), "SHOW server_version", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(FirebirdShowVariableExecutor.class));
    }
    
    @Test
    void assertCreateWithDMLStatement() {
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(new DeleteStatement(databaseType));
        assertThat(new FirebirdAdminExecutorCreator().create(sqlStatementContext, "DELETE FROM t WHERE id = 1", "", Collections.emptyList()), is(Optional.empty()));
    }
}
