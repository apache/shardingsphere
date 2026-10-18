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

package org.apache.shardingsphere.test.e2e.env.container.adapter.impl;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.EmbeddedE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere JDBC embedded container.
 */
@RequiredArgsConstructor
public final class ShardingSphereJdbcEmbeddedContainer implements EmbeddedE2EContainer, AdapterContainer {
    
    private final StorageContainer storageContainer;
    
    private final Map<String, DataSource> targetDataSources = new ConcurrentHashMap<>();
    
    private final String configPath;
    
    @Override
    public void start() {
    }
    
    @Override
    @SneakyThrows(SQLException.class)
    public void stop() {
        for (Entry<String, DataSource> entry : targetDataSources.entrySet()) {
            HikariDataSource targetDataSource = (HikariDataSource) entry.getValue();
            DataSource dataSource = targetDataSource.getDataSource();
            if (null != dataSource) {
                targetDataSource.close();
                ((ShardingSphereDataSource) dataSource).close();
                targetDataSources.remove(entry.getKey());
            }
        }
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        return getTargetDataSource(serverLists, configPath);
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists, final String dataSourceName) {
        String effectiveDataSourceName = Strings.isNullOrEmpty(dataSourceName) ? configPath : dataSourceName;
        return targetDataSources.computeIfAbsent(effectiveDataSourceName, unused -> createTargetDataSource(serverLists));
    }
    
    @SneakyThrows({IOException.class, SQLException.class})
    private DataSource createTargetDataSource(@Nullable final String serverLists) {
        HikariDataSource result = new HikariDataSource();
        if (Strings.isNullOrEmpty(serverLists)) {
            result.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
            result.setJdbcUrl("jdbc:shardingsphere:absolutepath:" + processFile(configPath, storageContainer.getLinkReplacements(), serverLists));
        } else {
            result.setDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new File(processFile(configPath, storageContainer.getLinkReplacements(), serverLists))));
        }
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        result.setLeakDetectionThreshold(10000L);
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private String processFile(final String filePath, final Map<String, String> replacements, @Nullable final String serverLists) {
        Path path = Paths.get(filePath);
        String content = new String(Files.readAllBytes(path));
        for (Entry<String, String> entry : replacements.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        if (!Strings.isNullOrEmpty(serverLists)) {
            YamlJDBCConfiguration config = YamlEngine.unmarshal(content, YamlJDBCConfiguration.class);
            YamlPersistRepositoryConfiguration repository = new YamlPersistRepositoryConfiguration();
            repository.setType("ZooKeeper");
            repository.getProps().setProperty("namespace", "e2e_db");
            repository.getProps().setProperty("server-lists", serverLists);
            YamlModeConfiguration modeConfig = new YamlModeConfiguration();
            modeConfig.setType("Cluster");
            modeConfig.setRepository(repository);
            config.setMode(modeConfig);
            content = YamlEngine.marshal(config);
        }
        File tempFile = File.createTempFile("shardingsphere_e2e_jdbc_tmp_config_", null);
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile.getAbsolutePath();
    }
    
    @Override
    public Collection<DataSource> getTargetDataSources() {
        return targetDataSources.values();
    }
    
    @Override
    public String getAbbreviation() {
        return "jdbc";
    }
}
