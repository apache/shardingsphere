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

package org.apache.shardingsphere.test.e2e.engine.rdl;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.engine.SingleE2EITContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.E2EITExtension;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(E2EITExtension.class)
public final class GeneralRDLE2EIT extends BaseRDLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecute(final AssertionTestParameter testParam) throws SQLException, ParseException {
        // TODO make sure RDL test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        try (SingleE2EITContainerComposer containerComposer = new SingleE2EITContainerComposer(testParam)) {
            init(containerComposer);
            assertExecute(testParam, containerComposer);
            tearDown(containerComposer);
        }
    }
    
    private void assertExecute(final AssertionTestParameter testParam, final SingleE2EITContainerComposer containerComposer) throws SQLException, ParseException {
        assertNotNull(testParam.getAssertion().getAssertionSQL(), "Assertion SQL is required");
        try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                executeSQLCase(containerComposer, statement);
                sleep();
                assertResultSet(containerComposer, statement);
            }
        }
    }
    
    private void executeSQLCase(final SingleE2EITContainerComposer containerComposer, final Statement statement) throws SQLException, ParseException {
        statement.execute(containerComposer.getSQL());
    }
    
    private void assertResultSet(final SingleE2EITContainerComposer containerComposer, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(containerComposer.getAssertion().getAssertionSQL().getSql())) {
            assertResultSet(containerComposer, resultSet);
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RDL).isEmpty();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<AssertionTestParameter> result = E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RDL);
            // TODO make sure RDL test case can not be null
            return result.isEmpty() ? Stream.of(Arguments.of(new AssertionTestParameter(null, null, null, null, null, null, null))) : result.stream().map(Arguments::of);
        }
    }
}
