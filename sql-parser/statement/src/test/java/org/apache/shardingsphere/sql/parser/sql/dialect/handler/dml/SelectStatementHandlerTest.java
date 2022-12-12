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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.util.Optional;

public final class SelectStatementHandlerTest {
    
    @Test
    public void assertGetLimitSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new MySQLSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new PostgreSQLSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForSQL92() {
        SQL92SelectStatement selectStatement = new SQL92SelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new SQL92SelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForSQLServer() {
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new SQLServerSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForOpenGauss() {
        OpenGaussSelectStatement selectStatement = new OpenGaussSelectStatement();
        selectStatement.setLimit(new LimitSegment(1, 2, new NumberLiteralLimitValueSegment(0, 3, 5L), new NumberLiteralLimitValueSegment(0, 3, 2L)));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(selectStatement.getLimit().get()));
        assertFalse(SelectStatementHandler.getLimitSegment(new OpenGaussSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForOracle() {
        assertFalse(SelectStatementHandler.getLimitSegment(new OracleSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLockSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new MySQLSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLockSegmentForOracle() {
        OracleSelectStatement selectStatement = new OracleSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new OracleSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLockSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new PostgreSQLSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLockSegmentForOpenGauss() {
        OpenGaussSelectStatement selectStatement = new OpenGaussSelectStatement();
        selectStatement.setLock(new LockSegment(0, 2));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
        assertThat(lockSegment.get(), is(selectStatement.getLock().get()));
        assertFalse(SelectStatementHandler.getLockSegment(new OpenGaussSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetLockSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getLockSegment(new SQL92SelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getLockSegment(new SQLServerSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWindowSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 0));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new MySQLSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWindowSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 0));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new PostgreSQLSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWindowSegmentForOpenGauss() {
        OpenGaussSelectStatement selectStatement = new OpenGaussSelectStatement();
        selectStatement.setWindow(new WindowSegment(0, 2));
        Optional<WindowSegment> windowSegment = SelectStatementHandler.getWindowSegment(selectStatement);
        assertTrue(windowSegment.isPresent());
        assertThat(windowSegment.get(), is(selectStatement.getWindow().get()));
        assertFalse(SelectStatementHandler.getWindowSegment(new OpenGaussSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWindowSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getWindowSegment(new OracleSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new SQL92SelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWindowSegment(new SQLServerSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWithSegmentForOracle() {
        OracleSelectStatement selectStatement = new OracleSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new OracleSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWithSegmentForSQLServer() {
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setWithSegment(new WithSegment(0, 2, new LinkedList<>()));
        Optional<WithSegment> withSegment = SelectStatementHandler.getWithSegment(selectStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(selectStatement.getWithSegment().get()));
        assertFalse(SelectStatementHandler.getWithSegment(new SQLServerSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetWithSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getWithSegment(new MySQLSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new OpenGaussSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new PostgreSQLSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getWithSegment(new SQL92SelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetModelSegmentForOracle() {
        OracleSelectStatement selectStatement = new OracleSelectStatement();
        selectStatement.setModelSegment(new ModelSegment(0, 2));
        Optional<ModelSegment> modelSegment = SelectStatementHandler.getModelSegment(selectStatement);
        assertTrue(modelSegment.isPresent());
        assertThat(modelSegment.get(), is(selectStatement.getModelSegment().get()));
        assertFalse(SelectStatementHandler.getModelSegment(new OracleSelectStatement()).isPresent());
    }
    
    @Test
    public void assertGetModelSegmentForOtherDatabases() {
        assertFalse(SelectStatementHandler.getModelSegment(new MySQLSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new OpenGaussSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new PostgreSQLSelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new SQL92SelectStatement()).isPresent());
        assertFalse(SelectStatementHandler.getModelSegment(new SQLServerSelectStatement()).isPresent());
    }
}
