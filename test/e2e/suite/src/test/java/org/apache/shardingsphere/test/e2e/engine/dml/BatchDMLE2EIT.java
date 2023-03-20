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

package org.apache.shardingsphere.test.e2e.engine.dml;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.engine.BatchE2EITContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.CaseTestParameter;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class BatchDMLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecuteBatch(final CaseTestParameter testParam) throws SQLException, ParseException, JAXBException, IOException {
        // TODO make sure DML test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        try (BatchE2EITContainerComposer containerComposer = new BatchE2EITContainerComposer(testParam)) {
            int[] actualUpdateCounts;
            try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
                actualUpdateCounts = executeBatchForPreparedStatement(testParam, connection);
            }
            containerComposer.assertDataSets(actualUpdateCounts);
        }
    }
    
    private int[] executeBatchForPreparedStatement(final CaseTestParameter testParam, final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(testParam.getTestCaseContext().getTestCase().getSql())) {
            for (IntegrationTestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
                addBatch(preparedStatement, each);
            }
            return preparedStatement.executeBatch();
        }
    }
    
    private void addBatch(final PreparedStatement preparedStatement, final IntegrationTestCaseAssertion assertion) throws ParseException, SQLException {
        for (SQLValue each : assertion.getSQLValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
        preparedStatement.addBatch();
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertClearBatch(final CaseTestParameter testParam) throws SQLException, ParseException, JAXBException, IOException {
        // TODO make sure DML test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        try (BatchE2EITContainerComposer containerComposer = new BatchE2EITContainerComposer(testParam)) {
            try (
                    Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(testParam.getTestCaseContext().getTestCase().getSql())) {
                for (IntegrationTestCaseAssertion each : testParam.getTestCaseContext().getTestCase().getAssertions()) {
                    addBatch(preparedStatement, each);
                }
                preparedStatement.clearBatch();
                assertThat(preparedStatement.executeBatch().length, is(0));
            }
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<E2ETestParameter> result = E2ETestParameterFactory.getCaseTestParameters(SQLCommandType.DML);
            // TODO make sure DML test case can not be null
            return result.isEmpty() ? Stream.of(Arguments.of(new CaseTestParameter(null, null, null, null, null))) : result.stream().map(Arguments::of);
        }
    }
}
