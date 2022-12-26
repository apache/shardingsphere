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
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.discovery.build.DiscoveryRuleBuilder;
import org.apache.shardingsphere.test.e2e.discovery.cases.DatabaseClusterEnvironment;
import org.apache.shardingsphere.test.e2e.discovery.env.DiscoveryE2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.discovery.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.framework.parameter.DiscoveryTestParameter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;

@Getter(AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseDiscoveryE2EIT {
    
    protected static final DiscoveryE2ETestEnvironment ENV = DiscoveryE2ETestEnvironment.getInstance();
    
    private final BaseContainerComposer containerComposer;
    
    private final DatabaseType databaseType;
    
    private final List<DataSource> mappedDataSources;
    
    private final DataSource proxyDataSource;
    
    public BaseDiscoveryE2EIT(final DiscoveryTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        containerComposer = new DockerContainerComposer(testParam.getScenario(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
        containerComposer.start();
        mappedDataSources = containerComposer.getMappedDatasource();
        proxyDataSource = containerComposer.getProxyDatasource();
    }
    
    /**
     * Initialization discovery environment.
     *
     * @throws SQLException SQL exception
     */
    public void initDiscoveryEnvironment() throws SQLException {
        new DiscoveryRuleBuilder(proxyDataSource).buildDiscoveryEnvironment();
    }
    
    /**
     * Assert close primary data source.
     * @param mgrEnvironment mgr environment
     * @throws SQLException SQL Exception
     */
    public void assertClosePrimaryDataSource(final DatabaseClusterEnvironment mgrEnvironment) throws SQLException {
        String oldPrimaryDataSourceName = getPrimaryDataSourceName();
        closeDataSource(mgrEnvironment.getDataSources().get(oldPrimaryDataSourceName));
        String newPrimaryDataSourceName = getPrimaryDataSourceName();
        assertPrimaryDataSourceChanged(oldPrimaryDataSourceName, newPrimaryDataSourceName);
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
        ThreadUtil.sleep(35, TimeUnit.SECONDS);
    }
    
    private void assertPrimaryDataSourceChanged(final String oldPrimaryDataSourceName, final String newPrimaryDataSourceName) {
        assertNotEquals(oldPrimaryDataSourceName, newPrimaryDataSourceName);
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
        String routeDataSourceName = getRouteDataSourceName();
        assertRouteDataSourceName(routeDataSourceName, Objects.requireNonNull(mgrEnvironment.getDataSources().entrySet().stream().findFirst().orElse(null)).getKey());
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
    
    private void assertRouteDataSourceName(final String actualRouteDataSourceName, final String expectedRouteDataSourceName) {
        Preconditions.checkState(StringUtils.isNotBlank(actualRouteDataSourceName) && StringUtils.isNotBlank(expectedRouteDataSourceName));
        assertThat(actualRouteDataSourceName, is(expectedRouteDataSourceName));
    }
    
    /**
     * Assert close all replication data source.
     * @param mgrEnvironment mgr environment
     * @throws SQLException SQL Exception
     */
    public void assertCloseAllReplicationDataSource(final DatabaseClusterEnvironment mgrEnvironment) throws SQLException {
        closeDataSource(Objects.requireNonNull(mgrEnvironment.getDataSources().values().stream().findFirst().orElse(null)));
        assertRouteDataSourceName(getRouteDataSourceName(), getPrimaryDataSourceName());
    }
}
