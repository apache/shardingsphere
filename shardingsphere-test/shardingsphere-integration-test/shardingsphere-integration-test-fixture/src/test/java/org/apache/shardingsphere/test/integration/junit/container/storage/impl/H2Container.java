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

package org.apache.shardingsphere.test.integration.junit.container.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;

import javax.sql.DataSource;
import java.util.Map;

/**
 * H2 container.
 */
public final class H2Container extends ShardingSphereStorageContainer {
    
    private volatile Map<String, DataSource> actualDataSources;
    
    private volatile boolean isHealthy;
    
    public H2Container() {
        super("h2:fake", new H2DatabaseType(), true);
    }
    
    @Override
    @SneakyThrows
    protected void configure() {
        super.configure();
        actualDataSources = ActualDataSourceBuilder.createActualDataSources(getDescription().getScenario(), getDescription().getDatabaseType());
        isHealthy = true;
    }
    
    @Override
    public boolean isHealthy() {
        return isHealthy;
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return null;
    }
    
    @Override
    protected int getPort() {
        return 0;
    }
    
    @Override
    protected String getUsername() {
        return null;
    }
    
    @Override
    protected String getPassword() {
        return null;
    }
    
    @Override
    @SneakyThrows
    public synchronized Map<String, DataSource> getDataSourceMap() {
        return actualDataSources;
    }
}
