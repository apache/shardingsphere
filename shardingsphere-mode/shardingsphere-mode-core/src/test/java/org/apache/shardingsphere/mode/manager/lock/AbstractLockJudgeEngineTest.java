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

public final class AbstractLockJudgeEngineTest {
    
    private AbstractLockJudgeEngine engine;
    
    @Before
    public void setUp() {
        engine = (AbstractLockJudgeEngine) LockJudgeEngineBuilder.build(mock(LockContext.class));
    }
    
    @Test
    public void assertIsWriteDDLStatement() {
        assertTrue(engine.isWriteStatement(mock(DDLStatement.class)));
        assertTrue(engine.isWriteStatement(mock(InsertStatement.class)));
        assertTrue(engine.isWriteStatement(mock(UpdateStatement.class)));
        assertTrue(engine.isWriteStatement(mock(DeleteStatement.class)));
        assertFalse(engine.isWriteStatement(mock(SelectStatement.class)));
    }
}
