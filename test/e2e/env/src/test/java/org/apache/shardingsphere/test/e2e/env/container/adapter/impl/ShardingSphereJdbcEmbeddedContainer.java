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

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.env.container.EmbeddedE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere JDBC embedded container.
 */
@RequiredArgsConstructor
public final class ShardingSphereJdbcEmbeddedContainer implements EmbeddedE2EContainer, AdapterContainer {
    
    private final StorageContainer storageContainer;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    private final String configPath;
    
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
    
    private DataSource createTargetDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        result.setJdbcUrl("jdbc:shardingsphere:absolutepath:" + processFile(configPath, storageContainer.getLinkReplacements()));
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        result.setLeakDetectionThreshold(10000L);
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private String processFile(final String filePath, final Map<String, String> replacements) {
        Path path = Paths.get(filePath);
        String content = new String(Files.readAllBytes(path));
        for (Entry<String, String> entry : replacements.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }
        File tempFile = File.createTempFile("shardingsphere_e2e_jdbc_tmp_config_", null);
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile.getAbsolutePath();
    }
    
    @Override
    public String getAbbreviation() {
        return "jdbc";
    }
}
