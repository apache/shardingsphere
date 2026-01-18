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

package org.apache.shardingsphere.driver.executor.engine.distsql;

import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.PreviewStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverDistSQLExecutorTest {
    
    private final DriverDistSQLExecutor executor = new DriverDistSQLExecutor(null);
    
    @Test
    void assertIsQueryStatementForRQL() {
        assertTrue(executor.isQueryStatement(new ShowStorageUnitsStatement(null, null)));
    }
    
    @Test
    void assertIsQueryStatementForRUL() {
        assertTrue(executor.isQueryStatement(new ParseStatement("SELECT 1")));
    }
    
    @Test
    void assertIsQueryStatementForQueryableRAL() {
        assertTrue(executor.isQueryStatement(new ShowDistVariablesStatement(false, null)));
    }
    
    @Test
    void assertIsQueryStatementForNonQueryableRAL() {
        assertFalse(executor.isQueryStatement(new SetDistVariableStatement("sql_show", "true")));
    }
    
    @Test
    void assertIsQueryStatementForRDL() {
        assertFalse(executor.isQueryStatement(new RegisterStorageUnitStatement(false, Collections.emptyList(), Collections.emptyList())));
    }
    
    @Test
    void assertExecuteQueryRejectsPreviewStatement() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> executor.executeQuery(new PreviewStatement("SELECT 1"), null, null));
    }
    
    @Test
    void assertExecuteUpdateRejectsPreviewStatement() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> executor.executeUpdate(new PreviewStatement("SELECT 1"), null));
    }
    
    @Test
    void assertExecuteRejectsPreviewStatement() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> executor.execute(new PreviewStatement("SELECT 1"), null, null));
    }
}
