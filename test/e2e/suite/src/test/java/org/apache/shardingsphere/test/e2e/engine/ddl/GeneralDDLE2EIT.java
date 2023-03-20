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

package org.apache.shardingsphere.test.e2e.engine.ddl;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.SQLExecuteType;
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(E2EITExtension.class)
public final class GeneralDDLE2EIT extends BaseDDLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecuteUpdate(final AssertionTestParameter testParam) throws SQLException, ParseException {
        try (SingleE2EITContainerComposer containerComposer = new SingleE2EITContainerComposer(testParam)) {
            init(containerComposer);
            try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
                if (SQLExecuteType.Literal == containerComposer.getSqlExecuteType()) {
                    executeUpdateForStatement(containerComposer, connection);
                } else {
                    executeUpdateForPreparedStatement(containerComposer, connection);
                }
                assertTableMetaData(testParam, containerComposer);
            }
            tearDown(containerComposer);
        }
    }
    
    private void executeUpdateForStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse(statement.executeUpdate(containerComposer.getSQL()) > 0, "Not a DDL statement.");
        }
        waitCompleted();
    }
    
    private void executeUpdateForPreparedStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(containerComposer.getSQL())) {
            assertFalse(preparedStatement.executeUpdate() > 0, "Not a DDL statement.");
        }
        waitCompleted();
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecute(final AssertionTestParameter testParam) throws Exception {
        try (SingleE2EITContainerComposer containerComposer = new SingleE2EITContainerComposer(testParam)) {
            init(containerComposer);
            try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
                if (SQLExecuteType.Literal == containerComposer.getSqlExecuteType()) {
                    executeForStatement(containerComposer, connection);
                } else {
                    executeForPreparedStatement(containerComposer, connection);
                }
                assertTableMetaData(testParam, containerComposer);
            }
            tearDown(containerComposer);
        }
    }
    
    private void executeForStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse(statement.execute(containerComposer.getSQL()), "Not a DDL statement.");
        }
        waitCompleted();
    }
    
    private void executeForPreparedStatement(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(containerComposer.getSQL())) {
            assertFalse(preparedStatement.execute(), "Not a DDL statement.");
        }
        waitCompleted();
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.DDL).stream().map(Arguments::of);
        }
    }
}
