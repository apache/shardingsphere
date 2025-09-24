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
import org.apache.shardingsphere.test.e2e.sql.cases.casse.assertion.SQLE2ETestCaseAssertion;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITArgumentsProvider;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.CaseTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SQLE2EITSettings(value = SQLCommandType.DML, batch = true)
class BatchDMLE2EIT extends BaseDMLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteBatch(final CaseTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        init(testParam);
        try {
            int[] actualUpdateCounts;
            try (Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection()) {
                actualUpdateCounts = executeBatchForPreparedStatement(testParam, connection);
            }
            assertDataSet(actualUpdateCounts, testParam);
        } finally {
            tearDown(testParam);
        }
    }
    
    void init(final CaseTestParameter testParam) throws SQLException, IOException, JAXBException {
        super.init(testParam);
        executeInitSQLs(testParam);
    }
    
    void tearDown(final CaseTestParameter testParam) throws SQLException {
        tearDown();
        executeDestroySQLs(testParam);
    }
    
    private void executeInitSQLs(final CaseTestParameter testParam) throws SQLException {
        for (SQLE2ETestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
            executeInitSQLs(each);
        }
    }
    
    private void executeDestroySQLs(final CaseTestParameter testParam) throws SQLException {
        for (SQLE2ETestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
            executeDestroySQLs(each);
        }
    }
    
    private int[] executeBatchForPreparedStatement(final CaseTestParameter testParam, final Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(testParam.getTestCaseContext().getTestCase().getSql())) {
            for (SQLE2ETestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
                addBatch(preparedStatement, each);
            }
            return preparedStatement.executeBatch();
        }
    }
    
    private void addBatch(final PreparedStatement preparedStatement, final SQLE2ETestCaseAssertion assertion) throws SQLException {
        for (SQLValue each : assertion.getSQLValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
        preparedStatement.addBatch();
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertClearBatch(final CaseTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        init(testParam);
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(testParam.getTestCaseContext().getTestCase().getSql())) {
            for (SQLE2ETestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
                addBatch(preparedStatement, each);
            }
            preparedStatement.clearBatch();
            assertThat(preparedStatement.executeBatch().length, is(0));
        } finally {
            tearDown(testParam);
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid();
    }
}
