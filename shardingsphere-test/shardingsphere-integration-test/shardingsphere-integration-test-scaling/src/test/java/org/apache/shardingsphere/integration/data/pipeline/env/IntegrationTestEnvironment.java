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

package org.apache.shardingsphere.integration.data.pipeline.env;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.StorageContainerConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
@Slf4j
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final Properties props;
    
    private final ITEnvTypeEnum itEnvType;
    
    private final List<String> mysqlVersions;
    
    private final List<String> postgresVersions;
    
    private final List<String> openGaussVersions;
    
    private IntegrationTestEnvironment() {
        props = loadProperties();
        itEnvType = ITEnvTypeEnum.valueOf(StringUtils.defaultIfBlank(props.getProperty("scaling.it.env.type").toUpperCase(), ITEnvTypeEnum.NONE.name()));
        mysqlVersions = Arrays.stream(props.getOrDefault("scaling.it.docker.mysql.version", "").toString().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        postgresVersions = Arrays.stream(props.getOrDefault("scaling.it.docker.postgresql.version", "").toString().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        openGaussVersions = Arrays.stream(props.getOrDefault("scaling.it.docker.opengauss.version", "").toString().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = IntegrationTestEnvironment.class.getClassLoader().getResourceAsStream("env/it-env.properties")) {
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
     * @param databaseType database type.
     * @return jdbc connection
     */
    public int getActualDatabasePort(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return Integer.parseInt(props.getOrDefault("scaling.it.native.mysql.port", 3307).toString());
            case "PostgreSQL":
                return Integer.parseInt(props.getOrDefault("scaling.it.native.postgresql.port", 5432).toString());
            case "openGauss":
                return Integer.parseInt(props.getOrDefault("scaling.it.native.opengauss.port", 5432).toString());
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + databaseType.getType());
        }
    }
    
    /**
     * Get actual data source default port.
     *
     * @param databaseType database type.
     * @return default port
     */
    public int getActualDataSourceDefaultPort(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return Integer.parseInt(props.getOrDefault("scaling.it.native.mysql.port", StorageContainerConstants.MYSQL_EXPOSED_PORT).toString());
            case "PostgreSQL":
                return Integer.parseInt(props.getOrDefault("scaling.it.native.postgresql.port", StorageContainerConstants.POSTGRESQL_EXPOSED_PORT).toString());
            case "openGauss":
                return Integer.parseInt(props.getOrDefault("scaling.it.native.opengauss.port", StorageContainerConstants.OPENGAUSS_EXPOSED_PORT).toString());
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
        return String.valueOf(props.get("scaling.it.native.database"));
    }
    
    /**
     * Get actual data source username.
     *
     * @param databaseType database type.
     * @return actual data source username
     */
    public String getActualDataSourceUsername(final DatabaseType databaseType) {
        return String.valueOf(props.getOrDefault(String.format("scaling.it.native.%s.username", databaseType.getType().toLowerCase()), "Root@123"));
    }
    
    /**
     * Get actual data source password.
     *
     * @param databaseType database type.
     * @return actual data source username
     */
    public String getActualDataSourcePassword(final DatabaseType databaseType) {
        return String.valueOf(props.getOrDefault(String.format("scaling.it.native.%s.password", databaseType.getType().toLowerCase()), "Root@123"));
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * List storage contaienr images.
     *
     * @param databaseType database type.
     * @return database storage container images
     */
    public List<String> listStorageContainerImages(final DatabaseType databaseType) {
        // Native mode needn't use docker image, just return a list which contain one item
        if (getItEnvType() == ITEnvTypeEnum.NATIVE) {
            return databaseType.getType().equalsIgnoreCase(getNativeDatabaseType()) ? Collections.singletonList("") : Collections.emptyList();
        }
        switch (databaseType.getType()) {
            case "MySQL":
                return mysqlVersions;
            case "PostgreSQL":
                return postgresVersions;
            case "openGauss":
                return openGaussVersions;
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + databaseType.getType());
        }
    }
}
