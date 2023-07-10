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

package org.apache.shardingsphere.test.e2e.engine.type.dml;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;

@E2ETestCaseSettings(SQLCommandType.DML)
class GeneralDMLE2EIT extends BaseDMLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecuteUpdate(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(testParam, containerComposer);
        int actualUpdateCount;
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLExecuteType.Literal == containerComposer.getSqlExecuteType()
                    ? executeUpdateForStatement(containerComposer, connection)
                    : executeUpdateForPreparedStatement(containerComposer, connection);
        }
        assertDataSet(testParam, containerComposer, actualUpdateCount);
    }
    
    private int executeUpdateForStatement(final SingleE2EContainerComposer containerComposer, final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(containerComposer.getSQL());
        }
    }
    
    private int executeUpdateForPreparedStatement(final SingleE2EContainerComposer containerComposer, final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(containerComposer.getSQL())) {
            for (SQLValue each : containerComposer.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
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
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(testParam, containerComposer);
        int actualUpdateCount;
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLExecuteType.Literal == containerComposer.getSqlExecuteType()
                    ? executeForStatement(containerComposer, connection)
                    : executeForPreparedStatement(containerComposer, connection);
        }
        assertDataSet(testParam, containerComposer, actualUpdateCount);
    }
    
    private int executeForStatement(final SingleE2EContainerComposer containerComposer, final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(containerComposer.getSQL()), "Not a DML statement.");
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final SingleE2EContainerComposer containerComposer, final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(containerComposer.getSQL())) {
            for (SQLValue each : containerComposer.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse(preparedStatement.execute(), "Not a DML statement.");
            return preparedStatement.getUpdateCount();
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter();
    }
}
