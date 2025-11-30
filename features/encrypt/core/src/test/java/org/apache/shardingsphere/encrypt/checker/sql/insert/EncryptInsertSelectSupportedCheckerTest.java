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

package org.apache.shardingsphere.encrypt.checker.sql.insert;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture.EncryptGeneratorFixtureBuilder;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptInsertSelectSupportedCheckerTest {
    
    @Test
    void assertIsCheck() {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getInsertSelect().isPresent()).thenReturn(true);
        assertTrue(new EncryptInsertSelectSupportedChecker().isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithoutCombineClause() {
        assertFalse(new EncryptInsertSelectSupportedChecker().isCheck(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertCheckWithoutEncryptTable() {
        assertDoesNotThrow(() -> new EncryptInsertSelectSupportedChecker()
                .check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, mockInsertStatementContext("t_order")));
    }
    
    @Test
    void assertCheckWithEncryptTable() {
        assertThrows(UnsupportedEncryptSQLException.class, () -> new EncryptInsertSelectSupportedChecker()
                .check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, mockInsertStatementContext("t_user")));
    }
    
    private InsertStatementContext mockInsertStatementContext(final String tableName) {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(Collections.singleton(tableName));
        return result;
    }
}
