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
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.engine.entity.JdbcInfoEntity;
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
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.StorageContainerUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.Properties;
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
    
    protected static final String DS_0 = TRANSACTION_IT + "_0";
    
    protected static final String DS_1 = TRANSACTION_IT + "_1";
    
    /**
     * For adding resource tests.
     */
    protected static final String DS_2 = TRANSACTION_IT + "_2";
    
    protected static final Collection<String> ALL_DS = Arrays.asList(DS_0, DS_1, DS_2);
    
    protected static final List<String> ALL_XA_PROVIDERS = Arrays.asList(TransactionTestConstants.ATOMIKOS, TransactionTestConstants.BITRONIX, TransactionTestConstants.NARAYANA);
    
    protected static final String SHARDING_DB = "sharding_db";
    
    private static final List<Class<? extends BaseTransactionTestCase>> TEST_CASES;
    
    private final BaseContainerComposer containerComposer;
    
    private final CommonSQLCommand commonSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String adapter;
    
    @Getter
    private AutoDataSource dataSource;
    
    static {
        long startTime = System.currentTimeMillis();
        TEST_CASES = TestCaseClassScanner.scan();
        log.info("Load transaction test case classes time consume: {}.", System.currentTimeMillis() - startTime);
    }
    
    public BaseITCase(final TransactionParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        adapter = parameterized.getAdapter();
        containerComposer = createAndStartContainerComposer(parameterized);
        commonSQLCommand = getSqlCommand();
        initActualDataSources();
        if (isProxyAdapter(parameterized)) {
            createProxyDatabase();
        } else {
            createJdbcDataSource();
        }
    }
    
    private void createJdbcDataSource() {
        if (containerComposer instanceof DockerContainerComposer) {
            DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
            DockerStorageContainer databaseContainer = dockerContainerComposer.getStorageContainer();
            Map<String, DataSource> actualDataSourceMap = databaseContainer.getActualDataSourceMap();
            actualDataSourceMap.put("ds_0", createDataSource(databaseContainer, DS_0));
            actualDataSourceMap.put("ds_1", createDataSource(databaseContainer, DS_1));
            dataSource = new JdbcDataSource(dockerContainerComposer);
        }
    }
    
    private DataSource createDataSource(final DockerStorageContainer databaseContainer, final String dataSourceName) {
        return StorageContainerUtil.generateDataSource(DataSourceEnvironment.getURL(databaseType, databaseContainer.getHost(), databaseContainer.getMappedPort(), dataSourceName),
                databaseContainer.getUsername(), databaseContainer.getPassword(), 50);
    }
    
    protected boolean isProxyAdapter(final TransactionParameterized parameterized) {
        return parameterized.getAdapter().equalsIgnoreCase(AdapterContainerConstants.PROXY);
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
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NATIVE && StringUtils.equalsIgnoreCase(ENV.getNativeDatabaseType(), "MySQL")) {
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
            String group = annotation.group();
            addParametersByTransactionTypes(version, currentTestCaseInfo, each, annotation, parameterizedMap, group);
        }
        
        return parameterizedMap.values();
    }
    
    private static void addParametersByTransactionTypes(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                                        final Class<? extends BaseTransactionTestCase> caseClass, final TransactionTestCase annotation,
                                                        final Map<String, TransactionParameterized> parameterizedMap, final String group) {
        if (AdapterContainerConstants.PROXY.equals(currentTestCaseInfo.getRunningAdaptor())) {
            List<TransactionType> allowTransactionTypes = ENV.getAllowTransactionTypes().isEmpty() ? Arrays.stream(TransactionType.values()).collect(Collectors.toList())
                    : ENV.getAllowTransactionTypes().stream().map(BaseITCase::getTransactionType).collect(Collectors.toList());
            List<String> allowProviders = ENV.getAllowXAProviders().isEmpty() ? ALL_XA_PROVIDERS : ENV.getAllowXAProviders();
            addTestParameters(version, currentTestCaseInfo, caseClass, allowTransactionTypes, allowProviders, parameterizedMap, group);
        } else {
            for (TransactionType each : annotation.transactionTypes()) {
                if (!ENV.getAllowTransactionTypes().isEmpty() && !ENV.getAllowTransactionTypes().contains(each.toString())) {
                    log.info("Collect transaction test case, need to run transaction types don't contain this, skip: {}-{}.", caseClass.getName(), each);
                    continue;
                }
                addParametersByTransactionProvidersInJDBC(version, currentTestCaseInfo, caseClass, each, parameterizedMap, group);
            }
        }
    }
    
    private static TransactionType getTransactionType(final String each) {
        switch (each) {
            case "LOCAL":
                return TransactionType.LOCAL;
            case "XA":
                return TransactionType.XA;
            case "BASE":
                return TransactionType.BASE;
            default:
                throw new UnsupportedOperationException("Unsupported transaction type.");
        }
    }
    
    private static void addParametersByTransactionProvidersInJDBC(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                                                  final Class<? extends BaseTransactionTestCase> caseClass, final TransactionType each,
                                                                  final Map<String, TransactionParameterized> parameterizedMap, final String group) {
        if (TransactionType.LOCAL.equals(each)) {
            addTestParameters(version, currentTestCaseInfo, caseClass, Collections.singletonList(each), Collections.singletonList(""), parameterizedMap, group);
        } else if (TransactionType.XA.equals(each)) {
            List<String> allowProviders = ENV.getAllowXAProviders().isEmpty() ? ALL_XA_PROVIDERS : ENV.getAllowXAProviders();
            for (String provider : allowProviders) {
                addTestParameters(version, currentTestCaseInfo, caseClass, Collections.singletonList(each), Collections.singletonList(provider), parameterizedMap, group);
            }
        }
    }
    
    private static void addTestParameters(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                          final Class<? extends BaseTransactionTestCase> caseClass, final List<TransactionType> transactionTypes, final List<String> providers,
                                          final Map<String, TransactionParameterized> parameterizedMap, final String group) {
        String uniqueKey = getUniqueKey(currentTestCaseInfo.getDbType(), currentTestCaseInfo.getRunningAdaptor(), transactionTypes, providers, group);
        parameterizedMap.putIfAbsent(uniqueKey, new TransactionParameterized(getSqlDatabaseType(currentTestCaseInfo.getDbType()), currentTestCaseInfo.getRunningAdaptor(), transactionTypes, providers,
                getStorageContainerImage(currentTestCaseInfo.getDbType(), version), group, new LinkedList<>()));
        parameterizedMap.get(uniqueKey).getTransactionTestCaseClasses().add(caseClass);
    }
    
    private static String getUniqueKey(final String dbType, final String runningAdapter, final List<TransactionType> transactionTypes, final List<String> providers, final String group) {
        return dbType + File.separator + runningAdapter + File.separator + transactionTypes + File.separator + providers + File.separator + group;
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
                throw new UnsupportedOperationException("Unsupported database type.");
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
                throw new UnsupportedOperationException("Unsupported database type.");
        }
    }
    
    private CommonSQLCommand getSqlCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
    }
    
    private BaseContainerComposer createAndStartContainerComposer(final TransactionParameterized parameterized) {
        final BaseContainerComposer containerComposer;
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            containerComposer = new DockerContainerComposer(parameterized);
        } else {
            containerComposer = new NativeContainerComposer(parameterized.getDatabaseType());
        }
        containerComposer.start();
        return containerComposer;
    }
    
    @SneakyThrows(SQLException.class)
    private void initActualDataSources() {
        JdbcInfoEntity jdbcInfo = getJdbcInfoEntity();
        String jdbcUrl = getJdbcUrl(jdbcInfo);
        dropDatabases(jdbcUrl, jdbcInfo);
        createDatabases(jdbcUrl, jdbcInfo);
    }
    
    private String getJdbcUrl(final JdbcInfoEntity jdbcInfo) {
        String jdbcUrl;
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
            DockerStorageContainer databaseContainer = dockerContainerComposer.getStorageContainer();
            jdbcUrl = databaseContainer.getJdbcUrl("");
        } else {
            jdbcUrl = DataSourceEnvironment.getURL(databaseType, "localhost", jdbcInfo.getPort());
        }
        return jdbcUrl;
    }
    
    private JdbcInfoEntity getJdbcInfoEntity() {
        JdbcInfoEntity jdbcInfo;
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            DockerContainerComposer dockerContainerComposer = (DockerContainerComposer) containerComposer;
            DockerStorageContainer databaseContainer = dockerContainerComposer.getStorageContainer();
            jdbcInfo = new JdbcInfoEntity(databaseContainer.getUsername(), databaseContainer.getPassword(), databaseContainer.getExposedPort());
        } else {
            jdbcInfo = ENV.getActualDatabaseJdbcInfo(getDatabaseType());
        }
        return jdbcInfo;
    }
    
    private void dropDatabases(final String jdbcUrl, final JdbcInfoEntity jdbcInfo) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcInfo.getUsername(), jdbcInfo.getPassword())) {
            for (String each : ALL_DS) {
                executeWithLog(connection, String.format("DROP DATABASE IF EXISTS %s", each));
            }
        }
    }
    
    private void createDatabases(final String jdbcUrl, final JdbcInfoEntity jdbcInfo) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcInfo.getUsername(), jdbcInfo.getPassword())) {
            for (String each : ALL_DS) {
                try {
                    executeWithLog(connection, String.format("CREATE DATABASE %s", each));
                } catch (final SQLException ex) {
                    log.error("Error occurred when create database. error msg={}", ex.getMessage());
                }
            }
        }
    }
    
    protected Connection getProxyConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    protected void createProxyDatabase() {
        String jdbcUrl = getProxyJdbcUrl(databaseType);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, ENV.getProxyUserName(), ENV.getProxyPassword())) {
            if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NATIVE) {
                executeWithLog(connection, "DROP DATABASE IF EXISTS " + SHARDING_DB);
            }
            executeWithLog(connection, "CREATE DATABASE " + SHARDING_DB);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        dataSource = getProxyDataSource(SHARDING_DB);
    }
    
    private String getProxyJdbcUrl(final DatabaseType databaseType) {
        String defaultDatabaseName = "";
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            defaultDatabaseName = "postgres";
        }
        String jdbcUrl = containerComposer.getProxyJdbcUrl(defaultDatabaseName);
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            jdbcUrl = JDBC_URL_APPENDER.appendQueryProperties(jdbcUrl, getPostgreSQLQueryProperties());
        }
        return jdbcUrl;
    }
    
    protected AutoDataSource getProxyDataSource(final String databaseName) {
        return new ProxyDataSource(containerComposer, databaseName, ENV.getProxyUserName(), ENV.getProxyPassword());
    }
    
    protected boolean waitShardingAlgorithmEffect(final int maxWaitTimes) throws SQLException {
        long startTime = System.currentTimeMillis();
        int waitTimes = 0;
        do {
            int result = countWithLog("SHOW SHARDING ALGORITHMS");
            if (result >= 5) {
                log.info("waitShardingAlgorithmEffect time consume: {}", System.currentTimeMillis() - startTime);
                return true;
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            waitTimes++;
        } while (waitTimes <= maxWaitTimes);
        return false;
    }
    
    protected void addResources() throws SQLException {
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            try (Connection connection = DriverManager.getConnection(getContainerComposer().getProxyJdbcUrl(""), ENV.getProxyUserName(), ENV.getProxyPassword())) {
                executeWithLog(connection, "USE sharding_db");
                addResources(connection);
            }
        } else {
            Properties queryProps = getPostgreSQLQueryProperties();
            try (
                    Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getContainerComposer().getProxyJdbcUrl("sharding_db"), queryProps),
                            ENV.getProxyUserName(), ENV.getProxyPassword())) {
                addResources(connection);
            }
        }
        int resourceCount = countWithLog("SHOW DATABASE RESOURCES FROM sharding_db");
        assertThat(resourceCount, is(2));
    }
    
    protected void addResources(final Connection connection) throws SQLException {
        String addSourceResource = commonSQLCommand.getSourceAddResourceTemplate()
                .replace("${user}", ENV.getActualDataSourceUsername(databaseType))
                .replace("${password}", ENV.getActualDataSourcePassword(databaseType))
                .replace("${ds0}", getActualJdbcUrlTemplate(DS_0))
                .replace("${ds1}", getActualJdbcUrlTemplate(DS_1));
        executeWithLog(connection, addSourceResource);
    }
    
    /**
     * Add ds_2 resource to proxy.
     *
     * @param connection connection
     * @throws SQLException SQL exception
     */
    @SneakyThrows(InterruptedException.class)
    public void addNewResource(final Connection connection) throws SQLException {
        String addSourceResource = commonSQLCommand.getSourceAddNewResourceTemplate()
                .replace("${user}", ENV.getActualDataSourceUsername(databaseType))
                .replace("${password}", ENV.getActualDataSourcePassword(databaseType))
                .replace("${ds2}", getActualJdbcUrlTemplate(DS_2));
        executeWithLog(connection, addSourceResource);
        int resourceCount = countWithLog("SHOW DATABASE RESOURCES FROM sharding_db");
        Thread.sleep(5000);
        assertThat(resourceCount, is(3));
    }
    
    /**
     * Drop previous account table rule and create the table rule with three data sources.
     *
     * @param connection connection
     * @throws SQLException SQL exception
     */
    public void createThreeDataSourceAccountTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP SHARDING TABLE RULE account;");
        executeWithLog(connection, getCommonSQLCommand().getCreateThreeDataSourceAccountTableRule());
        int ruleCount = countWithLog("SHOW SHARDING TABLE RULES FROM sharding_db;");
        assertThat(ruleCount, is(3));
    }
    
    /**
     * Create the account table rule with one data source.
     *
     * @param connection connection
     * @throws SQLException SQL exception
     */
    public void createOriginalAccountTableRule(final Connection connection) throws SQLException {
        executeWithLog(connection, "DROP SHARDING TABLE RULE account;");
        executeWithLog(connection, getCommonSQLCommand().getCreateOneDataSourceAccountTableRule());
        int ruleCount = countWithLog("SHOW SHARDING TABLE RULES FROM sharding_db;");
        assertThat(ruleCount, is(3));
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName) {
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            final DockerStorageContainer databaseContainer = ((DockerContainerComposer) containerComposer).getStorageContainer();
            return DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", databaseContainer.getExposedPort(), databaseName);
        } else {
            return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
        }
    }
    
    protected void initShardingAlgorithm() throws SQLException {
        Connection connection = getProxyConnection();
        executeWithLog(connection, commonSQLCommand.getCreateDatabaseShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateDatabaseIdShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateOrderShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateOrderItemShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateAccountShardingAlgorithm());
    }
    
    protected void createSchema(final String schemaName) throws SQLException {
        Connection connection = getProxyConnection();
        executeWithLog(connection, String.format("CREATE SCHEMA %s", schemaName));
    }
    
    protected int countWithLog(final String sql) throws SQLException {
        Connection connection = getProxyConnection();
        int retryNumber = 0;
        while (retryNumber <= 3) {
            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                int result = 0;
                while (rs.next()) {
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
    
    protected void executeWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute:{}.", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
    
    protected void executeUpdateWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute update:{}.", sql);
        connection.createStatement().executeUpdate(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
    
    /**
     * Get query properties by database type.
     *
     * @return query properties
     */
    public static Properties getPostgreSQLQueryProperties() {
        Properties result = new Properties();
        result.put("preferQueryMode", "extendedForPrepared");
        return result;
    }
}
