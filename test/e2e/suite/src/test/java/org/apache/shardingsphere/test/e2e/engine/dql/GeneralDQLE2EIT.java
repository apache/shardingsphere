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

package org.apache.shardingsphere.test.e2e.engine.dql;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.framework.runner.ParallelRunningStrategy;
import org.apache.shardingsphere.test.e2e.framework.runner.ParallelRunningStrategy.ParallelLevel;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ParallelRunningStrategy(ParallelLevel.CASE)
public final class GeneralDQLE2EIT extends BaseDQLE2EIT {
    
    public GeneralDQLE2EIT(final AssertionTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<AssertionTestParameter> getTestParameters() {
        return E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.DQL);
    }
    
    @Test
    public void assertExecuteQuery() throws SQLException, ParseException {
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXmlExpected();
        } else {
            assertExecuteQueryWithExpectedDataSource();
        }
    }
    
    private void assertExecuteQueryWithXmlExpected() throws SQLException, ParseException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(getAdapter())) {
            return;
        }
        try (
                Connection connection = getTargetDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getSQL())) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryWithExpectedDataSource() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatement(actualConnection, expectedConnection);
            } else {
                assertExecuteQueryForPreparedStatement(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                ResultSet actualResultSet = actualStatement.executeQuery(getSQL());
                Statement expectedStatement = expectedConnection.createStatement();
                ResultSet expectedResultSet = expectedStatement.executeQuery(getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(getSQL())) {
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
    public void assertExecute() throws SQLException, ParseException {
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXmlExpected();
        } else {
            assertExecuteWithExpectedDataSource();
        }
    }
    
    private void assertExecuteWithXmlExpected() throws SQLException, ParseException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(getAdapter())) {
            return;
        }
        try (
                Connection connection = getTargetDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            assertTrue(statement.execute(getSQL()), "Not a query statement.");
            ResultSet resultSet = statement.getResultSet();
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteWithExpectedDataSource() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatement(actualConnection, expectedConnection);
            } else {
                assertExecuteForPreparedStatement(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteForStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                Statement expectedStatement = expectedConnection.createStatement()) {
            assertTrue(actualStatement.execute(getSQL()) && expectedStatement.execute(getSQL()), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue(actualPreparedStatement.execute() && expectedPreparedStatement.execute(), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
}
