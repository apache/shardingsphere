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

package org.apache.shardingsphere.test.e2e.discovery.cases.base;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.discovery.build.DiscoveryRuleBuilder;
import org.apache.shardingsphere.test.e2e.discovery.cases.DatabaseClusterEnvironment;
import org.apache.shardingsphere.test.e2e.discovery.command.DiscoveryDistSQLCommand;
import org.apache.shardingsphere.test.e2e.discovery.env.DiscoveryE2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.discovery.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.framework.parameter.DiscoveryTestParameter;
import org.awaitility.Awaitility;
import org.awaitility.Durations;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Getter(AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseDiscoveryE2EIT {
    
    protected static final DiscoveryE2ETestEnvironment ENV = DiscoveryE2ETestEnvironment.getInstance();
    
    private final BaseContainerComposer containerComposer;
    
    private final DatabaseType databaseType;
    
    private final List<DataSource> mappedDataSources;
    
    private final DataSource proxyDataSource;
    
    private final DiscoveryDistSQLCommand discoveryDistSQLCommand;
    
    public BaseDiscoveryE2EIT(final DiscoveryTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        containerComposer = new DockerContainerComposer(testParam.getScenario(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
        containerComposer.start();
        mappedDataSources = containerComposer.getMappedDatasource();
        proxyDataSource = containerComposer.getProxyDatasource();
        discoveryDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseDiscoveryE2EIT.class.getClassLoader().getResource("env/common/discovery-command.xml")), DiscoveryDistSQLCommand.class);
    }
    
    /**
     * Initialization discovery environment.
     *
     * @throws SQLException SQL exception
     */
    public void initDiscoveryEnvironment() throws SQLException {
        new DiscoveryRuleBuilder(discoveryDistSQLCommand, proxyDataSource).buildDiscoveryEnvironment();
    }
    
    /**
     * Assert close primary data source.
     * @param mgrEnvironment mgr environment
     * @throws SQLException SQL Exception
     */
    public void assertClosePrimaryDataSource(final DatabaseClusterEnvironment mgrEnvironment) throws SQLException {
        String oldPrimaryDataSourceName = getPrimaryDataSourceName();
        closeDataSource(mgrEnvironment.getDataSources().get(oldPrimaryDataSourceName));
        Awaitility.await().atMost(Durations.ONE_MINUTE).until(() -> !oldPrimaryDataSourceName.equals(getPrimaryDataSourceName()));
        mgrEnvironment.getDataSources().remove(oldPrimaryDataSourceName);
    }
    
    private String getPrimaryDataSourceName() throws SQLException {
        try (
                Connection connection = proxyDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            String actualPrimaryDataSourceName = getReadwriteSplittingRulePrimaryDataSourceName(statement);
            assertPrimaryDataSource(actualPrimaryDataSourceName, getDiscoveryRulePrimaryDataSourceName(statement));
            return actualPrimaryDataSourceName;
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
    
    /**
     * Assert close replication data source.
     * @param mgrEnvironment mgr environment
     * @throws SQLException SQL Exception
     */
    public void assertCloseReplicationDataSource(final DatabaseClusterEnvironment mgrEnvironment) throws SQLException {
        mgrEnvironment.getDataSources().remove(getPrimaryDataSourceName());
        String closedRoutingDataSourceName = getCloseReplicationDataSourceName(mgrEnvironment);
        mgrEnvironment.getDataSources().remove(closedRoutingDataSourceName);
        Awaitility.await().atMost(Durations.TWO_MINUTES)
                .until(() -> getRouteDataSourceName().equals(Objects.requireNonNull(mgrEnvironment.getDataSources().entrySet().stream().findFirst().orElse(null)).getKey()));
    }
    
    private String getCloseReplicationDataSourceName(final DatabaseClusterEnvironment mgrEnvironment) throws SQLException {
        for (Map.Entry<String, DataSource> entry : mgrEnvironment.getDataSources().entrySet()) {
            closeDataSource(mgrEnvironment.getDataSources().get(entry.getKey()));
            return entry.getKey();
        }
        return null;
    }
    
    private String getRouteDataSourceName() throws SQLException {
        try (
                Connection connection = proxyDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            return getRouteDataSourceName(statement);
        }
    }
    
    private String getRouteDataSourceName(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("PREVIEW SELECT 1")) {
            return resultSet.next() ? resultSet.getString("data_source_name") : "";
        }
    }
    
    /**
     * Drop database discovery database.
     *
     * @throws SQLException sql exception
     */
    public void dropDatabaseDiscoveryDatabase() throws SQLException {
        try (
                Connection connection = getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQLCommand.getDropDatabase().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertDropSQL(statement, discoveryDistSQLCommand.getDropDatabase().getAssertionSQL()));
        }
    }
    
    private boolean assertDropSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return false;
        } catch (final SQLException ignored) {
            return true;
        }
    }
    
    /**
     * Create readwrite-splitting database.
     *
     * @throws SQLException sql exception
     */
    public void createReadWriteSplittingDatabase() throws SQLException {
        try (
                Connection connection = getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQLCommand.getCreateReadwriteSplittingDatabase().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertCreateSQL(statement, discoveryDistSQLCommand.getCreateReadwriteSplittingDatabase().getAssertionSQL()));
        }
    }
    
    private boolean assertCreateSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return true;
        } catch (final SQLException ignored) {
            return false;
        }
    }
    
    /**
     * Register single storage units.
     *
     * @throws SQLException sql exception
     */
    public void registerSingleStorageUnit() throws SQLException {
        try (
                Connection connection = getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQLCommand.getRegisterSingleStorageUnit().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertRDLDistSQL(statement, discoveryDistSQLCommand.getRegisterSingleStorageUnit().getAssertionSQL()));
        }
    }
    
    private boolean assertRDLDistSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return resultSet.next();
        } catch (final SQLException ignored) {
            return false;
        }
    }
}
