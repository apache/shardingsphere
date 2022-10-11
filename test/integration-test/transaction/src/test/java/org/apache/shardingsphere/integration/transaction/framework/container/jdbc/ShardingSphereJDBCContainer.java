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

package org.apache.shardingsphere.integration.transaction.framework.container.jdbc;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.EmbeddedITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere JDBC container.
 */
public final class ShardingSphereJDBCContainer implements EmbeddedITContainer {
    
    private final DockerStorageContainer databaseContainer;
    
    private final String ruleConfigPath;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereJDBCContainer(final DockerStorageContainer databaseContainer, final String ruleConfigPath) {
        this.databaseContainer = databaseContainer;
        this.ruleConfigPath = ruleConfigPath;
    }
    
    @Override
    public void start() {
    }
    
    /**
     * Get target data source.
     *
     * @return target data source
     */
    public DataSource getTargetDataSource() {
        DataSource dataSource = targetDataSourceProvider.get();
        if (Objects.isNull(dataSource)) {
            try {
                targetDataSourceProvider.set(
                        YamlShardingSphereDataSourceFactory.createDataSource(databaseContainer.getActualDataSourceMap(), new File(ruleConfigPath)));
            } catch (final SQLException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return targetDataSourceProvider.get();
    }
    
    @Override
    public String getAbbreviation() {
        return "jdbc";
    }
}
