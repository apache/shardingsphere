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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertStatementHandlerTest {
    
    @Test
    void assertGetOnDuplicateKeyColumnsSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList()));
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertTrue(onDuplicateKeyColumnsSegment.isPresent());
        assertThat(onDuplicateKeyColumnsSegment.get(), is(insertStatement.getOnDuplicateKeyColumns().get()));
        assertFalse(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(new MySQLInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetOnDuplicateKeyColumnsSegmentForOpenGauss() {
        OpenGaussInsertStatement insertStatement = new OpenGaussInsertStatement();
        insertStatement.setOnDuplicateKeyColumnsSegment(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList()));
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertTrue(onDuplicateKeyColumnsSegment.isPresent());
        assertThat(onDuplicateKeyColumnsSegment.get(), is(insertStatement.getOnDuplicateKeyColumns().get()));
        assertFalse(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(new OpenGaussInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetOnDuplicateKeyColumnsSegmentForPostgreSQL() {
        PostgreSQLInsertStatement insertStatement = new PostgreSQLInsertStatement();
        insertStatement.setOnDuplicateKeyColumnsSegment(new OnDuplicateKeyColumnsSegment(0, 0, Collections.emptyList()));
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement);
        assertTrue(onDuplicateKeyColumnsSegment.isPresent());
        assertThat(onDuplicateKeyColumnsSegment.get(), is(insertStatement.getOnDuplicateKeyColumns().get()));
        assertFalse(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(new PostgreSQLInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetOnDuplicateKeyColumnsSegmentForOtherDatabases() {
        assertFalse(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(new OracleInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(new SQL92InsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getOnDuplicateKeyColumnsSegment(new SQLServerInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetSetAssignmentSegmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(insertStatement);
        assertTrue(setAssignmentSegment.isPresent());
        assertThat(setAssignmentSegment.get(), is(insertStatement.getSetAssignment().get()));
        assertFalse(InsertStatementHandler.getSetAssignmentSegment(new MySQLInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetSetAssignmentSegmentForOtherDatabases() {
        assertFalse(InsertStatementHandler.getSetAssignmentSegment(new OpenGaussInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getSetAssignmentSegment(new OracleInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getSetAssignmentSegment(new PostgreSQLInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getSetAssignmentSegment(new SQL92InsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getSetAssignmentSegment(new SQLServerInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForPostgreSQL() {
        PostgreSQLInsertStatement insertStatement = new PostgreSQLInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(insertStatement.getWithSegment().get()));
        assertFalse(InsertStatementHandler.getWithSegment(new PostgreSQLInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForSQLServer() {
        SQLServerInsertStatement insertStatement = new SQLServerInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(insertStatement.getWithSegment().get()));
        assertFalse(InsertStatementHandler.getWithSegment(new SQLServerInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForOpenGauss() {
        OpenGaussInsertStatement insertStatement = new OpenGaussInsertStatement();
        insertStatement.setWithSegment(new WithSegment(0, 0, Collections.emptyList()));
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(insertStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(insertStatement.getWithSegment().get()));
        assertFalse(InsertStatementHandler.getWithSegment(new OpenGaussInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForOtherDatabases() {
        assertFalse(InsertStatementHandler.getWithSegment(new MySQLInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getWithSegment(new OracleInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getWithSegment(new SQL92InsertStatement()).isPresent());
    }
    
    @Test
    void assertGetOutputSegmentForSQLServer() {
        SQLServerInsertStatement insertStatement = new SQLServerInsertStatement();
        insertStatement.setOutputSegment(new OutputSegment(0, 0));
        Optional<OutputSegment> outputSegment = InsertStatementHandler.getOutputSegment(insertStatement);
        assertTrue(outputSegment.isPresent());
        assertThat(outputSegment.get(), is(insertStatement.getOutputSegment().get()));
        assertFalse(InsertStatementHandler.getOutputSegment(new SQLServerInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetOutputSegmentForOtherDatabases() {
        assertFalse(InsertStatementHandler.getOutputSegment(new MySQLInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getOutputSegment(new OpenGaussInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getOutputSegment(new OracleInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getOutputSegment(new PostgreSQLInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getOutputSegment(new SQL92InsertStatement()).isPresent());
    }
    
    @Test
    void assertGetInsertMultiTableElementSegmentForOracle() {
        OracleInsertStatement insertStatement = new OracleInsertStatement();
        insertStatement.setInsertMultiTableElementSegment(new InsertMultiTableElementSegment(0, 0));
        Optional<InsertMultiTableElementSegment> insertMultiTableElementSegment = InsertStatementHandler.getInsertMultiTableElementSegment(insertStatement);
        assertTrue(insertMultiTableElementSegment.isPresent());
        assertThat(insertMultiTableElementSegment.get(), is(insertStatement.getInsertMultiTableElementSegment().get()));
        assertFalse(InsertStatementHandler.getInsertMultiTableElementSegment(new SQLServerInsertStatement()).isPresent());
    }
    
    @Test
    void assertGetInsertMultiTableElementSegmentForOtherDatabases() {
        assertFalse(InsertStatementHandler.getInsertMultiTableElementSegment(new MySQLInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getInsertMultiTableElementSegment(new OpenGaussInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getInsertMultiTableElementSegment(new PostgreSQLInsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getInsertMultiTableElementSegment(new SQL92InsertStatement()).isPresent());
        assertFalse(InsertStatementHandler.getInsertMultiTableElementSegment(new SQLServerInsertStatement()).isPresent());
    }
}
