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

package org.apache.shardingsphere.test.integration.showprocesslist.engine;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.showprocesslist.container.composer.ClusterShowProcessListContainerComposer;
import org.apache.shardingsphere.test.integration.showprocesslist.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.showprocesslist.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.test.integration.showprocesslist.parameter.ShowProcessListParameterized;
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
import static org.junit.Assert.assertTrue;

// TODO add workflow, add jdbc
@RunWith(Parameterized.class)
public final class ShowProcessListIT {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final ClusterShowProcessListContainerComposer containerComposer;
    
    public ShowProcessListIT(final ShowProcessListParameterized parameterized) {
        containerComposer = new ClusterShowProcessListContainerComposer(parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ShowProcessListParameterized> getParameters() {
        Collection<ShowProcessListParameterized> result = new LinkedList<>();
        ENV.getScenarios().forEach(each -> {
            if (ITEnvTypeEnum.DOCKER == ENV.getItEnvType()) {
                result.add(new ShowProcessListParameterized(new MySQLDatabaseType(), each));
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
        CompletableFuture<Void> executeSelectSleep1 = CompletableFuture.runAsync(getExecuteSleepThread("proxy"));
        CompletableFuture<Void> executeSelectSleep2 = CompletableFuture.runAsync(getExecuteSleepThread("proxy"));
        CompletableFuture<Void> executeSelectSleep3 = CompletableFuture.runAsync(getExecuteSleepThread("proxy"));
        Thread.sleep(100);
        try (
                Connection connection = containerComposer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("show processlist");
            assertResultSet(resultSet);
        }
        CompletableFuture.allOf(executeSelectSleep1, executeSelectSleep2, executeSelectSleep3).join();
    }
    
    private void assertResultSet(final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData());
        assertRows(resultSet);
    }
    
    private void assertMetaData(final ResultSetMetaData metaData) throws SQLException {
        assertThat(metaData.getColumnCount(), is(8));
    }
    
    private void assertRows(final ResultSet resultSet) throws SQLException {
        assertTrue(resultSet.next());
        assertTrue(resultSet.next());
        assertTrue(resultSet.next());
    }
    
    private Runnable getExecuteSleepThread(final String targetContainer) {
        return () -> {
            try (
                    Connection connection = "proxy".equals(targetContainer) ? containerComposer.getProxyDataSource().getConnection() : containerComposer.getJdbcDataSource().getConnection();
                    Statement statement = connection.createStatement()) {
                statement.executeQuery("select sleep(10)");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
