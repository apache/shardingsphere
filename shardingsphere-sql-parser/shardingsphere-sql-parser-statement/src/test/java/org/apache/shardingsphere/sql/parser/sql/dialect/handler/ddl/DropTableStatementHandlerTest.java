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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTableStatement;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DropTableStatementHandlerTest {
    
    @Test
    public void assertContainsIfExistClauseForMySQL() {
        assertTrue(DropTableStatementHandler.containsExistClause(new MySQLDropTableStatement(true)));
    }
    
    @Test
    public void assertContainsIfExistClauseForPostgreSQL() {
        assertTrue(DropTableStatementHandler.containsExistClause(new PostgreSQLDropTableStatement(true)));
    }
    
    @Test
    public void assertContainsIfExistClauseForSQLServer() {
        assertTrue(DropTableStatementHandler.containsExistClause(new SQLServerDropTableStatement(true)));
    }
    
    @Test
    public void assertNotContainsIfExistClauseForMySQL() {
        assertFalse(DropTableStatementHandler.containsExistClause(new MySQLDropTableStatement(false)));
    }
    
    @Test
    public void assertNotContainsIfExistClauseForOracle() {
        assertFalse(DropTableStatementHandler.containsExistClause(new OracleDropTableStatement()));
    }
    
    @Test
    public void assertNotContainsIfExistClauseForPostgreSQL() {
        assertFalse(DropTableStatementHandler.containsExistClause(new PostgreSQLDropTableStatement(false)));
    }
    
    @Test
    public void assertNotContainsIfExistClauseForSQL92() {
        assertFalse(DropTableStatementHandler.containsExistClause(new SQL92DropTableStatement()));
    }
    
    @Test
    public void assertNotContainsIfExistClauseForSQLServer() {
        assertFalse(DropTableStatementHandler.containsExistClause(new SQLServerDropTableStatement(false)));
    }
}
