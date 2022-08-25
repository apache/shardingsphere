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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.ha.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.ha.framework.container.compose.AbstractComposedContainer;
import org.apache.shardingsphere.test.integration.ha.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.test.integration.ha.framework.parameter.Parameterized;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final AbstractComposedContainer composedContainer;
    
    private final DatabaseType databaseType;
    
    private List<DataSource> storageDataSources;
    
    private DataSource proxyDataSource;
    
    @Setter
    private Thread increaseTaskThread;
    
    public BaseITCase(final Parameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        composedContainer = new DockerComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        composedContainer.start();
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
    
    private List<String> getStorageDataSources(final String databaseName) {
        List<DockerStorageContainer> storageContainers = ((DockerComposedContainer) composedContainer).getStorageContainers();
        return storageContainers.stream().map(storageContainer -> 
                DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getPort(), databaseName)).collect(Collectors.toList());
    }
}
