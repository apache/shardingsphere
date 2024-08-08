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

package org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.url.core.ShardingSphereURL;
import org.apache.shardingsphere.infra.url.core.ShardingSphereURLLoadEngine;
import org.apache.shardingsphere.test.e2e.env.container.atomic.EmbeddedITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioCommonPath;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere JDBC container.
 */
public final class ShardingSphereJdbcContainer implements EmbeddedITContainer, AdapterContainer {
    
    private final ScenarioCommonPath scenarioCommonPath;
    
    private final DatabaseType databaseType;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereJdbcContainer(final String scenario, final DatabaseType databaseType) {
        scenarioCommonPath = new ScenarioCommonPath(scenario);
        this.databaseType = databaseType;
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (null == dataSource) {
            targetDataSourceProvider.set(createTargetDataSource());
        }
        return targetDataSourceProvider.get();
    }
    
    @SneakyThrows({SQLException.class, IOException.class})
    private DataSource createTargetDataSource() {
        ShardingSphereURLLoadEngine urlLoadEngine = new ShardingSphereURLLoadEngine(ShardingSphereURL.parse("absolutepath:" + scenarioCommonPath.getRuleConfigurationFile(databaseType)));
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(urlLoadEngine.loadContent());
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        result.setDataSource(dataSource);
        result.setUsername("root");
        result.setPassword("Root@123");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        result.setLeakDetectionThreshold(10000L);
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return "jdbc";
    }
}
