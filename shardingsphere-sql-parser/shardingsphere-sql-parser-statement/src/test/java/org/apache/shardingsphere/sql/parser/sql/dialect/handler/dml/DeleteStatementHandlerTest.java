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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerDeleteStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

public final class DeleteStatementHandlerTest {
    
    @Test
    public void assertGetOrderBySegmentForMySQL() {
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        deleteStatement.setOrderBy(new OrderBySegment(0, 0, Collections.emptyList()));
        Optional<OrderBySegment> orderBySegment = DeleteStatementHandler.getOrderBySegment(deleteStatement);
        assertTrue(orderBySegment.isPresent());
        assertThat(orderBySegment.get(), is(deleteStatement.getOrderBy().get()));
        assertFalse(DeleteStatementHandler.getOrderBySegment(new MySQLDeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetOrderBySegmentForOtherDatabases() {
        assertFalse(DeleteStatementHandler.getOrderBySegment(new OpenGaussDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOrderBySegment(new OracleDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOrderBySegment(new PostgreSQLDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOrderBySegment(new SQL92DeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOrderBySegment(new SQLServerDeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForMySQL() {
        MySQLDeleteStatement deleteStatement = new MySQLDeleteStatement();
        deleteStatement.setLimit(new LimitSegment(0, 0, null, null));
        Optional<LimitSegment> limitSegment = DeleteStatementHandler.getLimitSegment(deleteStatement);
        assertTrue(limitSegment.isPresent());
        assertThat(limitSegment.get(), is(deleteStatement.getLimit().get()));
        assertFalse(DeleteStatementHandler.getLimitSegment(new MySQLDeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetLimitSegmentForOtherDatabases() {
        assertFalse(DeleteStatementHandler.getLimitSegment(new OpenGaussDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getLimitSegment(new OracleDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getLimitSegment(new PostgreSQLDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getLimitSegment(new SQL92DeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getLimitSegment(new SQLServerDeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetOutputSegmentForSQLServer() {
        SQLServerDeleteStatement deleteStatement = new SQLServerDeleteStatement();
        deleteStatement.setOutputSegment(new OutputSegment(0, 0));
        Optional<OutputSegment> outputSegment = DeleteStatementHandler.getOutputSegment(deleteStatement);
        assertTrue(outputSegment.isPresent());
        assertThat(outputSegment.get(), is(deleteStatement.getOutputSegment().get()));
        assertFalse(DeleteStatementHandler.getOutputSegment(new SQLServerDeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetOutputSegmentForOtherDatabases() {
        assertFalse(DeleteStatementHandler.getOutputSegment(new MySQLDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOutputSegment(new OpenGaussDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOutputSegment(new OracleDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOutputSegment(new PostgreSQLDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getOutputSegment(new SQL92DeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetWithSegmentForSQLServer() {
        SQLServerDeleteStatement deleteStatement = new SQLServerDeleteStatement();
        deleteStatement.setWithSegment(new WithSegment(0, 0, new LinkedList<>()));
        Optional<WithSegment> withSegment = DeleteStatementHandler.getWithSegment(deleteStatement);
        assertTrue(withSegment.isPresent());
        assertThat(withSegment.get(), is(deleteStatement.getWithSegment().get()));
        assertFalse(DeleteStatementHandler.getWithSegment(new SQLServerDeleteStatement()).isPresent());
    }
    
    @Test
    public void assertGetWithSegmentForOtherDatabases() {
        assertFalse(DeleteStatementHandler.getWithSegment(new MySQLDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getWithSegment(new OpenGaussDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getWithSegment(new OracleDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getWithSegment(new PostgreSQLDeleteStatement()).isPresent());
        assertFalse(DeleteStatementHandler.getWithSegment(new SQL92DeleteStatement()).isPresent());
    }
}
