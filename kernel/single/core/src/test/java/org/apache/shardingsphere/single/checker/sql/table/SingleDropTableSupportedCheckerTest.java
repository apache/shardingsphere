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

package org.apache.shardingsphere.single.checker.sql.table;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedDropCascadeTableException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleDropTableSupportedCheckerTest {
    
    private final SingleDropTableSupportedChecker checker = new SingleDropTableSupportedChecker();
    
    @Test
    void assertIsCheckWithDropTableStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DropTableStatement.class));
        assertTrue(checker.isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithoutDropTableStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        assertFalse(checker.isCheck(sqlStatementContext));
    }
    
    @Test
    void assertCheckWithCascade() {
        assertThrows(UnsupportedDropCascadeTableException.class,
                () -> checker.check(mock(SingleRule.class), mock(ShardingSphereDatabase.class), mock(ShardingSphereSchema.class), mockSQLStatementContext(true)));
    }
    
    @Test
    void assertCheckWithoutCascade() {
        assertDoesNotThrow(() -> checker.check(mock(SingleRule.class), mock(ShardingSphereDatabase.class), mock(ShardingSphereSchema.class), mockSQLStatementContext(false)));
    }
    
    private SQLStatementContext mockSQLStatementContext(final boolean containsCascade) {
        SQLStatementContext result = mock(SQLStatementContext.class);
        DropTableStatement sqlStatement = mock(DropTableStatement.class);
        when(sqlStatement.isContainsCascade()).thenReturn(containsCascade);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
}
