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

package org.apache.shardingsphere.test.e2e.showprocesslist.engine;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.showprocesslist.container.composer.ClusterShowProcessListContainerComposer;
import org.apache.shardingsphere.test.e2e.showprocesslist.env.enums.ShowProcessListEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.showprocesslist.parameter.ShowProcessListTestParameter;
import org.apache.shardingsphere.test.e2e.showprocesslist.env.ShowProcessListEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// TODO add jdbc
@RunWith(Parameterized.class)
public final class ShowProcessListE2EIT {
    
    private static final ShowProcessListEnvironment ENV = ShowProcessListEnvironment.getInstance();
    
    private static final String SELECT_SLEEP = "select sleep(10)";
    
    private final ClusterShowProcessListContainerComposer containerComposer;
    
    public ShowProcessListE2EIT(final ShowProcessListTestParameter testParam) {
        containerComposer = new ClusterShowProcessListContainerComposer(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ShowProcessListTestParameter> getTestParameters() {
        Collection<ShowProcessListTestParameter> result = new LinkedList<>();
        ENV.getScenarios().forEach(each -> {
            if (ShowProcessListEnvTypeEnum.DOCKER == ENV.getItEnvType()) {
                for (String runMode : ENV.getRunModes()) {
                    result.add(new ShowProcessListTestParameter(new MySQLDatabaseType(), each, runMode));
                }
            }
        });
        return result;
    }
    
    @Before
    public void setUp() {
        containerComposer.start();
    }
    
    @After
    public void closeContainers() {
        containerComposer.stop();
    }
    
    @Test
    public void assertShowProcessList() throws SQLException, InterruptedException {
        CompletableFuture<Void> executeSelectSleep = CompletableFuture.runAsync(getExecuteSleepThread("proxy"));
        Thread.sleep(5000);
        try (
                Connection connection = containerComposer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("show processlist");
            assertResultSet(resultSet);
        }
        executeSelectSleep.join();
    }
    
    private Runnable getExecuteSleepThread(final String targetContainer) {
        return () -> {
            try (
                    Connection connection = "proxy".equals(targetContainer) ? containerComposer.getProxyDataSource().getConnection() : containerComposer.getJdbcDataSource().getConnection();
                    Statement statement = connection.createStatement()) {
                statement.executeQuery(SELECT_SLEEP);
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
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
}
