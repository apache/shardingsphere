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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class InsertStatementHandlerTest {
    
    @Test
    public void assertGetOnDuplicateKeyColumnsSegmentWithOnDuplicateKeyColumnsSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList()));
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertTrue(onDuplicateKeyColumnsSegment.isPresent());
    }
    
    @Test
    public void assertGetOnDuplicateKeyColumnsSegmentWithoutOnDuplicateKeyColumnsSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertFalse(onDuplicateKeyColumnsSegment.isPresent());
    }
    
    @Test
    public void assertGetSetAssignmentSegmentWithSetAssignmentSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        assertTrue(setAssignmentSegment.isPresent());
    }
    
    @Test
    public void assertGetSetAssignmentSegmentWithoutSetAssignmentSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        assertFalse(setAssignmentSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithWithSegmentForPostgreSQL() {
        PostgreSQLInsertStatement insertStatement = new PostgreSQLInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithoutWithSegmentForPostgreSQL() {
        PostgreSQLInsertStatement insertStatement = new PostgreSQLInsertStatement();
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertFalse(withSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithWithSegmentForSQLServer() {
        SQLServerInsertStatement insertStatement = new SQLServerInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
    }
    
    @Test
    public void assertGetWithSegmentWithoutWithSegmentForSQLServer() {
        SQLServerInsertStatement insertStatement = new SQLServerInsertStatement();
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertFalse(withSegment.isPresent());
    }
}
