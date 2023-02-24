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
import org.apache.shardingsphere.test.e2e.env.runtime.IntegrationTestEnvironment;
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
import java.util.Collections;

import static org.junit.Assert.assertTrue;

@ParallelRunningStrategy(ParallelLevel.CASE)
public final class AdditionalDQLE2EIT extends BaseDQLE2EIT {
    
    public AdditionalDQLE2EIT(final AssertionTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<AssertionTestParameter> getTestParameters() {
        return IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases() ? E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.DQL) : Collections.emptyList();
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXMLExpected(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            assertExecuteQueryWithExpectedDataSource(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
    }
    
    @Test
    public void assertExecuteQueryWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXMLExpected(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } else {
            assertExecuteQueryWithExpectedDataSource(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrency() throws SQLException, ParseException {
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXMLExpected(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            assertExecuteWithExpectedDataSource(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
    }
    
    @Test
    public void assertExecuteWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException, ParseException {
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXMLExpected(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } else {
            assertExecuteWithExpectedDataSource(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        }
    }
    
    private boolean isPostgreSQLOrOpenGauss(final String databaseType) {
        return "PostgreSQL".equals(databaseType) || "openGauss".equals(databaseType);
    }
    
    private void assertExecuteQueryWithXMLExpected(final int... resultSetTypes) throws SQLException, ParseException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(getAdapter())) {
            return;
        }
        try (
                Connection connection = getTargetDataSource().getConnection();
                Statement statement = 2 == resultSetTypes.length ? connection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : connection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet resultSet = statement.executeQuery(getSQL())) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryWithExpectedDataSource(final int... resultSetTypes) throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypes(actualConnection, expectedConnection, resultSetTypes);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypes(actualConnection, expectedConnection, resultSetTypes);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypes(
                                                                  final Connection actualConnection, final Connection expectedConnection,
                                                                  final int... resultSetTypes) throws SQLException, ParseException {
        try (
                Statement actualStatement = 2 == resultSetTypes.length ? actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet actualResultSet = actualStatement.executeQuery(getSQL());
                Statement expectedStatement = 2 == resultSetTypes.length ? expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet expectedResultSet = expectedStatement.executeQuery(getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypes(final Connection actualConnection, final Connection expectedConnection,
                                                                          final int... resultSetTypes) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = 2 == resultSetTypes.length ? actualConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                PreparedStatement expectedPreparedStatement = 2 == resultSetTypes.length ? expectedConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
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
    
    private void assertExecuteWithXMLExpected(final int... resultSetTypes) throws SQLException, ParseException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(getAdapter())) {
            return;
        }
        try (
                Connection connection = getTargetDataSource().getConnection();
                Statement statement = 2 == resultSetTypes.length ? connection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : connection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            assertTrue("Not a query statement.", statement.execute(getSQL()));
            ResultSet resultSet = statement.getResultSet();
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteWithExpectedDataSource(final int... resultSetTypes) throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypes(actualConnection, expectedConnection, resultSetTypes);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypes(actualConnection, expectedConnection, resultSetTypes);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypes(
                                                             final Connection actualConnection, final Connection expectedConnection, final int... resultSetTypes) throws SQLException, ParseException {
        try (
                Statement actualStatement = 2 == resultSetTypes.length ? actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                Statement expectedStatement = 2 == resultSetTypes.length ? expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            assertTrue("Not a query statement.", actualStatement.execute(getSQL()) && expectedStatement.execute(getSQL()));
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypes(final Connection actualConnection, final Connection expectedConnection,
                                                                     final int... resultSetTypes) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = 2 == resultSetTypes.length ? actualConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                PreparedStatement expectedPreparedStatement = 2 == resultSetTypes.length ? expectedConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.prepareStatement(getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
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
