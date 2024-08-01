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

package org.apache.shardingsphere.test.e2e.engine.type.dql;

import org.apache.shardingsphere.test.e2e.engine.context.SingleE2EContext;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.framework.type.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.engine.composer.E2EContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@E2ETestCaseSettings(SQLCommandType.DQL)
class GeneralDQLE2EIT extends BaseDQLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecuteQuery(final AssertionTestParameter testParam) throws SQLException, IOException, JAXBException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        E2EContainerComposer containerComposer = new E2EContainerComposer(testParam.getKey(), testParam.getScenario(), testParam.getDatabaseType(),
                AdapterMode.valueOf(testParam.getMode().toUpperCase()), AdapterType.valueOf(testParam.getAdapter().toUpperCase()));
        SingleE2EContext singleE2EContext = new SingleE2EContext(testParam);
        init(testParam, containerComposer, singleE2EContext);
        assertExecuteQuery(testParam, containerComposer, singleE2EContext);
    }
    
    private void assertExecuteQuery(final AssertionTestParameter testParam, final E2EContainerComposer containerComposer, final SingleE2EContext singleE2EContext) throws SQLException {
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXmlExpected(testParam, containerComposer, singleE2EContext);
        } else {
            assertExecuteQueryWithExpectedDataSource(testParam, containerComposer, singleE2EContext);
        }
    }
    
    private void assertExecuteQueryWithXmlExpected(final AssertionTestParameter testParam, final E2EContainerComposer containerComposer,
                                                   final SingleE2EContext singleE2EContext) throws SQLException {
        // TODO Fix jdbc adapter and empty_storage_units proxy adapter
        if ("jdbc".equals(testParam.getAdapter()) && !"empty_storage_units".equalsIgnoreCase(testParam.getScenario())
                || "proxy".equals(testParam.getAdapter()) && "empty_storage_units".equalsIgnoreCase(testParam.getScenario())) {
            return;
        }
        try (
                Connection connection = containerComposer.getTargetDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(singleE2EContext.getSQL())) {
            assertResultSet(singleE2EContext, resultSet);
        }
    }
    
    private void assertExecuteQueryWithExpectedDataSource(final AssertionTestParameter testParam, final E2EContainerComposer containerComposer,
                                                          final SingleE2EContext singleE2EContext) throws SQLException {
        try (
                Connection actualConnection = containerComposer.getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == singleE2EContext.getSqlExecuteType()) {
                assertExecuteQueryForStatement(singleE2EContext, actualConnection, expectedConnection, testParam);
            } else {
                assertExecuteQueryForPreparedStatement(singleE2EContext, actualConnection, expectedConnection, testParam);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final SingleE2EContext singleE2EContext, final Connection actualConnection, final Connection expectedConnection,
                                                final AssertionTestParameter testParam) throws SQLException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                ResultSet actualResultSet = actualStatement.executeQuery(singleE2EContext.getSQL());
                Statement expectedStatement = expectedConnection.createStatement();
                ResultSet expectedResultSet = expectedStatement.executeQuery(singleE2EContext.getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet, testParam);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final SingleE2EContext singleE2EContext, final Connection actualConnection, final Connection expectedConnection,
                                                        final AssertionTestParameter testParam) throws SQLException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(singleE2EContext.getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(singleE2EContext.getSQL())) {
            for (SQLValue each : singleE2EContext.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery();
                    ResultSet expectedResultSet = expectedPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        E2EContainerComposer containerComposer = new E2EContainerComposer(testParam.getKey(), testParam.getScenario(), testParam.getDatabaseType(),
                AdapterMode.valueOf(testParam.getMode().toUpperCase()), AdapterType.valueOf(testParam.getAdapter().toUpperCase()));
        SingleE2EContext singleE2EContext = new SingleE2EContext(testParam);
        init(testParam, containerComposer, singleE2EContext);
        assertExecute(testParam, containerComposer, singleE2EContext);
    }
    
    private void assertExecute(final AssertionTestParameter testParam, final E2EContainerComposer containerComposer, final SingleE2EContext singleE2EContext) throws SQLException {
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXmlExpected(testParam, containerComposer, singleE2EContext);
        } else {
            assertExecuteWithExpectedDataSource(testParam, containerComposer, singleE2EContext);
        }
    }
    
    private void assertExecuteWithXmlExpected(final AssertionTestParameter testParam, final E2EContainerComposer containerComposer, final SingleE2EContext singleE2EContext) throws SQLException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(testParam.getAdapter())) {
            return;
        }
        try (
                Connection connection = containerComposer.getTargetDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            assertTrue(statement.execute(singleE2EContext.getSQL()), "Not a query statement.");
            ResultSet resultSet = statement.getResultSet();
            assertResultSet(singleE2EContext, resultSet);
        }
    }
    
    private void assertExecuteWithExpectedDataSource(final AssertionTestParameter testParam, final E2EContainerComposer containerComposer,
                                                     final SingleE2EContext singleE2EContext) throws SQLException {
        try (
                Connection actualConnection = containerComposer.getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == singleE2EContext.getSqlExecuteType()) {
                assertExecuteForStatement(singleE2EContext, actualConnection, expectedConnection, testParam);
            } else {
                assertExecuteForPreparedStatement(singleE2EContext, actualConnection, expectedConnection, testParam);
            }
        }
    }
    
    private void assertExecuteForStatement(final SingleE2EContext singleE2EContext, final Connection actualConnection, final Connection expectedConnection,
                                           final AssertionTestParameter testParam) throws SQLException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                Statement expectedStatement = expectedConnection.createStatement()) {
            assertTrue(actualStatement.execute(singleE2EContext.getSQL()) && expectedStatement.execute(singleE2EContext.getSQL()), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final SingleE2EContext singleE2EContext, final Connection actualConnection, final Connection expectedConnection,
                                                   final AssertionTestParameter testParam) throws SQLException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(singleE2EContext.getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(singleE2EContext.getSQL())) {
            for (SQLValue each : singleE2EContext.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue(actualPreparedStatement.execute() && expectedPreparedStatement.execute(), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter();
    }
}
