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

package org.apache.shardingsphere.proxy.backend.mysql.connector.sane;

import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowOtherStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLDialectSaneQueryResultEngineTest {
    
    @Test
    void assertGetSaneQueryResultForSyntaxError() {
        SQLException ex = new SQLException("", "", 1064, null);
        assertThat(new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(null, ex), is(Optional.empty()));
    }
    
    @Test
    void assertGetSaneQueryResultForSelectStatementWithFrom() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t"))));
        assertThat(new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(selectStatement, new SQLException()), is(Optional.empty()));
    }
    
    @Test
    void assertGetSaneQueryResultForSelectStatementWithoutFrom() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "@@session.transaction_read_only", new VariableSegment(0, 0, "transaction_read_only")));
        selectStatement.getProjections().getProjections().add(new ExpressionProjectionSegment(0, 0, "unknown_variable"));
        Optional<ExecuteResult> actual = new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(selectStatement, new SQLException());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(RawMemoryQueryResult.class));
        RawMemoryQueryResult actualResult = (RawMemoryQueryResult) actual.get();
        assertThat(actualResult.getRowCount(), is(1L));
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is("0"));
        assertThat(actualResult.getValue(2, String.class), is("1"));
        assertFalse(actualResult.next());
    }
    
    @Test
    void assertGetSaneQueryResultForSelectNoProjectionsStatementWithoutFrom() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        assertThat(new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(selectStatement, new SQLException()), is(Optional.empty()));
    }
    
    @Test
    void assertGetSaneQueryResultForSetStatement() {
        Optional<ExecuteResult> actual = new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(new MySQLSetStatement(), new SQLException());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UpdateResult.class));
    }
    
    @Test
    void assertGetSaneQueryResultForShowOtherStatement() {
        Optional<ExecuteResult> actual = new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(new MySQLShowOtherStatement(), new SQLException());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(RawMemoryQueryResult.class));
        RawMemoryQueryResult actualResult = (RawMemoryQueryResult) actual.get();
        assertThat(actualResult.getRowCount(), is(1L));
        assertTrue(actualResult.next());
        assertThat(actualResult.getValue(1, String.class), is("1"));
        assertFalse(actualResult.next());
    }
    
    @Test
    void assertGetSaneQueryResultForOtherStatements() {
        assertThat(new MySQLDialectSaneQueryResultEngine().getSaneQueryResult(new MySQLInsertStatement(), new SQLException()), is(Optional.empty()));
    }
    
    @Test
    void assertGetType() {
        assertThat(new MySQLDialectSaneQueryResultEngine().getType().getType(), is("MySQL"));
    }
}
