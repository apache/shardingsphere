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

import org.apache.shardingsphere.infra.binder.context.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.FirebirdSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.FirebirdShowVariableExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.statement.firebird.dal.FirebirdSetStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.dal.FirebirdShowStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.dml.FirebirdDeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.dml.FirebirdInsertStatement;
import org.apache.shardingsphere.sql.parser.statement.firebird.dml.FirebirdSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdAdminExecutorCreatorTest {

    @Test
    void assertCreateWithOtherSQLStatementContextOnly() {
        assertThat(new FirebirdAdminExecutorCreator().create(new UnknownSQLStatementContext(new FirebirdInsertStatement())), is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithShowSQLStatement() {
        Optional<DatabaseAdminExecutor> actual = new FirebirdAdminExecutorCreator().create(new UnknownSQLStatementContext(new FirebirdShowStatement("server_version")));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(FirebirdShowVariableExecutor.class));
    }
    
    @Test
    void assertCreateWithSelectNonSystem() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(new FirebirdSelectStatement());
        assertThat(new FirebirdAdminExecutorCreator().create(selectStatementContext, "select 1", "", Collections.emptyList()), is(Optional.empty()));
    }
    
    @Test
    void assertCreateWithSetStatement() {
        FirebirdSetStatement setStatement = new FirebirdSetStatement();
        UnknownSQLStatementContext sqlStatementContext = new UnknownSQLStatementContext(setStatement);
        Optional<DatabaseAdminExecutor> actual = new FirebirdAdminExecutorCreator().create(sqlStatementContext, "SET NAMES utf8", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(FirebirdSetVariableAdminExecutor.class));
    }
    
    @Test
    void assertCreateWithDMLStatement() {
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(new FirebirdDeleteStatement());
        assertThat(new FirebirdAdminExecutorCreator().create(sqlStatementContext, "delete from t where id = 1", "", Collections.emptyList()), is(Optional.empty()));
    }
}
