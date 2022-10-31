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

package org.apache.shardingsphere.test.integration.engine.dql;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.SQLExecuteType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.env.runtime.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.framework.param.array.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

@ParallelRuntimeStrategy(ParallelLevel.CASE)
public final class AdditionalDQLIT extends BaseDQLIT {
    
    public AdditionalDQLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameters(name = "{0}")
    public static Collection<AssertionParameterizedArray> getParameters() {
        return IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases() ? ParameterizedArrayFactory.getAssertionParameterized(SQLCommandType.DQL) : Collections.emptyList();
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrency(actualConnection, expectedConnection);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrency(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrency(
                                                                                        final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet actualResultSet = actualStatement.executeQuery(String.format(getSQL(), getAssertion().getSQLValues().toArray()));
                Statement expectedStatement = expectedConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet expectedResultSet = expectedStatement.executeQuery(String.format(getSQL(), getAssertion().getSQLValues().toArray()))) {
            assertResultSet(actualResultSet, expectedResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrency(
                                                                                                final Connection actualConnection,
                                                                                                final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery();
                    ResultSet expectedResultSet = expectedPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(actualConnection, expectedConnection);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(
                                                                                                               final Connection actualConnection,
                                                                                                               final Connection expectedConnection) throws SQLException, ParseException {
        String sql = String.format(getSQL(), getAssertion().getSQLValues().toArray());
        try (
                Statement actualStatement = actualConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                ResultSet actualResultSet = actualStatement.executeQuery(sql);
                Statement expectedStatement = expectedConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                ResultSet expectedResultSet = expectedStatement.executeQuery(sql)) {
            assertResultSet(actualResultSet, expectedResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(
                                                                                                                       final Connection actualConnection,
                                                                                                                       final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(
                        getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery();
                    ResultSet expectedResultSet = expectedPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypeAndResultSetConcurrency(actualConnection, expectedConnection);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrency(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypeAndResultSetConcurrency(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                Statement expectedStatement = expectedConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            String sql = String.format(getSQL(), getAssertion().getSQLValues().toArray());
            assertTrue("Not a query statement.", actualStatement.execute(sql) && expectedStatement.execute(sql));
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrency(
                                                                                           final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", actualPreparedStatement.execute() && expectedPreparedStatement.execute());
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(actualConnection, expectedConnection);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(
                                                                                                          final Connection actualConnection,
                                                                                                          final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                Statement expectedStatement = expectedConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            String sql = String.format(getSQL(), getAssertion().getSQLValues().toArray());
            assertTrue("Not a query statement.", actualStatement.execute(sql) && expectedStatement.execute(sql));
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(
                                                                                                                  final Connection actualConnection,
                                                                                                                  final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(
                        getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", actualPreparedStatement.execute() && expectedPreparedStatement.execute());
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
}
