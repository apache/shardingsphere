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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SelectStatementHandlerTest {
    
    @Test
    public void assertGetLimitSegmentWithLimitSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithoutLimitSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertFalse(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithLimitSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithoutLimitSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertFalse(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithLimitSegmentForSQL92() {
        SQL92SelectStatement selectStatement = new SQL92SelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithoutLimitSegmentForSQL92() {
        SQL92SelectStatement selectStatement = new SQL92SelectStatement();
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertFalse(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithLimitSegmentForSQLServer() {
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertTrue(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentWithoutLimitSegmentForSQLServer() {
        SQLServerSelectStatement selectStatement = new SQLServerSelectStatement();
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        assertFalse(limitSegment.isPresent());
    }
    
    @Test
    public void assertGetLockSegmentWithLockSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
    }
    
    @Test
    public void assertGetLockSegmentWithoutLockSegmentForMySQL() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertFalse(lockSegment.isPresent());
    }
    
    @Test
    public void assertGetLockSegmentWithLockSegmentForOracle() {
        OracleSelectStatement selectStatement = new OracleSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
    }
    
    @Test
    public void assertGetLockSegmentWithoutLockSegmentForOracle() {
        OracleSelectStatement selectStatement = new OracleSelectStatement();
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertFalse(lockSegment.isPresent());
    }
    
    @Test
    public void assertGetLockSegmentWithLockSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        selectStatement.setLock(new LockSegment(0, 0));
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertTrue(lockSegment.isPresent());
    }
    
    @Test
    public void assertGetLockSegmentWithoutLockSegmentForPostgreSQL() {
        PostgreSQLSelectStatement selectStatement = new PostgreSQLSelectStatement();
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        assertFalse(lockSegment.isPresent());
    }
}
