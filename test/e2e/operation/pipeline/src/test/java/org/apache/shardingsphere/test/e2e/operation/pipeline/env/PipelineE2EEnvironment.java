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

package org.apache.shardingsphere.test.e2e.operation.pipeline.env;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.operation.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.operation.pipeline.env.enums.PipelineProxyTypeEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
public final class PipelineE2EEnvironment {
    
    private static final PipelineE2EEnvironment INSTANCE = new PipelineE2EEnvironment();
    
    private final Properties props;
    
    private final PipelineEnvTypeEnum itEnvType;
    
    private final PipelineProxyTypeEnum itProxyType;
    
    private PipelineE2EEnvironment() {
        props = loadProperties();
        itEnvType = PipelineEnvTypeEnum.valueOf(props.getProperty("pipeline.it.env.type", PipelineEnvTypeEnum.NONE.name()).toUpperCase());
        itProxyType = PipelineProxyTypeEnum.valueOf(props.getProperty("pipeline.it.proxy.type", PipelineProxyTypeEnum.NONE.name()).toUpperCase());
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("env/it-env.properties")) {
            result.load(inputStream);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    /**
     * Get actual database port.
     *
     * @param databaseType database type
     * @return actual database port
     */
    public int getActualDatabasePort(final DatabaseType databaseType) {
        int defaultPort = DatabaseTypedSPILoader.getService(StorageContainerConfigurationOption.class, databaseType).getPort();
        return Integer.parseInt(props.getProperty(String.format("pipeline.it.native.%s.port", databaseType.getType().toLowerCase()), String.valueOf(defaultPort)));
    }
    
    /**
     * Get native database type.
     *
     * @return native database type
     */
    public String getNativeDatabaseType() {
        return String.valueOf(props.getProperty("pipeline.it.native.database"));
    }
    
    /**
     * Get actual data source username.
     *
     * @param databaseType database type
     * @return actual data source username
     */
    public String getActualDataSourceUsername(final DatabaseType databaseType) {
        return String.valueOf(props.getProperty(String.format("pipeline.it.native.%s.username", databaseType.getType().toLowerCase()), StorageContainerConstants.OPERATION_USER));
    }
    
    /**
     * Get actual data source password.
     *
     * @param databaseType database type
     * @return actual data source username
     */
    public String getActualDataSourcePassword(final DatabaseType databaseType) {
        return String.valueOf(props.getProperty(String.format("pipeline.it.native.%s.password", databaseType.getType().toLowerCase()), StorageContainerConstants.OPERATION_PASSWORD));
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static PipelineE2EEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * List storage container images.
     *
     * @param databaseType database type
     * @return database storage container images
     */
    public List<String> listStorageContainerImages(final DatabaseType databaseType) {
        // Native mode needn't use docker image, just return a list which contain one item
        if (PipelineEnvTypeEnum.NATIVE == itEnvType) {
            return databaseType.getType().equalsIgnoreCase(getNativeDatabaseType()) ? Collections.singletonList("") : Collections.emptyList();
        }
        return Arrays.stream(props.getOrDefault(String.format("pipeline.it.docker.%s.version", databaseType.getType().toLowerCase()), "").toString()
                .split(",")).filter(each -> !Strings.isNullOrEmpty(each)).collect(Collectors.toList());
    }
}
