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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dql;

import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EITContext;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Focused JDBC E2E for issue #28841: HintManager data-source hint + table.* with alias column labels.
 */
@SQLE2EITSettings(SQLCommandType.DQL)
class HintManagerColumnLabelE2EIT extends BaseDQLE2EIT {
    
    @ParameterizedTest(name = "{0}", allowZeroInvocations = true)
    @Execution(ExecutionMode.SAME_THREAD)
    @EnabledIf("isEnabled")
    @ArgumentsSource(HintManagerColumnLabelArgumentsProvider.class)
    void assertColumnLabelAccessibleWithHintManager(final AssertionTestParameter testParam) throws SQLException, IOException, JAXBException {
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        executeDQL(context, () -> {
            init(testParam, context);
            assertDoesNotThrow(() -> executeShorthandAliasQuery(false),
                    "Without data-source hint, SELECT T.*, T.status status_new should allow getObject by bare labels");
            assertDoesNotThrow(() -> executeShorthandAliasQuery(true),
                    "HintManager.setDataSourceName(write_ds) + SELECT T.*, T.status status_new should allow getObject by bare labels");
        });
    }
    
    private void executeShorthandAliasQuery(final boolean useDataSourceHint) throws SQLException {
        if (useDataSourceHint) {
            try (
                    HintManager hintManager = HintManager.getInstance();
                    Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                    Statement statement = connection.createStatement()) {
                hintManager.setDataSourceName(HintManagerColumnLabelArgumentsProvider.HINT_DATA_SOURCE_NAME);
                try (ResultSet resultSet = statement.executeQuery(HintManagerColumnLabelArgumentsProvider.TARGET_SQL)) {
                    assertTrue(resultSet.next(), "Expected row for order_id=1000");
                    assertBareColumnLabelsAccessible(resultSet);
                }
            }
            return;
        }
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(HintManagerColumnLabelArgumentsProvider.TARGET_SQL)) {
            assertTrue(resultSet.next(), "Expected row for order_id=1000");
            assertBareColumnLabelsAccessible(resultSet);
        }
    }
    
    private void assertBareColumnLabelsAccessible(final ResultSet resultSet) throws SQLException {
        resultSet.getObject("order_id");
        resultSet.getObject("user_id");
        resultSet.getObject("status");
        resultSet.getObject("status_new");
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid();
    }
}
