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

package org.apache.shardingsphere.test.e2e.operation.showprocesslist.engine;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.showprocesslist.container.composer.ClusterShowProcessListContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.showprocesslist.parameter.ShowProcessListTestParameter;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// TODO add jdbc
class ShowProcessListE2EIT {
    
    private static final E2ETestEnvironment ENV = E2ETestEnvironment.getInstance();
    
    private static final String SELECT_SLEEP = "SELECT sleep(10)";
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertShowProcessList(final ShowProcessListTestParameter testParam) throws SQLException {
        try (ClusterShowProcessListContainerComposer containerComposer = new ClusterShowProcessListContainerComposer(testParam)) {
            containerComposer.start();
            CompletableFuture<Void> executeSelectSleep = CompletableFuture.runAsync(getExecuteSleepThread("proxy", containerComposer));
            Awaitility.await().pollDelay(5L, TimeUnit.SECONDS).until(() -> true);
            try (
                    Connection connection = containerComposer.getProxyDataSource().getConnection();
                    Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("show processlist");
                assertResultSet(resultSet);
            }
            executeSelectSleep.join();
        }
    }
    
    private Runnable getExecuteSleepThread(final String targetContainer, final ClusterShowProcessListContainerComposer containerComposer) {
        return () -> {
            try (
                    Connection connection = "proxy".equals(targetContainer) ? containerComposer.getProxyDataSource().getConnection() : containerComposer.getJdbcDataSource().getConnection();
                    Statement statement = connection.createStatement()) {
                statement.executeQuery(SELECT_SLEEP);
            } catch (final SQLException ex) {
                throw new SQLWrapperException(ex);
            }
        };
    }
    
    private void assertResultSet(final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData());
        assertRows(resultSet);
    }
    
    private void assertMetaData(final ResultSetMetaData metaData) throws SQLException {
        assertThat(metaData.getColumnCount(), is(8));
        assertThat(metaData.getColumnName(1), is("Id"));
        assertThat(metaData.getColumnName(2), is("User"));
        assertThat(metaData.getColumnName(3), is("Host"));
        assertThat(metaData.getColumnName(4), is("db"));
        assertThat(metaData.getColumnName(5), is("Command"));
        assertThat(metaData.getColumnName(6), is("Time"));
        assertThat(metaData.getColumnName(7), is("State"));
        assertThat(metaData.getColumnName(8), is("Info"));
    }
    
    private void assertRows(final ResultSet resultSet) throws SQLException {
        int count = 0;
        while (resultSet.next()) {
            if (SELECT_SLEEP.equals(resultSet.getObject(8).toString())) {
                assertThat(resultSet.getObject(5), is("Execute"));
                assertThat(resultSet.getObject(7), is("Executing 0/1"));
                count++;
            }
        }
        assertThat(count, is(1));
    }
    
    private static boolean isEnabled() {
        return Type.DOCKER == ENV.getRunEnvironment().getType();
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            Collection<Arguments> result = new LinkedList<>();
            for (String each : ENV.getScenarios()) {
                for (Mode mode : ENV.getArtifactEnvironment().getModes()) {
                    result.add(Arguments.of(new ShowProcessListTestParameter(TypedSPILoader.getService(DatabaseType.class, "MySQL"), each, mode, ENV.getArtifactEnvironment().getRegCenterType())));
                }
            }
            return result.stream();
        }
    }
}
