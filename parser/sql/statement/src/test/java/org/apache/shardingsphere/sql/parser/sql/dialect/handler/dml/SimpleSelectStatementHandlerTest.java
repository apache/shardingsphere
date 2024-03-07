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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSimpleSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleSelectStatementHandlerTest {
    
    @Test
    void assertGetLimitSegmentForMySQL() {
        MySQLSimpleSelectStatement selectStatement = new MySQLSimpleSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new MySQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForPostgreSQL() {
        PostgreSQLSimpleSelectStatement selectStatement = new PostgreSQLSimpleSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new PostgreSQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForSQL92() {
        SQL92SimpleSelectStatement selectStatement = new SQL92SimpleSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new SQL92SimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForSQLServer() {
        SQLServerSimpleSelectStatement selectStatement = new SQLServerSimpleSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new SQLServerSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForOpenGauss() {
        OpenGaussSimpleSelectStatement selectStatement = new OpenGaussSimpleSelectStatement();
        selectStatement.setLimit(new LimitSegment(1, 2, new NumberLiteralLimitValueSegment(0, 3, 5L), new NumberLiteralLimitValueSegment(0, 3, 2L)));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new OpenGaussSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLimitSegmentForOracle() {
        assertFalse(SelectStatementHandler.getLimitSegment(new OracleSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForMySQL() {
        MySQLSimpleSelectStatement selectStatement = new MySQLSimpleSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new MySQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForOracle() {
        OracleSimpleSelectStatement selectStatement = new OracleSimpleSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new OracleSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForPostgreSQL() {
        PostgreSQLSimpleSelectStatement selectStatement = new PostgreSQLSimpleSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new PostgreSQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForOpenGauss() {
        OpenGaussSimpleSelectStatement selectStatement = new OpenGaussSimpleSelectStatement();
        selectStatement.setLock(new LockSegment(0, 2));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new OpenGaussSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetLockSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getLockSegment(new SQL92SimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getLockSegment(new SQLServerSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForMySQL() {
        MySQLSimpleSelectStatement selectStatement = new MySQLSimpleSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 0));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new MySQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForPostgreSQL() {
        PostgreSQLSimpleSelectStatement selectStatement = new PostgreSQLSimpleSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 0));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new PostgreSQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForOpenGauss() {
        OpenGaussSimpleSelectStatement selectStatement = new OpenGaussSimpleSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 2));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertThat(windowSegment.get(), is(selectStatement.getWindow().get()));
        assertFalse(SelectStatementHandler.getWindowSegment(new OpenGaussSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWindowSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getWindowSegment(new OracleSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new SQL92SimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new SQLServerSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForOracle() {
        OracleSimpleSelectStatement selectStatement = new OracleSimpleSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new OracleSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForMysql() {
        MySQLSimpleSelectStatement selectStatement = new MySQLSimpleSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new MySQLSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForSQLServer() {
        SQLServerSimpleSelectStatement selectStatement = new SQLServerSimpleSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new SQLServerSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetWithSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getWithSegment(new MySQLSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new OpenGaussSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new PostgreSQLSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new SQL92SimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetModelSegmentForOracle() {
        OracleSimpleSelectStatement selectStatement = new OracleSimpleSelectStatement();
        selectStatement.setModelSegment(new ModelSegment(0, 2));
        Optional<ModelSegment> modelSegment = SelectStatementHandler.getModelSegment(selectStatement);
        assertTrue(modelSegment.isPresent());
        assertThat(modelSegment.get(), is(selectStatement.getModelSegment().get()));
        assertFalse(SelectStatementHandler.getModelSegment(new OracleSimpleSelectStatement()).isPresent());
    }
    
    @Test
    void assertGetModelSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getModelSegment(new MySQLSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new OpenGaussSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new PostgreSQLSimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new SQL92SimpleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new SQLServerSimpleSelectStatement()).isPresent());
    }
}
