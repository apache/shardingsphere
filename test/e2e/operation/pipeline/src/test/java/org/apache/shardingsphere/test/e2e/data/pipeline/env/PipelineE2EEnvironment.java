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

package org.apache.shardingsphere.test.e2e.data.pipeline.env;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.OpenGaussContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.PostgreSQLContainer;

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
    
    private final List<String> mysqlVersions;
    
    private final List<String> postgresqlVersions;
    
    private final List<String> openGaussVersions;
    
    private PipelineE2EEnvironment() {
        props = loadProperties();
        itEnvType = PipelineEnvTypeEnum.valueOf(props.getProperty("pipeline.it.env.type", PipelineEnvTypeEnum.NONE.name()).toUpperCase());
        mysqlVersions = Arrays.stream(props.getOrDefault("pipeline.it.docker.mysql.version", "").toString().split(",")).filter(each -> !Strings.isNullOrEmpty(each)).collect(Collectors.toList());
        postgresqlVersions = Arrays.stream(props.getOrDefault("pipeline.it.docker.postgresql.version", "").toString().split(",")).filter(cs -> !Strings.isNullOrEmpty(cs)).collect(Collectors.toList());
        openGaussVersions = Arrays.stream(props.getOrDefault("pipeline.it.docker.opengauss.version", "").toString().split(",")).filter(cs -> !Strings.isNullOrEmpty(cs)).collect(Collectors.toList());
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
     * Get actual data source connection.
     *
     * @param databaseType database type
     * @return jdbc connection
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public int getActualDatabasePort(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return Integer.parseInt(props.getOrDefault("pipeline.it.native.mysql.port", 3307).toString());
            case "PostgreSQL":
                return Integer.parseInt(props.getOrDefault("pipeline.it.native.postgresql.port", 5432).toString());
            case "openGauss":
                return Integer.parseInt(props.getOrDefault("pipeline.it.native.opengauss.port", 5432).toString());
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + databaseType.getType());
        }
    }
    
    /**
     * Get actual data source default port.
     *
     * @param databaseType database type
     * @return default port
     * @throws IllegalArgumentException illegal argument exception
     */
    public int getActualDataSourceDefaultPort(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return Integer.parseInt(props.getOrDefault("pipeline.it.native.mysql.port", MySQLContainer.MYSQL_EXPOSED_PORT).toString());
            case "PostgreSQL":
                return Integer.parseInt(props.getOrDefault("pipeline.it.native.postgresql.port", PostgreSQLContainer.POSTGRESQL_EXPOSED_PORT).toString());
            case "openGauss":
                return Integer.parseInt(props.getOrDefault("pipeline.it.native.opengauss.port", OpenGaussContainer.OPENGAUSS_EXPOSED_PORT).toString());
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType.getType());
        }
    }
    
    /**
     * Get native database type.
     *
     * @return native database type
     */
    public String getNativeDatabaseType() {
        return String.valueOf(props.get("pipeline.it.native.database"));
    }
    
    /**
     * Get actual data source username.
     *
     * @param databaseType database type
     * @return actual data source username
     */
    public String getActualDataSourceUsername(final DatabaseType databaseType) {
        return String.valueOf(props.getOrDefault(String.format("pipeline.it.native.%s.username", databaseType.getType().toLowerCase()), "Root@123"));
    }
    
    /**
     * Get actual data source password.
     *
     * @param databaseType database type
     * @return actual data source username
     */
    public String getActualDataSourcePassword(final DatabaseType databaseType) {
        return String.valueOf(props.getOrDefault(String.format("pipeline.it.native.%s.password", databaseType.getType().toLowerCase()), "Root@123"));
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
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public List<String> listStorageContainerImages(final DatabaseType databaseType) {
        // Native mode needn't use docker image, just return a list which contain one item
        if (PipelineEnvTypeEnum.NATIVE == getItEnvType()) {
            return databaseType.getType().equalsIgnoreCase(getNativeDatabaseType()) ? Collections.singletonList("") : Collections.emptyList();
        }
        switch (databaseType.getType()) {
            case "MySQL":
                return mysqlVersions;
            case "PostgreSQL":
                return postgresqlVersions;
            case "openGauss":
                return openGaussVersions;
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + databaseType.getType());
        }
    }
}
