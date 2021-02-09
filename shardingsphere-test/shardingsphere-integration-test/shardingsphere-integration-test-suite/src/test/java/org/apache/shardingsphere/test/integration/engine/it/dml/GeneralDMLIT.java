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

package org.apache.shardingsphere.test.integration.engine.it.dml;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.engine.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;

import static org.junit.Assert.assertFalse;

public final class GeneralDMLIT extends BaseDMLIT {
    
    private final IntegrationTestCaseAssertion assertion;
    
    public GeneralDMLIT(final ParameterizedWrapper parameterizedWrapper) throws IOException, JAXBException, SQLException, ParseException {
        super(parameterizedWrapper.getTestCaseContext().getParentPath(),
                parameterizedWrapper.getAssertion(),
                parameterizedWrapper.getAdapter(),
                parameterizedWrapper.getScenario(),
                parameterizedWrapper.getDatabaseType(),
                parameterizedWrapper.getSqlExecuteType(),
                parameterizedWrapper.getTestCaseContext().getTestCase().getSql());
        this.assertion = parameterizedWrapper.getAssertion();
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterizedArray(SQLCommandType.DML);
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException, ParseException {
        // TODO fix replica-query
        if ("replica_query".equals(getScenario())) {
            return;
        }
        // TODO fix shadow
        if ("shadow".equals(getScenario())) {
            return;
        }
        // TODO fix encrypt
        if ("encrypt".equals(getScenario())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLExecuteType.Literal == getSqlExecuteType() ? executeUpdateForStatement(connection) : executeUpdateForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeUpdateForStatement(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(getSql());
        }
    }
    
    private int executeUpdateForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getScenario())) {
            return;
        }
        // TODO fix shadow
        if ("shadow".equals(getScenario())) {
            return;
        }
        // TODO fix encrypt
        if ("encrypt".equals(getScenario())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLExecuteType.Literal == getSqlExecuteType() ? executeForStatement(connection) : executeForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatement(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(getSql()));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
}
