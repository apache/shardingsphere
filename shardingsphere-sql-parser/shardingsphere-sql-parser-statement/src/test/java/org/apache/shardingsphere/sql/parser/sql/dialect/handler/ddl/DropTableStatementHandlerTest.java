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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTableStatement;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DropTableStatementHandlerTest {
    
    @Test
    public void assertIfExistsForMySQL() {
        assertTrue(DropTableStatementHandler.ifExists(new MySQLDropTableStatement(true)));
        assertFalse(DropTableStatementHandler.ifExists(new MySQLDropTableStatement(false)));
    }
    
    @Test
    public void assertIfExistsForPostgreSQL() {
        assertTrue(DropTableStatementHandler.ifExists(new PostgreSQLDropTableStatement(true, false)));
        assertFalse(DropTableStatementHandler.ifExists(new PostgreSQLDropTableStatement(false, false)));
    }
    
    @Test
    public void assertIfExistsForSQLServer() {
        assertTrue(DropTableStatementHandler.ifExists(new SQLServerDropTableStatement(true)));
        assertFalse(DropTableStatementHandler.ifExists(new SQLServerDropTableStatement(false)));
    }

    @Test
    public void assertIfExistsForGauss() {
        assertFalse(DropTableStatementHandler.ifExists(new OpenGaussDropTableStatement(false, true)));
        assertTrue(DropTableStatementHandler.ifExists(new OpenGaussDropTableStatement(true, true)));
    }

    @Test
    public void assertNotExistsForOtherDatabases() {
        assertFalse(DropTableStatementHandler.ifExists(new OracleDropTableStatement()));
        assertFalse(DropTableStatementHandler.ifExists(new SQL92DropTableStatement()));
    }

    @Test
    public void assertContainsCascadeForPostgres() {
        assertFalse(DropTableStatementHandler.containsCascade(new PostgreSQLDropTableStatement(false, false)));
        assertTrue(DropTableStatementHandler.containsCascade(new PostgreSQLDropTableStatement(true, true)));
    }

    @Test
    public void assertContainsCascadeForGauss() {
        assertFalse(DropTableStatementHandler.containsCascade(new OpenGaussDropTableStatement(false, false)));
        assertTrue(DropTableStatementHandler.containsCascade(new OpenGaussDropTableStatement(true, true)));
    }

    @Test
    public void assertNotContainsCascadeForOtherDatabases() {
        assertFalse(DropTableStatementHandler.containsCascade(new MySQLDropTableStatement(false)));
        assertFalse(DropTableStatementHandler.containsCascade(new OracleDropTableStatement()));
        assertFalse(DropTableStatementHandler.containsCascade(new SQL92DropTableStatement()));
        assertFalse(DropTableStatementHandler.containsCascade(new SQLServerDropTableStatement(false)));
    }
}
