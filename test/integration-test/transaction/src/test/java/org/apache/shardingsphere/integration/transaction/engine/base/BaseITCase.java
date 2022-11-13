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

package org.apache.shardingsphere.integration.transaction.engine.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionITEnvTypeEnum;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionTestCaseRegistry;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.apache.shardingsphere.integration.transaction.util.TestCaseClassScanner;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.AdapterContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.StorageContainerUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    protected static final String TRANSACTION_IT = "transaction_it";
    
    protected static final String DATA_SOURCE_0 = TRANSACTION_IT + "_0";
    
    protected static final String DATA_SOURCE_1 = TRANSACTION_IT + "_1";
    
    protected static final List<String> ALL_XA_PROVIDERS = Arrays.asList(TransactionTestConstants.ATOMIKOS, TransactionTestConstants.BITRONIX, TransactionTestConstants.NARAYANA);
    
    protected static final String SHARDING_DB = "sharding_db";
    
    private static final List<Class<? extends BaseTransactionTestCase>> TEST_CASES;
    
    private final BaseContainerComposer containerComposer;
    
    @Getter(AccessLevel.PUBLIC)
    private final CommonSQLCommand commonSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String adapter;
    
    @Getter
    private final AutoDataSource dataSource;
    
    static {
        long startTime = System.currentTimeMillis();
        TEST_CASES = TestCaseClassScanner.scan();
        log.info("Load transaction test case classes time consume: {}.", System.currentTimeMillis() - startTime);
    }
    
    public BaseITCase(final TransactionParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        adapter = parameterized.getAdapter();
        containerComposer = initializeContainerComposer(parameterized);
        commonSQLCommand = loadCommonSQLCommand();
        dataSource = isProxyAdapter(parameterized) ? createProxyDataSource() : createJdbcDataSource();
    }
    
    private BaseContainerComposer initializeContainerComposer(final TransactionParameterized parameterized) {
        BaseContainerComposer result = ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER
                ? new DockerContainerComposer(parameterized)
                : new NativeContainerComposer(parameterized.getDatabaseType());
        result.start();
        return result;
    }
    
    private CommonSQLCommand loadCommonSQLCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
    }
    
    final boolean isProxyAdapter(final TransactionParameterized parameterized) {
        return AdapterContainerConstants.PROXY.equalsIgnoreCase(parameterized.getAdapter());
    }
    
    private ProxyDataSource createProxyDataSource() {
        return new ProxyDataSource(containerComposer, SHARDING_DB, ENV.getProxyUserName(), ENV.getProxyPassword());
    }
    
    private JdbcDataSource createJdbcDataSource() {
        DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
        DockerStorageContainer storageContainer = dockerContainerComposer.getStorageContainer();
        Map<String, DataSource> actualDataSourceMap = storageContainer.getActualDataSourceMap();
        actualDataSourceMap.put("ds_0", createDataSource(storageContainer, DATA_SOURCE_0));
        actualDataSourceMap.put("ds_1", createDataSource(storageContainer, DATA_SOURCE_1));
        return new JdbcDataSource(dockerContainerComposer);
    }
    
    private DataSource createDataSource(final DockerStorageContainer storageContainer, final String dataSourceName) {
        return StorageContainerUtil.generateDataSource(DataSourceEnvironment.getURL(databaseType, storageContainer.getHost(), storageContainer.getMappedPort(), dataSourceName),
                storageContainer.getUsername(), storageContainer.getPassword(), 50);
    }
    
    protected static Collection<TransactionParameterized> getTransactionParameterizedList(final Class<? extends BaseTransactionITCase> testCaseClass) {
        TransactionTestCaseRegistry currentTestCaseInfo = ENV.getTransactionTestCaseRegistryMap().get(testCaseClass.getName());
        Collection<TransactionParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NONE) {
            return result;
        }
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            addParameters(currentTestCaseInfo, result);
        }
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NATIVE && "MySQL".equalsIgnoreCase(ENV.getNativeDatabaseType())) {
            addParametersByVersions(ENV.getMysqlVersions(), result, currentTestCaseInfo);
        }
        return result;
    }
    
    private static void addParameters(final TransactionTestCaseRegistry currentTestCaseInfo, final Collection<TransactionParameterized> result) {
        if (TransactionTestConstants.MYSQL.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
            addParametersByVersions(ENV.getMysqlVersions(), result, currentTestCaseInfo);
        } else if (TransactionTestConstants.POSTGRESQL.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
            addParametersByVersions(ENV.getPostgresVersions(), result, currentTestCaseInfo);
        } else if (TransactionTestConstants.OPENGAUSS.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
            addParametersByVersions(ENV.getOpenGaussVersions(), result, currentTestCaseInfo);
        }
    }
    
    private static void addParametersByVersions(final List<String> databaseVersion, final Collection<TransactionParameterized> result, final TransactionTestCaseRegistry currentTestCaseInfo) {
        for (String each : databaseVersion) {
            result.addAll(addParametersByTestCaseClasses(each, currentTestCaseInfo));
        }
    }
    
    private static Collection<TransactionParameterized> addParametersByTestCaseClasses(final String version, final TransactionTestCaseRegistry currentTestCaseInfo) {
        Map<String, TransactionParameterized> parameterizedMap = new LinkedHashMap<>();
        for (Class<? extends BaseTransactionTestCase> each : TEST_CASES) {
            if (!ENV.getNeedToRunTestCases().isEmpty() && !ENV.getNeedToRunTestCases().contains(each.getSimpleName())) {
                log.info("Collect transaction test case, need to run cases don't contain this, skip: {}.", each.getName());
                continue;
            }
            TransactionTestCase annotation = each.getAnnotation(TransactionTestCase.class);
            if (null == annotation) {
                log.info("Collect transaction test case, annotation is null, skip: {}.", each.getName());
                continue;
            }
            Optional<String> dbType = Arrays.stream(annotation.dbTypes()).filter(currentTestCaseInfo.getDbType()::equalsIgnoreCase).findAny();
            if (!dbType.isPresent()) {
                log.info("Collect transaction test case, dbType is not matched, skip: {}.", each.getName());
                continue;
            }
            Optional<String> runAdapters = Arrays.stream(annotation.adapters()).filter(currentTestCaseInfo.getRunningAdaptor()::equalsIgnoreCase).findAny();
            if (!runAdapters.isPresent()) {
                log.info("Collect transaction test case, runAdapter is not matched, skip: {}.", each.getName());
                continue;
            }
            String scenario = annotation.scenario();
            addParametersByTransactionTypes(version, currentTestCaseInfo, each, annotation, parameterizedMap, scenario);
        }
        return parameterizedMap.values();
    }
    
    private static void addParametersByTransactionTypes(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                                        final Class<? extends BaseTransactionTestCase> caseClass, final TransactionTestCase annotation,
                                                        final Map<String, TransactionParameterized> parameterizedMap, final String scenario) {
        if (AdapterContainerConstants.PROXY.equals(currentTestCaseInfo.getRunningAdaptor())) {
            List<TransactionType> allowTransactionTypes = ENV.getAllowTransactionTypes().isEmpty() ? Arrays.stream(TransactionType.values()).collect(Collectors.toList())
                    : ENV.getAllowTransactionTypes().stream().map(TransactionType::valueOf).collect(Collectors.toList());
            List<String> allowProviders = ENV.getAllowXAProviders().isEmpty() ? ALL_XA_PROVIDERS : ENV.getAllowXAProviders();
            addTestParameters(version, currentTestCaseInfo, caseClass, allowTransactionTypes, allowProviders, parameterizedMap, scenario);
        } else {
            for (TransactionType each : annotation.transactionTypes()) {
                if (!ENV.getAllowTransactionTypes().isEmpty() && !ENV.getAllowTransactionTypes().contains(each.toString())) {
                    log.info("Collect transaction test case, need to run transaction types don't contain this, skip: {}-{}.", caseClass.getName(), each);
                    continue;
                }
                addParametersByTransactionProvidersInJDBC(version, currentTestCaseInfo, caseClass, each, parameterizedMap, scenario);
            }
        }
    }
    
    private static void addParametersByTransactionProvidersInJDBC(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                                                  final Class<? extends BaseTransactionTestCase> caseClass, final TransactionType each,
                                                                  final Map<String, TransactionParameterized> parameterizedMap, final String scenario) {
        if (TransactionType.LOCAL.equals(each)) {
            addTestParameters(version, currentTestCaseInfo, caseClass, Collections.singletonList(each), Collections.singletonList(""), parameterizedMap, scenario);
        } else if (TransactionType.XA.equals(each)) {
            List<String> allowProviders = ENV.getAllowXAProviders().isEmpty() ? ALL_XA_PROVIDERS : ENV.getAllowXAProviders();
            for (String provider : allowProviders) {
                addTestParameters(version, currentTestCaseInfo, caseClass, Collections.singletonList(each), Collections.singletonList(provider), parameterizedMap, scenario);
            }
        }
    }
    
    private static void addTestParameters(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                          final Class<? extends BaseTransactionTestCase> caseClass, final List<TransactionType> transactionTypes, final List<String> providers,
                                          final Map<String, TransactionParameterized> parameterizedMap, final String scenario) {
        String uniqueKey = getUniqueKey(currentTestCaseInfo.getDbType(), currentTestCaseInfo.getRunningAdaptor(), transactionTypes, providers, scenario);
        parameterizedMap.putIfAbsent(uniqueKey, new TransactionParameterized(getSqlDatabaseType(currentTestCaseInfo.getDbType()), currentTestCaseInfo.getRunningAdaptor(), transactionTypes, providers,
                getStorageContainerImage(currentTestCaseInfo.getDbType(), version), scenario, new LinkedList<>()));
        parameterizedMap.get(uniqueKey).getTransactionTestCaseClasses().add(caseClass);
    }
    
    private static String getUniqueKey(final String dbType, final String runningAdapter, final List<TransactionType> transactionTypes, final List<String> providers, final String scenario) {
        return dbType + File.separator + runningAdapter + File.separator + transactionTypes + File.separator + providers + File.separator + scenario;
    }
    
    private static DatabaseType getSqlDatabaseType(final String databaseType) {
        switch (databaseType) {
            case TransactionTestConstants.MYSQL:
                return new MySQLDatabaseType();
            case TransactionTestConstants.POSTGRESQL:
                return new PostgreSQLDatabaseType();
            case TransactionTestConstants.OPENGAUSS:
                return new OpenGaussDatabaseType();
            default:
                throw new UnsupportedOperationException(String.format("Unsupported database type `%s`.", databaseType));
        }
    }
    
    private static String getStorageContainerImage(final String databaseType, final String version) {
        switch (databaseType) {
            case TransactionTestConstants.MYSQL:
                return "mysql/mysql-server:" + version;
            case TransactionTestConstants.POSTGRESQL:
            case TransactionTestConstants.OPENGAUSS:
                return version;
            default:
                throw new UnsupportedOperationException(String.format("Unsupported database type `%s`.", databaseType));
        }
    }
    
    final Connection getProxyConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName) {
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainer();
            return DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getExposedPort(), databaseName);
        }
        return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    /**
     * Add resource.
     *
     * @param connection connection
     * @param databaseName database name
     * @throws SQLException SQL exception
     */
    @SneakyThrows(InterruptedException.class)
    public final void addResource(final Connection connection, final String databaseName) throws SQLException {
        String addSourceResource = commonSQLCommand.getSourceAddNewResourceTemplate()
                .replace("${user}", ENV.getActualDataSourceUsername(databaseType))
                .replace("${password}", ENV.getActualDataSourcePassword(databaseType))
                .replace("${ds2}", getActualJdbcUrlTemplate(databaseName));
        executeWithLog(connection, addSourceResource);
        int resourceCount = countWithLog("SHOW STORAGE UNITS FROM sharding_db");
        Thread.sleep(5000L);
        assertThat(resourceCount, is(3));
    }
    
    /**
     * Create the account table rule with one data source.
     *
     * @param connection connection
     * @throws SQLException SQL exception
     */
    public final void createOriginalAccountTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP SHARDING TABLE RULE account;");
        executeWithLog(connection, getCommonSQLCommand().getCreateOneDataSourceAccountTableRule());
        assertThat(countWithLog("SHOW SHARDING TABLE RULES FROM sharding_db;"), is(3));
    }
    
    int countWithLog(final String sql) throws SQLException {
        Connection connection = getProxyConnection();
        int retryNumber = 0;
        while (retryNumber <= 3) {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                int result = 0;
                while (resultSet.next()) {
                    result++;
                }
                return result;
            } catch (final SQLException ex) {
                log.error("Data access error.", ex);
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            retryNumber++;
        }
        throw new RuntimeException("Can't get result from proxy.");
    }
    
    protected final void executeWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute:{}.", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
}
