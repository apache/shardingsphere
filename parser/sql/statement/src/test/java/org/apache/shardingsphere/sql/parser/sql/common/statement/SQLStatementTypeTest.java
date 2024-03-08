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

package org.apache.shardingsphere.sql.parser.sql.common.statement;

import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SQLStatementTypeTest {
    
    @Test
    void assertInvolvesDataChangesWithSelectStatement() {
        assertFalse(SQLStatementType.involvesDataChanges(mock(SelectStatement.class)));
    }
    
    @Test
    void assertNotInvolvesDataChangesWithUpdateStatement() {
        assertTrue(SQLStatementType.involvesDataChanges(mock(UpdateStatement.class)));
    }
    
    @Test
    void assertNotInvolvesDataChangesWithDDLStatement() {
        assertTrue(SQLStatementType.involvesDataChanges(mock(CreateTableStatement.class)));
    }
    
    @Test
    void assertInvolvesDataChangesWithOtherStatement() {
        assertFalse(SQLStatementType.involvesDataChanges(mock(ShowStatement.class)));
    }
    
    @Test
    void assertInvolvesDataChangesWithCache() {
        assertFalse(SQLStatementType.involvesDataChanges(mock(SelectStatement.class)));
        assertFalse(SQLStatementType.involvesDataChanges(mock(SelectStatement.class)));
    }
    
    @Test
    void assertNotInvolvesDataChangesWithCache() {
        assertTrue(SQLStatementType.involvesDataChanges(mock(DeleteStatement.class)));
        assertTrue(SQLStatementType.involvesDataChanges(mock(DeleteStatement.class)));
    }
}
