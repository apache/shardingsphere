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

package org.apache.shardingsphere.test.e2e.it.sql.dcl;

import lombok.Setter;
import org.apache.shardingsphere.test.e2e.it.SQLE2EIT;
import org.apache.shardingsphere.test.e2e.env.SQLE2EEnvironmentEngine;
import org.apache.shardingsphere.test.e2e.framework.SQLE2EITArgumentsProvider;
import org.apache.shardingsphere.test.e2e.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.it.SQLE2EITContext;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.authority.AuthorityEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioCommonPath;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.framework.type.SQLExecuteType;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@SQLE2EITSettings(SQLCommandType.DCL)
@Setter
class DCLE2EIT implements SQLE2EIT {
    
    private SQLE2EEnvironmentEngine environmentEngine;
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteUpdate(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        try (
                AuthorityEnvironmentManager ignored = new AuthorityEnvironmentManager(
                        new ScenarioCommonPath(testParam.getScenario()).getAuthorityFile(), environmentEngine.getActualDataSourceMap(), testParam.getDatabaseType())) {
            assertExecuteUpdate(context);
        }
    }
    
    private void assertExecuteUpdate(final SQLE2EITContext context) throws SQLException {
        String sql = context.getSQL();
        try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
            if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                }
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.executeUpdate();
                }
            }
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
        try (
                AuthorityEnvironmentManager ignored = new AuthorityEnvironmentManager(
                        new ScenarioCommonPath(testParam.getScenario()).getAuthorityFile(), environmentEngine.getActualDataSourceMap(), testParam.getDatabaseType())) {
            assertExecute(context);
        }
    }
    
    private void assertExecute(final SQLE2EITContext context) throws SQLException {
        String sql = context.getSQL();
        try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
            if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.execute();
                }
            }
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter();
    }
}
