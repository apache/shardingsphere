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

package org.apache.shardingsphere.test.integration.engine.it.dql;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.junit.compose.ComposeManager;
import org.apache.shardingsphere.test.integration.junit.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.junit.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@ParallelRuntimeStrategy(ParallelLevel.CASE)
public final class AdditionalDQLIT extends BaseDQLIT {
    
    @ClassRule
    public static ComposeManager composeManager = new ComposeManager("AdditionalDQLIT");
    
    public AdditionalDQLIT(final AssertionParameterizedArray parameter) {
        super(parameter);
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<AssertionParameterizedArray> getParameters() {
        if (IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases()) {
            return ParameterizedArrayFactory.getAssertionParameterized(SQLCommandType.DQL)
                    .stream()
                    .peek(each -> each.setCompose(composeManager.getOrCreateCompose(each)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrency(connection);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrency(connection);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (
                Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(String.format(getSQL(), getAssertion().getSQLValues().toArray()))) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection)
            throws SQLException, ParseException {
        try (
                Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                ResultSet resultSet = statement.executeQuery(String.format(getSQL(), getAssertion().getSQLValues().toArray()))) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection)
            throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypeAndResultSetConcurrency(connection);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrency(connection);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            assertTrue("Not a query statement.", statement.execute(String.format(getSQL(), getAssertion().getSQLValues().toArray())));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrency(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(connection);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            assertTrue("Not a query statement.", statement.execute(String.format(getSQL(), getAssertion().getSQLValues().toArray())));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
}
