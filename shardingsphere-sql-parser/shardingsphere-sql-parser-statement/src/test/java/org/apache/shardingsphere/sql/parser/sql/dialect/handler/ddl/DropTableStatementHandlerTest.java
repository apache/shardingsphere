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
    public void assertContainsIfExistsForMySQL() {
        assertTrue(DropTableStatementHandler.ifExists(new MySQLDropTableStatement(true)));
    }
    
    @Test
    public void assertContainsIfExistsForPostgreSQL() {
        assertTrue(DropTableStatementHandler.ifExists(new PostgreSQLDropTableStatement(true, false)));
    }
    
    @Test
    public void assertContainsIfExistsForSQLServer() {
        assertTrue(DropTableStatementHandler.ifExists(new SQLServerDropTableStatement(true)));
    }
    
    @Test
    public void assertNotContainsIfExistsForMySQL() {
        assertFalse(DropTableStatementHandler.ifExists(new MySQLDropTableStatement(false)));
    }
    
    @Test
    public void assertNotContainsIfExistsForOracle() {
        assertFalse(DropTableStatementHandler.ifExists(new OracleDropTableStatement()));
    }
    
    @Test
    public void assertNotContainsIfExistsForPostgreSQL() {
        assertFalse(DropTableStatementHandler.ifExists(new PostgreSQLDropTableStatement(false, false)));
    }
    
    @Test
    public void assertNotContainsIfExistsForSQL92() {
        assertFalse(DropTableStatementHandler.ifExists(new SQL92DropTableStatement()));
    }
    
    @Test
    public void assertNotContainsIfExistsForSQLServer() {
        assertFalse(DropTableStatementHandler.ifExists(new SQLServerDropTableStatement(false)));
    }
}
