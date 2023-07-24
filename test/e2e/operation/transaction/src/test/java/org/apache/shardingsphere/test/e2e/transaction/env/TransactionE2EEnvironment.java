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

package org.apache.shardingsphere.test.e2e.transaction.env;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.OpenGaussContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.PostgreSQLContainer;
import org.apache.shardingsphere.test.e2e.transaction.env.enums.TransactionE2EEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.transaction.env.enums.TransactionTestCaseRegistry;

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
public final class TransactionE2EEnvironment {
    
    private static final TransactionE2EEnvironment INSTANCE = new TransactionE2EEnvironment();
    
    private final Properties props;
    
    private final TransactionE2EEnvTypeEnum itEnvType;
    
    private final List<String> mysqlVersions;
    
    private final List<String> postgresqlVersions;
    
    private final List<String> openGaussVersions;
    
    private final List<String> needToRunTestCases;
    
    private final List<String> allowTransactionTypes;
    
    private final List<String> allowXAProviders;
    
    private final Map<String, TransactionTestCaseRegistry> transactionTestCaseRegistryMap;
    
    private TransactionE2EEnvironment() {
        props = loadProperties();
        itEnvType = TransactionE2EEnvTypeEnum.valueOf(props.getProperty("transaction.it.env.type", TransactionE2EEnvTypeEnum.NONE.name()).toUpperCase());
        mysqlVersions = splitProperty("transaction.it.docker.mysql.version");
        postgresqlVersions = splitProperty("transaction.it.docker.postgresql.version");
        openGaussVersions = splitProperty("transaction.it.docker.opengauss.version");
        needToRunTestCases = splitProperty("transaction.it.env.cases");
        allowTransactionTypes = splitProperty("transaction.it.env.transtypes");
        allowXAProviders = splitProperty("transaction.it.env.xa.providers");
        log.info("Loaded properties, allowTransactionTypes:{}, allowXAProviders:{}", allowTransactionTypes, allowXAProviders);
        transactionTestCaseRegistryMap = initTransactionTestCaseRegistryMap();
    }
    
    private Map<String, TransactionTestCaseRegistry> initTransactionTestCaseRegistryMap() {
        Map<String, TransactionTestCaseRegistry> result = new HashMap<>(TransactionTestCaseRegistry.values().length, 1);
        for (TransactionTestCaseRegistry each : TransactionTestCaseRegistry.values()) {
            result.put(each.getTestCaseClass().getName(), each);
        }
        return result;
    }
    
    private List<String> splitProperty(final String key) {
        return Arrays.stream(props.getOrDefault(key, "").toString().split(",")).filter(each -> !Strings.isNullOrEmpty(each)).map(String::trim).collect(Collectors.toList());
    }
    
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("env/it-env.properties")) {
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
     * Get actual data source default port.
     *
     * @param databaseType database type
     * @return default port
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public int getActualDataSourceDefaultPort(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "MySQL":
                return Integer.parseInt(props.getOrDefault("transaction.it.native.mysql.port", MySQLContainer.MYSQL_EXPOSED_PORT).toString());
            case "PostgreSQL":
                return Integer.parseInt(props.getOrDefault("transaction.it.native.postgresql.port", PostgreSQLContainer.POSTGRESQL_EXPOSED_PORT).toString());
            case "openGauss":
                return Integer.parseInt(props.getOrDefault("transaction.it.native.opengauss.port", OpenGaussContainer.OPENGAUSS_EXPOSED_PORT).toString());
            default:
                throw new UnsupportedOperationException(String.format("Unsupported database type: `%s`", databaseType.getType()));
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
     * @param databaseType database type
     * @return actual data source username
     */
    public String getActualDataSourceUsername(final DatabaseType databaseType) {
        return itEnvType == TransactionE2EEnvTypeEnum.NATIVE
                ? String.valueOf(props.getOrDefault(String.format("transaction.it.native.%s.username", databaseType.getType().toLowerCase()), ProxyContainerConstants.USERNAME))
                : StorageContainerConstants.USERNAME;
    }
    
    /**
     * Get actual data source password.
     *
     * @param databaseType database type
     * @return actual data source username
     */
    public String getActualDataSourcePassword(final DatabaseType databaseType) {
        return itEnvType == TransactionE2EEnvTypeEnum.NATIVE
                ? props.getOrDefault(String.format("transaction.it.native.%s.password", databaseType.getType().toLowerCase()), ProxyContainerConstants.PASSWORD).toString()
                : StorageContainerConstants.PASSWORD;
    }
    
    /**
     * Get proxy password.
     * 
     * @return proxy password
     */
    public String getProxyPassword() {
        // TODO this should extract into a constant
        return props.getOrDefault("transaction.it.proxy.password", ProxyContainerConstants.PASSWORD).toString();
    }
    
    /**
     * Get proxy userName.
     * 
     * @return proxy userName
     */
    public String getProxyUserName() {
        // TODO this should extract into a constant
        return props.getOrDefault("transaction.it.proxy.username", ProxyContainerConstants.USERNAME).toString();
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static TransactionE2EEnvironment getInstance() {
        return INSTANCE;
    }
}
