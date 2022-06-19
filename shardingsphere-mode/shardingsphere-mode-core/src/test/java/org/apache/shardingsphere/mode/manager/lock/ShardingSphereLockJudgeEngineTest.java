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

package org.apache.shardingsphere.mode.manager.lock;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereLockJudgeEngineTest {
    
    private LockJudgeEngine engine;
    
    @Before
    public void setUp() {
        LockContext lockContext = mock(LockContext.class);
        when(lockContext.isLocked("databaseName")).thenReturn(true);
        engine = LockJudgeEngineBuilder.build(lockContext);
    }
    
    @Test
    public void assertDDLIsLocked() {
        SQLStatementContext<DDLStatement> sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        assertTrue(engine.isLocked("databaseName", sqlStatementContext));
    }
    
    @Test
    public void assertInsertIsLocked() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        assertTrue(engine.isLocked("databaseName", insertStatementContext));
    }
    
    @Test
    public void assertUpdateIsLocked() {
        UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        when(updateStatementContext.getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        assertTrue(engine.isLocked("databaseName", updateStatementContext));
    }
    
    @Test
    public void assertDeleteIsLocked() {
        DeleteStatementContext deleteStatementContext = mock(DeleteStatementContext.class);
        when(deleteStatementContext.getSqlStatement()).thenReturn(mock(DeleteStatement.class));
        assertTrue(engine.isLocked("databaseName", deleteStatementContext));
    }
    
    @Test
    public void assertSelectIsLocked() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        assertFalse(engine.isLocked("databaseName", selectStatementContext));
    }
}
