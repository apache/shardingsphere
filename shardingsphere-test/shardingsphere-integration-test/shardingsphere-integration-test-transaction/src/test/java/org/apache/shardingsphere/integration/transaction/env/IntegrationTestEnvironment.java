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

package org.apache.shardingsphere.integration.transaction.env;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.transaction.engine.entity.JdbcInfoEntity;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionITEnvTypeEnum;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionTestCaseRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
@Slf4j
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final Properties props;
    
    private final TransactionITEnvTypeEnum itEnvType;
    
    private final List<String> mysqlVersions;
    
    private final List<String> postgresVersions;
    
    private final List<String> openGaussVersions;
    
    private final List<String> needToRunTestCases;
    
    private final List<String> allowTransactionTypes;
    
    private final List<String> allowXAProviders;
    
    private final Map<String, TransactionTestCaseRegistry> transactionTestCaseRegistryMap;
    
    private IntegrationTestEnvironment() {
        props = loadProperties();
        itEnvType = TransactionITEnvTypeEnum.valueOf(StringUtils.defaultIfBlank(props.getProperty("transaction.it.env.type").toUpperCase(), TransactionITEnvTypeEnum.NONE.name()));
        mysqlVersions = splitProperty("transaction.it.docker.mysql.version");
        postgresVersions = splitProperty("transaction.it.docker.postgresql.version");
        openGaussVersions = splitProperty("transaction.it.docker.opengauss.version");
        needToRunTestCases = splitProperty("transaction.it.env.cases");
        allowTransactionTypes = splitProperty("transaction.it.env.transtypes");
        allowXAProviders = splitProperty("transaction.it.env.xa.providers");
        log.info("Loaded properties, allowTransactionTypes:{}, allowXAProviders:{}", allowTransactionTypes, allowXAProviders);
        transactionTestCaseRegistryMap = initTransactionTestCaseRegistryMap();
    }
    
    private Map<String, TransactionTestCaseRegistry> initTransactionTestCaseRegistryMap() {
        final Map<String, TransactionTestCaseRegistry> transactionTestCaseRegistryMap;
        transactionTestCaseRegistryMap = new HashMap<>(TransactionTestCaseRegistry.values().length, 1);
        for (TransactionTestCaseRegistry each : TransactionTestCaseRegistry.values()) {
            transactionTestCaseRegistryMap.put(each.getTestCaseClass().getName(), each);
        }
        return transactionTestCaseRegistryMap;
    }
    
    private List<String> splitProperty(final String key) {
        return Arrays.stream(props.getOrDefault(key, "").toString().split(",")).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
    }
    
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = IntegrationTestEnvironment.class.getClassLoader().getResourceAsStream("env/it-env.properties")) {
            result.load(inputStream);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
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
    public JdbcInfoEntity getActualDatabaseJdbcInfo(final DatabaseType databaseType) {
        String username;
        String password;
        int port;
        switch (databaseType.getType()) {
            case "MySQL":
                username = props.getOrDefault("transaction.it.native.mysql.username", "root").toString();
                password = props.getOrDefault("transaction.it.native.mysql.password", "root").toString();
                port = Integer.parseInt(props.getOrDefault("transaction.it.native.mysql.port", 3307).toString());
                break;
            case "PostgreSQL":
                username = props.getOrDefault("transaction.it.native.postgresql.username", "postgres").toString();
                password = props.getOrDefault("transaction.it.native.postgresql.password", "postgres").toString();
                port = Integer.parseInt(props.getOrDefault("transaction.it.native.postgresql.port", 5432).toString());
                break;
            case "openGauss":
                username = props.getOrDefault("transaction.it.native.opengauss.username", "gaussdb").toString();
                password = props.getOrDefault("transaction.it.native.opengauss.password", "Root@123").toString();
                port = Integer.parseInt(props.getOrDefault("transaction.it.native.opengauss.port", 5432).toString());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + databaseType.getType());
        }
        return new JdbcInfoEntity(username, password, port);
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
                return Integer.parseInt(props.getOrDefault("transaction.it.native.mysql.port", 3306).toString());
            case "PostgreSQL":
                return Integer.parseInt(props.getOrDefault("transaction.it.native.postgresql.port", 5432).toString());
            case "openGauss":
                return Integer.parseInt(props.getOrDefault("transaction.it.native.opengauss.port", 5432).toString());
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
        return String.valueOf(props.get("transaction.it.native.database"));
    }
    
    /**
     * Get actual data source username.
     *
     * @param databaseType database type.
     * @return actual data source username
     */
    public String getActualDataSourceUsername(final DatabaseType databaseType) {
        String username = "test_user";
        return itEnvType == TransactionITEnvTypeEnum.NATIVE
                ? String.valueOf(props.getOrDefault(String.format("transaction.it.native.%s.username", databaseType.getType().toLowerCase()), username))
                : username;
    }
    
    /**
     * Get actual data source password.
     *
     * @param databaseType database type.
     * @return actual data source username
     */
    public String getActualDataSourcePassword(final DatabaseType databaseType) {
        String password = "Test@123";
        return itEnvType == TransactionITEnvTypeEnum.NATIVE
                ? props.getOrDefault(String.format("transaction.it.native.%s.password", databaseType.getType().toLowerCase()), password).toString()
                : password;
    }
    
    /**
     * Get proxy password.
     * 
     * @return proxy password
     */
    public String getProxyPassword() {
        // TODO this should extract into a constant
        return props.getOrDefault("transaction.it.proxy.password", "Proxy@123").toString();
    }
    
    /**
     * Get proxy userName.
     * 
     * @return proxy userName
     */
    public String getProxyUserName() {
        // TODO this should extract into a constant
        return props.getOrDefault("transaction.it.proxy.username", "proxy").toString();
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
}
