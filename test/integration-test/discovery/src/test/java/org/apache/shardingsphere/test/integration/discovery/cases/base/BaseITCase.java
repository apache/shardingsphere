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

package org.apache.shardingsphere.test.integration.discovery.cases.base;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.discovery.build.DiscoveryRuleBuilder;
import org.apache.shardingsphere.test.integration.discovery.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.discovery.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.integration.discovery.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.integration.discovery.framework.parameter.DiscoveryParameterized;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Base integration test.
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final BaseContainerComposer containerComposer;
    
    private final DatabaseType databaseType;
    
    private final List<DataSource> mappedDataSources;
    
    private final DataSource proxyDataSource;
    
    public BaseITCase(final DiscoveryParameterized discoveryParameterized) {
        databaseType = discoveryParameterized.getDatabaseType();
        containerComposer = new DockerContainerComposer(discoveryParameterized.getScenario(), discoveryParameterized.getDatabaseType(), discoveryParameterized.getStorageContainerImage());
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
     * Get primary data source name.
     *
     * @return primary data source name
     * @throws SQLException SQL exception
     */
    public String getPrimaryDataSourceName() throws SQLException {
        try (
                Connection connection = proxyDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            String expectedPrimaryDataSourceName = getDiscoveryRulePrimaryDataSourceName(statement);
            String actualPrimaryDataSourceName = getReadwriteSplittingRulePrimaryDataSourceName(statement);
            assertPrimaryDataSource(actualPrimaryDataSourceName, expectedPrimaryDataSourceName);
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
        Preconditions.checkState(StringUtils.isNotBlank(expectedPrimaryDataSourceName) && StringUtils.isNotBlank(actualPrimaryDataSourceName));
        assertThat(actualPrimaryDataSourceName, is(expectedPrimaryDataSourceName));
    }
    
    /**
     * Close data sources.
     *
     * @param dataSources data sources
     * @throws SQLException SQL exception
     */
    public void closeDataSources(final List<DataSource> dataSources) throws SQLException {
        for (DataSource each : dataSources) {
            close(each);
        }
        ThreadUtil.sleep(20, TimeUnit.SECONDS);
    }
    
    private void close(final DataSource dataSource) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }
    
    /**
     * Assert primary data source changed.
     *
     * @param oldPrimaryDataSourceName old primary data source name
     * @param newPrimaryDataSourceName new primary data source name
     */
    public void assertPrimaryDataSourceChanged(final String oldPrimaryDataSourceName, final String newPrimaryDataSourceName) {
        assertNotEquals(oldPrimaryDataSourceName, newPrimaryDataSourceName);
    }
}
