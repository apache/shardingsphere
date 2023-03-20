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
import org.apache.shardingsphere.test.e2e.cases.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
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

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(E2EITExtension.class)
public final class GeneralDMLE2EIT extends BaseDMLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecuteUpdate(final AssertionTestParameter testParam) throws SQLException, ParseException, JAXBException, IOException {
        // TODO make sure DML test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        try (SingleE2EITContainerComposer containerComposer = new SingleE2EITContainerComposer(testParam)) {
            init(testParam, containerComposer);
            int actualUpdateCount;
            try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
                actualUpdateCount = SQLExecuteType.Literal == containerComposer.getSqlExecuteType()
                        ? executeUpdateForStatement(containerComposer, connection)
                        : executeUpdateForPreparedStatement(containerComposer, connection);
            }
            assertDataSet(testParam, containerComposer, actualUpdateCount);
        }
    }
    
    private int executeUpdateForStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(containerComposer.getSQL());
        }
    }
    
    private int executeUpdateForPreparedStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(containerComposer.getSQL())) {
            for (SQLValue each : containerComposer.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecute(final AssertionTestParameter testParam) throws SQLException, ParseException, JAXBException, IOException {
        // TODO make sure DML test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        try (SingleE2EITContainerComposer containerComposer = new SingleE2EITContainerComposer(testParam)) {
            init(testParam, containerComposer);
            int actualUpdateCount;
            try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
                actualUpdateCount = SQLExecuteType.Literal == containerComposer.getSqlExecuteType()
                        ? executeForStatement(containerComposer, connection)
                        : executeForPreparedStatement(containerComposer, connection);
            }
            assertDataSet(testParam, containerComposer, actualUpdateCount);
        }
    }
    
    private int executeForStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(containerComposer.getSQL()), "Not a DML statement.");
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
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
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<AssertionTestParameter> result = E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.DML);
            // TODO make sure DML test case can not be null
            return result.isEmpty() ? Stream.of(Arguments.of(new AssertionTestParameter(null, null, null, null, null, null, null))) : result.stream().map(Arguments::of);
        }
    }
}
