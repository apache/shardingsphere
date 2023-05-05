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

package org.apache.shardingsphere.test.e2e.container;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.ExternalCompatibilityTestParameter;
import org.apache.shardingsphere.test.e2e.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtils;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Compatibility container composer.
 */
@Getter
@Slf4j
public final class CompatibilityContainerComposer implements AutoCloseable {
    
    public static final String SCHEMA_NAME = "test";
    
    public static final String DS_0 = "pipeline_it_0";
    
    private static final String PROXY_DATABASE = "sharding_db";
    
    private final BaseContainerComposer containerComposer;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private final DataSource sourceDataSource;
    
    private final DataSource proxyDataSource;
    
    public CompatibilityContainerComposer(final ExternalCompatibilityTestParameter testParam, final JobType jobType) {
        databaseType = new MySQLDatabaseType();
        containerComposer = PipelineE2EEnvironment.getInstance().getItEnvType() == EnvTypeEnum.DOCKER
                ? new DockerContainerComposer(databaseType)
                : new NativeContainerComposer(databaseType);
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == EnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainer();
            username = storageContainer.getUsername();
            password = storageContainer.getPassword();
        } else {
            username = PipelineE2EEnvironment.getInstance().getActualDataSourceUsername(databaseType);
            password = PipelineE2EEnvironment.getInstance().getActualDataSourcePassword(databaseType);
        }
        containerComposer.start();
        sourceDataSource = StorageContainerUtils.generateDataSource(appendExtraParameter(getActualJdbcUrlTemplate(DS_0, false)), username, password);
        proxyDataSource = StorageContainerUtils.generateDataSource(
                appendExtraParameter(containerComposer.getProxyJdbcUrl(PROXY_DATABASE)), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD);
        init();
    }
    
    @SneakyThrows(SQLException.class)
    private void init() {
        String jdbcUrl = containerComposer.getProxyJdbcUrl(DatabaseTypeUtils.isPostgreSQL(databaseType) || DatabaseTypeUtils.isOpenGauss(databaseType) ? "postgres" : "");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD)) {
            cleanUpProxyDatabase(connection);
            createProxyDatabase(connection);
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void cleanUpProxyDatabase(final Connection connection) {
        if (EnvTypeEnum.NATIVE != PipelineE2EEnvironment.getInstance().getItEnvType()) {
            return;
        }
        try {
            connection.createStatement().execute(String.format("DROP DATABASE IF EXISTS %s", PROXY_DATABASE));
            Thread.sleep(2000L);
        } catch (final SQLException ex) {
            log.warn("Drop proxy database failed, error={}", ex.getMessage());
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void createProxyDatabase(final Connection connection) throws SQLException {
        String sql = String.format("CREATE DATABASE %s", PROXY_DATABASE);
        log.info("Create proxy database {}", PROXY_DATABASE);
        connection.createStatement().execute(sql);
        Thread.sleep(2000L);
    }
    
    
    /**
     * Append extra parameter.
     * 
     * @param jdbcUrl JDBC URL
     * @return appended JDBC URL
     */
    public String appendExtraParameter(final String jdbcUrl) {
        if (DatabaseTypeUtils.isMySQL(databaseType)) {
            return new JdbcUrlAppender().appendQueryProperties(jdbcUrl, PropertiesBuilder.build(new Property("rewriteBatchedStatements", Boolean.TRUE.toString())));
        }
        if (DatabaseTypeUtils.isPostgreSQL(databaseType) || DatabaseTypeUtils.isOpenGauss(databaseType)) {
            return new JdbcUrlAppender().appendQueryProperties(jdbcUrl, PropertiesBuilder.build(new Property("stringtype", "unspecified")));
        }
        return jdbcUrl;
    }
    
    /**
     * Get actual JDBC URL template.
     * 
     * @param databaseName database name
     * @param isInContainer is in container
     * @return actual JDBC URL template
     */
    public String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer) {
        if (EnvTypeEnum.DOCKER == PipelineE2EEnvironment.getInstance().getItEnvType()) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainer();
            return isInContainer
                    ? DataSourceEnvironment.getURL(getDatabaseType(), storageContainer.getNetworkAliases().get(0), storageContainer.getExposedPort(), databaseName)
                    : storageContainer.getJdbcUrl(databaseName);
        }
        return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", PipelineE2EEnvironment.getInstance().getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
