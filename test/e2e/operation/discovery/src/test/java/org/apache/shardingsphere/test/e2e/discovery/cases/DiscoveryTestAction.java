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

package org.apache.shardingsphere.test.e2e.discovery.cases;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.test.e2e.discovery.build.DiscoveryRuleBuilder;
import org.apache.shardingsphere.test.e2e.discovery.command.DiscoveryDistSQLCommand;
import org.awaitility.Awaitility;
import org.awaitility.Durations;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Discovery test action.
 */
public final class DiscoveryTestAction {
    
    private final DiscoveryContainerComposer composer;
    
    private final DatabaseClusterEnvironment databaseClusterEnv;
    
    private final DiscoveryDistSQLCommand discoveryDistSQL;
    
    public DiscoveryTestAction(final DiscoveryContainerComposer composer, final DatabaseClusterEnvironment databaseClusterEnv) {
        this.composer = composer;
        this.databaseClusterEnv = databaseClusterEnv;
        discoveryDistSQL = JAXB.unmarshal(Objects.requireNonNull(DiscoveryTestAction.class.getClassLoader().getResource("env/common/discovery-command.xml")), DiscoveryDistSQLCommand.class);
    }
    
    /**
     * Execute test action.
     * 
     * @throws SQLException SQL exception
     */
    public void execute() throws SQLException {
        initDiscoveryEnvironment();
        assertClosePrimaryDataSource();
        assertCloseReplicationDataSource();
        dropDatabaseDiscoveryDatabase();
        createReadWriteSplittingDatabase();
        registerSingleStorageUnit();
    }
    
    private void initDiscoveryEnvironment() throws SQLException {
        new DiscoveryRuleBuilder(composer.getProxyDataSource(), discoveryDistSQL).buildDiscoveryEnvironment();
    }
    
    private void assertClosePrimaryDataSource() throws SQLException {
        String oldPrimaryDataSourceName = getPrimaryDataSourceName();
        closeDataSource(databaseClusterEnv.getDataSources().get(oldPrimaryDataSourceName));
        Awaitility.await().atMost(Durations.ONE_MINUTE).until(() -> !oldPrimaryDataSourceName.equals(getPrimaryDataSourceName()));
        databaseClusterEnv.getDataSources().remove(oldPrimaryDataSourceName);
    }
    
    private String getPrimaryDataSourceName() throws SQLException {
        try (
                Connection connection = composer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            String result = getReadwriteSplittingRulePrimaryDataSourceName(statement);
            assertPrimaryDataSource(result, getDiscoveryRulePrimaryDataSourceName(statement));
            return result;
        }
    }
    
    private String getReadwriteSplittingRulePrimaryDataSourceName(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SHOW READWRITE_SPLITTING RULES")) {
            return resultSet.next() ? resultSet.getString("write_storage_unit_name") : "";
        }
    }
    
    private String getDiscoveryRulePrimaryDataSourceName(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("SHOW DB_DISCOVERY RULES")) {
            return resultSet.next() ? resultSet.getString("primary_data_source_name") : "";
        }
    }
    
    private void assertPrimaryDataSource(final String actualPrimaryDataSourceName, final String expectedPrimaryDataSourceName) {
        Preconditions.checkState(StringUtils.isNotBlank(actualPrimaryDataSourceName) && StringUtils.isNotBlank(expectedPrimaryDataSourceName));
        assertThat(actualPrimaryDataSourceName, is(expectedPrimaryDataSourceName));
    }
    
    private void closeDataSource(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }
    
    private void assertCloseReplicationDataSource() throws SQLException {
        databaseClusterEnv.getDataSources().remove(getPrimaryDataSourceName());
        String closedRoutingDataSourceName = getCloseReplicationDataSourceName(databaseClusterEnv);
        databaseClusterEnv.getDataSources().remove(closedRoutingDataSourceName);
        Awaitility.await().atMost(Durations.TWO_MINUTES)
                .until(() -> getRouteDataSourceName().equals(Objects.requireNonNull(databaseClusterEnv.getDataSources().entrySet().stream().findFirst().orElse(null)).getKey()));
    }
    
    private String getCloseReplicationDataSourceName(final DatabaseClusterEnvironment databaseClusterEnv) throws SQLException {
        for (Map.Entry<String, DataSource> entry : databaseClusterEnv.getDataSources().entrySet()) {
            closeDataSource(databaseClusterEnv.getDataSources().get(entry.getKey()));
            return entry.getKey();
        }
        return null;
    }
    
    private String getRouteDataSourceName() throws SQLException {
        try (
                Connection connection = composer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            return getRouteDataSourceName(statement);
        }
    }
    
    private String getRouteDataSourceName(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("PREVIEW SELECT 1")) {
            return resultSet.next() ? resultSet.getString("data_source_name") : "";
        }
    }
    
    private void dropDatabaseDiscoveryDatabase() throws SQLException {
        try (
                Connection connection = composer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQL.getDropDatabase().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertDropSQL(statement, discoveryDistSQL.getDropDatabase().getAssertionSQL()));
        }
    }
    
    private boolean assertDropSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet ignored = statement.executeQuery(assertionSQL)) {
            return false;
        } catch (final SQLException ignored) {
            return true;
        }
    }
    
    private void createReadWriteSplittingDatabase() throws SQLException {
        try (
                Connection connection = composer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQL.getCreateReadwriteSplittingDatabase().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertCreateSQL(statement, discoveryDistSQL.getCreateReadwriteSplittingDatabase().getAssertionSQL()));
        }
    }
    
    private boolean assertCreateSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet ignored = statement.executeQuery(assertionSQL)) {
            return true;
        } catch (final SQLException ignored) {
            return false;
        }
    }
    
    private void registerSingleStorageUnit() throws SQLException {
        try (
                Connection connection = composer.getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQL.getRegisterSingleStorageUnit().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertRDL(statement, discoveryDistSQL.getRegisterSingleStorageUnit().getAssertionSQL()));
        }
    }
    
    private boolean assertRDL(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return resultSet.next();
        } catch (final SQLException ignored) {
            return false;
        }
    }
}
