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
        MySQLDropTableStatement dropTableStatement = new MySQLDropTableStatement();
        dropTableStatement.setContainsIfExistClause(true);
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertTrue(containsIfExistClause);
    }
    
    @Test
    public void assertContainsIfExistClauseForPostgreSQL() {
        PostgreSQLDropTableStatement dropTableStatement = new PostgreSQLDropTableStatement();
        dropTableStatement.setContainsIfExistClause(true);
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertTrue(containsIfExistClause);
    }
    
    @Test
    public void assertContainsIfExistClauseForSQLServer() {
        SQLServerDropTableStatement dropTableStatement = new SQLServerDropTableStatement();
        dropTableStatement.setContainsIfExistClause(true);
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertTrue(containsIfExistClause);
    }
    
    @Test
    public void assertNotContainsIfExistClauseForMySQL() {
        MySQLDropTableStatement dropTableStatement = new MySQLDropTableStatement();
        dropTableStatement.setContainsIfExistClause(false);
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertFalse(containsIfExistClause);
    }
    
    @Test
    public void assertNotContainsIfExistClauseForOracle() {
        OracleDropTableStatement dropTableStatement = new OracleDropTableStatement();
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertFalse(containsIfExistClause);
    }
    
    @Test
    public void assertNotContainsIfExistClauseForPostgreSQL() {
        PostgreSQLDropTableStatement dropTableStatement = new PostgreSQLDropTableStatement();
        dropTableStatement.setContainsIfExistClause(false);
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertFalse(containsIfExistClause);
    }
    
    @Test
    public void assertNotContainsIfExistClauseForSQL92() {
        SQL92DropTableStatement dropTableStatement = new SQL92DropTableStatement();
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertFalse(containsIfExistClause);
    }
    
    @Test
    public void assertNotContainsIfExistClauseForSQLServer() {
        SQLServerDropTableStatement dropTableStatement = new SQLServerDropTableStatement();
        dropTableStatement.setContainsIfExistClause(false);
        boolean containsIfExistClause = DropTableStatementHandler.containsIfExistClause(dropTableStatement);
        assertFalse(containsIfExistClause);
    }
}
