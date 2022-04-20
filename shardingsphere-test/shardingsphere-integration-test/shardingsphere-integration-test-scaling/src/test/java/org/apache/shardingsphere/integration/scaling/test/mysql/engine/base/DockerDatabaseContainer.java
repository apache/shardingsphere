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

package org.apache.shardingsphere.integration.scaling.test.mysql.engine.base;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.testcontainers.containers.BindMode;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Docker storage container.
 */
@Getter
public abstract class DockerDatabaseContainer extends DockerITContainer implements StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    public DockerDatabaseContainer(final DatabaseType databaseType, final String dockerImageName) {
        super(databaseType.getName().toLowerCase(), dockerImageName);
        this.databaseType = databaseType;
        actualDataSourceMap = new LinkedHashMap<>();
        expectedDataSourceMap = new LinkedHashMap<>();
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping(String.format("/env/%s", databaseType.getName()), "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
    }
    
    @Override
    @SneakyThrows
    protected void postStart() {
        Lists.newArrayList("ds_src_0", "ds_src_1").forEach(each -> actualDataSourceMap.put(each, createDataSource(each)));
        Lists.newArrayList("ds_dst_2", "ds_dst_3", "ds_dst_4").forEach(each -> expectedDataSourceMap.put(each, createDataSource(each)));
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(databaseType));
        result.setJdbcUrl(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(getPort()), dataSourceName));
        result.setUsername("root");
        result.setPassword("123456");
        result.setMaximumPoolSize(4);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    protected abstract int getPort();
}
