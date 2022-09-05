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

package org.apache.shardingsphere.test.integration.ha.cases.base;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl.ShardingSphereProxyClusterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.ha.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.ha.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.test.integration.ha.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.test.integration.ha.framework.parameter.HAParameterized;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base integration test.
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    protected static final String DEFAULT_SCHEMA = "ha_test";
    
    private final BaseComposedContainer composedContainer;
    
    private final DatabaseType databaseType;
    
    private List<DataSource> storageDataSources;
    
    private DataSource proxyDataSource;
    
    public BaseITCase(final HAParameterized haParameterized) {
        databaseType = haParameterized.getDatabaseType();
        composedContainer = new DockerComposedContainer(haParameterized.getScenario(), haParameterized.getDatabaseType(), haParameterized.getDockerImageName());
        composedContainer.start();
        initStorageDataSources();
        initProxyDataSource();
    }
    
    private void initProxyDataSource() {
        String databaseName = (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) ? "postgres" : "";
        ShardingSphereProxyClusterContainer proxyContainer = ((DockerComposedContainer) composedContainer).getProxyContainer();
        this.proxyDataSource = getDataSource(DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(),
                composedContainer.getProxyJdbcUrl(databaseName)), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD);
    }
    
    private void initStorageDataSources() {
        List<DockerStorageContainer> storageContainers = ((DockerComposedContainer) composedContainer).getStorageContainers();
        this.storageDataSources = storageContainers.stream()
                .map(storageContainer -> DataSourceEnvironment.getURL(getDatabaseType(), storageContainer.getNetworkAliases().get(0), storageContainer.getExposedPort(), DEFAULT_SCHEMA))
                .map(jdbcUrl -> getDataSource(jdbcUrl, StorageContainerConstants.USERNAME, StorageContainerConstants.PASSWORD))
                .collect(Collectors.toList());
    }
    
    private DataSource getDataSource(final String jdbcUrl, final String username, final String password) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(getDatabaseType()));
        result.setJdbcUrl(jdbcUrl);
        result.setUsername(username);
        result.setPassword(password);
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
}
