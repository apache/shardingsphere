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

package org.apache.shardingsphere.test.integration.ha.env;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.ha.env.enums.ITEnvTypeEnum;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
        itEnvType = ITEnvTypeEnum.valueOf(StringUtils.defaultIfBlank(props.getProperty("it.env.type").toUpperCase(), ITEnvTypeEnum.NONE.name()));
        mysqlVersions = Arrays.stream(props.getOrDefault("it.docker.mysql.version", "").toString().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        postgresVersions = Arrays.stream(props.getOrDefault("it.docker.postgresql.version", "").toString().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        openGaussVersions = Arrays.stream(props.getOrDefault("it.docker.opengauss.version", "").toString().split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
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
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * List database docker image names.
     *
     * @param databaseType database type.
     * @return database docker image names
     */
    public List<String> listDatabaseDockerImageNames(final DatabaseType databaseType) {
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
