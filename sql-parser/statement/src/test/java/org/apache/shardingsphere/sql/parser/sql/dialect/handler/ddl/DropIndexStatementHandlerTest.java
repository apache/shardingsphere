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

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropIndexStatement;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DropIndexStatementHandlerTest {
    
    @Test
    public void assertGetSimpleTableSegmentWithSimpleTableSegmentForMySQL() {
        MySQLDropIndexStatement dropIndexStatement = new MySQLDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(dropIndexStatement);
        assertTrue(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentWithoutSimpleTableSegmentForMySQL() {
        MySQLDropIndexStatement dropIndexStatement = new MySQLDropIndexStatement();
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(dropIndexStatement);
        assertFalse(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentWithSimpleTableSegmentForSQLServer() {
        SQLServerDropIndexStatement dropIndexStatement = new SQLServerDropIndexStatement(false);
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(dropIndexStatement);
        assertTrue(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentWithoutSimpleTableSegmentForSQLServer() {
        SQLServerDropIndexStatement dropIndexStatement = new SQLServerDropIndexStatement(false);
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(dropIndexStatement);
        assertFalse(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentForOtherDatabases() {
        assertFalse(DropIndexStatementHandler.getSimpleTableSegment(new OpenGaussDropIndexStatement(true)).isPresent());
        assertFalse(DropIndexStatementHandler.getSimpleTableSegment(new OracleDropIndexStatement()).isPresent());
        assertFalse(DropIndexStatementHandler.getSimpleTableSegment(new PostgreSQLDropIndexStatement(true)).isPresent());
    }
    
    @Test
    public void assertIfExistsForPostgres() {
        assertFalse(DropIndexStatementHandler.ifExists(new PostgreSQLDropIndexStatement(false)));
        assertTrue(DropIndexStatementHandler.ifExists(new PostgreSQLDropIndexStatement(true)));
    }
    
    @Test
    public void assertIfExistsForSQLServer() {
        assertFalse(DropIndexStatementHandler.ifExists(new SQLServerDropIndexStatement(false)));
        assertTrue(DropIndexStatementHandler.ifExists(new SQLServerDropIndexStatement(true)));
    }
    
    @Test
    public void assertIfExistsForOpenGauss() {
        assertFalse(DropIndexStatementHandler.ifExists(new OpenGaussDropIndexStatement(false)));
        assertTrue(DropIndexStatementHandler.ifExists(new OpenGaussDropIndexStatement(true)));
    }
    
    @Test
    public void assertIfExistsForOtherDatabases() {
        assertFalse(DropIndexStatementHandler.ifExists(new MySQLDropIndexStatement()));
        assertFalse(DropIndexStatementHandler.ifExists(new OracleDropIndexStatement()));
    }
}
