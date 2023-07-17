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

package org.apache.shardingsphere.test.e2e.transaction.engine.base;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.transaction.env.TransactionE2EEnvironment;
import org.apache.shardingsphere.test.e2e.transaction.env.enums.TransactionE2EEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.param.TransactionTestParameter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Transaction container composer.
 */
@Getter
public final class TransactionContainerComposer implements AutoCloseable {
    
    private static final TransactionE2EEnvironment ENV = TransactionE2EEnvironment.getInstance();
    
    private final DatabaseType databaseType;
    
    private final BaseContainerComposer containerComposer;
    
    private final AutoDataSource dataSource;
    
    public TransactionContainerComposer(final TransactionTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        containerComposer = initContainerComposer(testParam);
        dataSource = isProxyAdapter(testParam) ? createProxyDataSource() : createJdbcDataSource();
    }
    
    private BaseContainerComposer initContainerComposer(final TransactionTestParameter testParam) {
        BaseContainerComposer result = ENV.getItEnvType() == TransactionE2EEnvTypeEnum.DOCKER ? new DockerContainerComposer(testParam) : new NativeContainerComposer(testParam.getDatabaseType());
        result.start();
        return result;
    }
    
    private boolean isProxyAdapter(final TransactionTestParameter testParam) {
        return AdapterType.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter());
    }
    
    private ProxyDataSource createProxyDataSource() {
        return new ProxyDataSource(containerComposer, "sharding_db", ENV.getProxyUserName(), ENV.getProxyPassword());
    }
    
    private JdbcDataSource createJdbcDataSource() {
        DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
        DockerStorageContainer storageContainer = dockerContainerComposer.getStorageContainer();
        Map<String, DataSource> actualDataSourceMap = storageContainer.getActualDataSourceMap();
        actualDataSourceMap.put("ds_0", createDataSource(storageContainer, "transaction_it_0"));
        actualDataSourceMap.put("ds_1", createDataSource(storageContainer, "transaction_it_1"));
        return new JdbcDataSource(dockerContainerComposer);
    }
    
    private DataSource createDataSource(final DockerStorageContainer storageContainer, final String dataSourceName) {
        return StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, storageContainer.getHost(), storageContainer.getMappedPort(), dataSourceName),
                storageContainer.getUsername(), storageContainer.getPassword(), 50);
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
