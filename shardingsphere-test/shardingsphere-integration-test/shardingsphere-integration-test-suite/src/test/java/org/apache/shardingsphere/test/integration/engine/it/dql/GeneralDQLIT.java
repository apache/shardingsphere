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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.engine.param.ParameterizedArrayFactory;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public final class GeneralDQLIT extends BaseDQLIT {
    
    private final IntegrationTestCaseAssertion assertion;
    
    public GeneralDQLIT(final String parentPath, final IntegrationTestCaseAssertion assertion, final String adapter, final String scenario,
                        final String databaseType, final SQLExecuteType sqlExecuteType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, adapter, scenario, DatabaseTypeRegistry.getActualDatabaseType(databaseType), sqlExecuteType, sql);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{2}: {3} -> {4} -> {5} -> {6}")
    public static Collection<Object[]> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterizedArray(SQLCommandType.DQL);
    }
    
    @Test
    public void assertExecuteQuery() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatement(connection);
            } else {
                assertExecuteQueryForPreparedStatement(connection);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getSql())) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatement(connection);
            } else {
                assertExecuteForPreparedStatement(connection);
            }
        }
    }
    
    private void assertExecuteForStatement(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            assertTrue("Not a query statement.", statement.execute(getSql()));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
}
