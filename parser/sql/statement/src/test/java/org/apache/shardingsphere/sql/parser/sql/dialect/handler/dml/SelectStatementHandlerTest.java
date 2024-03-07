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

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectStatementHandlerTest {
    
    @Test
    void assertGetLimitSegmentForMySQL() {
        MySQLGenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new MySQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForPostgreSQL() {
        PostgreSQLGenericSelectStatement selectStatement = new PostgreSQLGenericSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new PostgreSQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForSQL92() {
        SQL92GenericSelectStatement selectStatement = new SQL92GenericSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new SQL92GenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForSQLServer() {
        SQLServerGenericSelectStatement selectStatement = new SQLServerGenericSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new SQLServerGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForOpenGauss() {
        OpenGaussGenericSelectStatement selectStatement = new OpenGaussGenericSelectStatement();
        selectStatement.setLimit(new LimitSegment(1, 2, new NumberLiteralLimitValueSegment(0, 3, 5L), new NumberLiteralLimitValueSegment(0, 3, 2L)));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new OpenGaussGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForOracle() {
        assertFalse(SelectStatementHandler.getLimitSegment(new OracleGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForMySQL() {
        MySQLGenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new MySQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForOracle() {
        OracleGenericSelectStatement selectStatement = new OracleGenericSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new OracleGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForPostgreSQL() {
        PostgreSQLGenericSelectStatement selectStatement = new PostgreSQLGenericSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new PostgreSQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForOpenGauss() {
        OpenGaussGenericSelectStatement selectStatement = new OpenGaussGenericSelectStatement();
        selectStatement.setLock(new LockSegment(0, 2));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new OpenGaussGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getLockSegment(new SQL92GenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getLockSegment(new SQLServerGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForMySQL() {
        MySQLGenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 0));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new MySQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForPostgreSQL() {
        PostgreSQLGenericSelectStatement selectStatement = new PostgreSQLGenericSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 0));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new PostgreSQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForOpenGauss() {
        OpenGaussGenericSelectStatement selectStatement = new OpenGaussGenericSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 2));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertThat(windowSegment.get(), is(selectStatement.getWindow().get()));
        assertFalse(SelectStatementHandler.getWindowSegment(new OpenGaussGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getWindowSegment(new OracleGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new SQL92GenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new SQLServerGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForOracle() {
        OracleGenericSelectStatement selectStatement = new OracleGenericSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new OracleGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForMysql() {
        MySQLGenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new MySQLGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForSQLServer() {
        SQLServerGenericSelectStatement selectStatement = new SQLServerGenericSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new SQLServerGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getWithSegment(new MySQLGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new OpenGaussGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new PostgreSQLGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new SQL92GenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetModelSegmentForOracle() {
        OracleGenericSelectStatement selectStatement = new OracleGenericSelectStatement();
        selectStatement.setModelSegment(new ModelSegment(0, 2));
        Optional<ModelSegment> modelSegment = SelectStatementHandler.getModelSegment(selectStatement);
        assertTrue(modelSegment.isPresent());
        assertThat(modelSegment.get(), is(selectStatement.getModelSegment().get()));
        assertFalse(SelectStatementHandler.getModelSegment(new OracleGenericSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetModelSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getModelSegment(new MySQLGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new OpenGaussGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new PostgreSQLGenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new SQL92GenericSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new SQLServerGenericSelectStatement()).isPresent());
    }
}
