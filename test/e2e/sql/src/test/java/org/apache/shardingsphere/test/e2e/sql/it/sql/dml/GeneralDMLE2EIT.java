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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dml;

import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITArgumentsProvider;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EITContext;
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

@SQLE2EITSettings(SQLCommandType.DML)
class GeneralDMLE2EIT extends BaseDMLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteUpdate(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam);
        try {
            int actualUpdateCount;
            try (Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection()) {
                actualUpdateCount = SQLExecuteType.LITERAL == context.getSqlExecuteType()
                        ? executeUpdateForStatement(context, connection)
                        : executeUpdateForPreparedStatement(context, connection);
            }
            assertDataSet(context, actualUpdateCount, testParam);
        } finally {
            tearDown(context);
        }
    }
    
    void init(final AssertionTestParameter testParam) throws SQLException, IOException, JAXBException {
        super.init(testParam);
        executeInitSQLs(testParam.getAssertion());
    }
    
    void tearDown(final SQLE2EITContext context) throws SQLException {
        tearDown();
        executeDestroySQLs(context.getAssertion());
    }
    
    private int executeUpdateForStatement(final SQLE2EITContext context, final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(context.getSQL());
        }
    }
    
    private int executeUpdateForPreparedStatement(final SQLE2EITContext context, final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(context.getSQL())) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam);
        try {
            int actualUpdateCount;
            try (Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection()) {
                actualUpdateCount = SQLExecuteType.LITERAL == context.getSqlExecuteType()
                        ? executeForStatement(context, connection)
                        : executeForPreparedStatement(context, connection);
            }
            assertDataSet(context, actualUpdateCount, testParam);
        } finally {
            tearDown(context);
        }
    }
    
    private int executeForStatement(final SQLE2EITContext context, final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(context.getSQL()), "Not a DML statement.");
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final SQLE2EITContext context, final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(context.getSQL())) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse(preparedStatement.execute(), "Not a DML statement.");
            return preparedStatement.getUpdateCount();
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid();
    }
}
