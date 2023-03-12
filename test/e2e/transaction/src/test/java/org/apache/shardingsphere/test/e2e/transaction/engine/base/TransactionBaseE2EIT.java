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

package org.apache.shardingsphere.test.e2e.transaction.engine.base;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.command.CommonSQLCommand;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.test.e2e.transaction.env.TransactionE2EEnvironment;
import org.apache.shardingsphere.test.e2e.transaction.env.enums.TransactionE2EEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.transaction.env.enums.TransactionTestCaseRegistry;
import org.apache.shardingsphere.test.e2e.transaction.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.transaction.framework.param.TransactionTestParameter;
import org.apache.shardingsphere.test.e2e.transaction.util.TestCaseClassScanner;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
@Slf4j
public abstract class TransactionBaseE2EIT {
    
    private static final List<String> ALL_XA_PROVIDERS = Arrays.asList(TransactionTestConstants.ATOMIKOS, TransactionTestConstants.BITRONIX, TransactionTestConstants.NARAYANA);
    
    private static final List<Class<? extends BaseTransactionTestCase>> TEST_CASES = TestCaseClassScanner.scan();
    
    private static final TransactionE2EEnvironment ENV = TransactionE2EEnvironment.getInstance();
    
    private final CommonSQLCommand commonSQL;
    
    public TransactionBaseE2EIT() {
        commonSQL = loadCommonSQLCommand();
    }
    
    private CommonSQLCommand loadCommonSQLCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(TransactionBaseE2EIT.class.getClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertTransaction(final TransactionTestParameter testParam) throws SQLException {
        try (TransactionContainerComposer containerComposer = new TransactionContainerComposer(testParam)) {
            try {
                callTestCases(testParam, containerComposer);
            } finally {
                containerComposer.getDataSource().close();
            }
        }
    }
    
    private void callTestCases(final TransactionTestParameter testParam, final TransactionContainerComposer containerComposer) throws SQLException {
        if (AdapterType.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter())) {
            for (TransactionType each : testParam.getTransactionTypes()) {
                if (TransactionType.LOCAL.equals(each)) {
                    log.info("Call transaction IT {}, alter transaction rule {}.", testParam, "");
                    alterTransactionRule(each, "", containerComposer);
                    doCallTestCases(testParam, each, "", containerComposer);
                } else if (TransactionType.XA.equals(each)) {
                    for (String eachProvider : testParam.getProviders()) {
                        log.info("Call transaction IT {}, alter transaction rule {}.", testParam, eachProvider);
                        alterTransactionRule(each, eachProvider, containerComposer);
                        doCallTestCases(testParam, each, eachProvider, containerComposer);
                    }
                }
            }
        } else {
            doCallTestCases(testParam, containerComposer);
        }
    }
    
    private void alterTransactionRule(final TransactionType transactionType, final String providerType, final TransactionContainerComposer containerComposer) throws SQLException {
        if (Objects.equals(transactionType, TransactionType.LOCAL)) {
            alterLocalTransactionRule(containerComposer);
        } else if (Objects.equals(transactionType, TransactionType.XA)) {
            alterXaTransactionRule(providerType, containerComposer);
        }
    }
    
    private void doCallTestCases(final TransactionTestParameter testParam, final TransactionContainerComposer containerComposer) {
        for (Class<? extends BaseTransactionTestCase> each : testParam.getTransactionTestCaseClasses()) {
            log.info("Transaction IT {} -> {} test begin.", testParam, each.getSimpleName());
            try {
                each.getConstructor(TransactionBaseE2EIT.class, DataSource.class).newInstance(this, containerComposer.getDataSource()).execute(containerComposer);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error(String.format("Transaction IT %s -> %s test failed", testParam, each.getSimpleName()), ex);
                throw new RuntimeException(ex);
            }
            log.info("Transaction IT {} -> {} test end.", testParam, each.getSimpleName());
            try {
                containerComposer.getDataSource().close();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    private void doCallTestCases(final TransactionTestParameter testParam, final TransactionType transactionType, final String provider, final TransactionContainerComposer containerComposer) {
        for (Class<? extends BaseTransactionTestCase> each : testParam.getTransactionTestCaseClasses()) {
            if (!Arrays.asList(each.getAnnotation(TransactionTestCase.class).transactionTypes()).contains(transactionType)) {
                return;
            }
            log.info("Call transaction IT {} -> {} -> {} -> {} test begin.", testParam, transactionType, provider, each.getSimpleName());
            try {
                each.getConstructor(TransactionBaseE2EIT.class, DataSource.class).newInstance(this, containerComposer.getDataSource()).execute(containerComposer);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error(String.format("Transaction IT %s -> %s test failed", testParam, each.getSimpleName()), ex);
                throw new RuntimeException(ex);
            }
            log.info("Call transaction IT {} -> {} -> {} -> {} test end.", testParam, transactionType, provider, each.getSimpleName());
            try {
                containerComposer.getDataSource().close();
            } catch (final SQLException ignored) {
            }
        }
    }
    
    /**
     * Create account table.
     * 
     * @param connection connection 
     * @throws SQLException SQL exception
     */
    public void createAccountTable(final Connection connection) throws SQLException {
        executeWithLog(connection, commonSQL.getCreateAccountTable());
    }
    
    /**
     * Drop account table.
     *
     * @param connection connection 
     * @throws SQLException SQL exception
     */
    public void dropAccountTable(final Connection connection) throws SQLException {
        executeWithLog(connection, "drop table if exists account;");
    }
    
    private void alterLocalTransactionRule(final TransactionContainerComposer containerComposer) throws SQLException {
        Connection connection = containerComposer.getDataSource().getConnection();
        if (isExpectedTransactionRule(connection, TransactionType.LOCAL, "")) {
            return;
        }
        String alterLocalTransactionRule = commonSQL.getAlterLocalTransactionRule();
        executeWithLog(connection, alterLocalTransactionRule);
        assertTrue(waitExpectedTransactionRule(TransactionType.LOCAL, "", containerComposer));
    }
    
    private void alterXaTransactionRule(final String providerType, final TransactionContainerComposer containerComposer) throws SQLException {
        Connection connection = containerComposer.getDataSource().getConnection();
        if (isExpectedTransactionRule(connection, TransactionType.XA, providerType)) {
            return;
        }
        String alterXaTransactionRule = commonSQL.getAlterXATransactionRule().replace("${providerType}", providerType);
        executeWithLog(connection, alterXaTransactionRule);
        assertTrue(waitExpectedTransactionRule(TransactionType.XA, providerType, containerComposer));
    }
    
    private boolean isExpectedTransactionRule(final Connection connection, final TransactionType expectedTransType, final String expectedProviderType) throws SQLException {
        Map<String, String> transactionRuleMap = executeShowTransactionRule(connection);
        return Objects.equals(transactionRuleMap.get(TransactionTestConstants.DEFAULT_TYPE), expectedTransType.toString())
                && Objects.equals(transactionRuleMap.get(TransactionTestConstants.PROVIDER_TYPE), expectedProviderType);
    }
    
    private boolean waitExpectedTransactionRule(final TransactionType expectedTransType, final String expectedProviderType, final TransactionContainerComposer containerComposer) throws SQLException {
        ThreadUtil.sleep(5, TimeUnit.SECONDS);
        Connection connection = containerComposer.getDataSource().getConnection();
        int waitTimes = 0;
        do {
            if (isExpectedTransactionRule(connection, expectedTransType, expectedProviderType)) {
                return true;
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            waitTimes++;
        } while (waitTimes <= 3);
        return false;
    }
    
    private Map<String, String> executeShowTransactionRule(final Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SHOW TRANSACTION RULE;");
        Map<String, String> result = new HashMap<>();
        while (resultSet.next()) {
            String defaultType = resultSet.getString(TransactionTestConstants.DEFAULT_TYPE);
            String providerType = resultSet.getString(TransactionTestConstants.PROVIDER_TYPE);
            result.put(TransactionTestConstants.DEFAULT_TYPE, defaultType);
            result.put(TransactionTestConstants.PROVIDER_TYPE, providerType);
        }
        statement.close();
        return result;
    }
    
    /**
     * Add resource.
     *
     * @param connection connection
     * @param databaseName database name
     * @param containerComposer container composer
     * @throws SQLException SQL exception
     */
    @SneakyThrows(InterruptedException.class)
    public void addResource(final Connection connection, final String databaseName, final TransactionContainerComposer containerComposer) throws SQLException {
        String addSourceResource = commonSQL.getSourceAddNewResourceTemplate()
                .replace("${user}", ENV.getActualDataSourceUsername(containerComposer.getDatabaseType()))
                .replace("${password}", ENV.getActualDataSourcePassword(containerComposer.getDatabaseType()))
                .replace("${ds2}", getActualJdbcUrlTemplate(databaseName, containerComposer));
        executeWithLog(connection, addSourceResource);
        int resourceCount = countWithLog("SHOW STORAGE UNITS FROM sharding_db", containerComposer);
        Thread.sleep(5000L);
        assertThat(resourceCount, is(3));
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName, final TransactionContainerComposer containerComposer) {
        if (ENV.getItEnvType() == TransactionE2EEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer.getContainerComposer()).getStorageContainer();
            return DataSourceEnvironment.getURL(containerComposer.getDatabaseType(),
                    containerComposer.getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getExposedPort(), databaseName);
        }
        return DataSourceEnvironment.getURL(containerComposer.getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(containerComposer.getDatabaseType()), databaseName);
    }
    
    /**
     * Create the account table rule with one data source.
     *
     * @param connection connection
     * @param containerComposer container composer 
     * @throws SQLException SQL exception
     */
    public void createOriginalAccountTableRule(final Connection connection, final TransactionContainerComposer containerComposer) throws SQLException {
        executeWithLog(connection, "DROP SHARDING TABLE RULE account;");
        executeWithLog(connection, commonSQL.getCreateOneDataSourceAccountTableRule());
        assertThat(countWithLog("SHOW SHARDING TABLE RULES FROM sharding_db;", containerComposer), is(3));
    }
    
    private void executeWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute:{}.", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
    
    private int countWithLog(final String sql, final TransactionContainerComposer containerComposer) throws SQLException {
        Connection connection = containerComposer.getDataSource().getConnection();
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
    
    private static boolean isEnabled() {
        return ENV.getItEnvType() != TransactionE2EEnvTypeEnum.NONE;
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            TransactionE2ESettings settings = extensionContext.getRequiredTestClass().getAnnotation(TransactionE2ESettings.class);
            Preconditions.checkNotNull(settings, "Annotation TransactionE2ESettings is required.");
            return getTransactionTestParameters(settings.value()).stream().map(Arguments::of);
        }
        
        private Collection<TransactionTestParameter> getTransactionTestParameters(final Class<? extends TransactionBaseE2EIT> testCaseClass) {
            TransactionTestCaseRegistry currentTestCaseInfo = ENV.getTransactionTestCaseRegistryMap().get(testCaseClass.getName());
            Collection<TransactionTestParameter> result = new LinkedList<>();
            if (ENV.getItEnvType() == TransactionE2EEnvTypeEnum.NONE) {
                return result;
            }
            if (ENV.getItEnvType() == TransactionE2EEnvTypeEnum.DOCKER) {
                addTestParameters(currentTestCaseInfo, result);
            }
            if (ENV.getItEnvType() == TransactionE2EEnvTypeEnum.NATIVE && "MySQL".equalsIgnoreCase(ENV.getNativeDatabaseType())) {
                addParametersByVersions(ENV.getMysqlVersions(), result, currentTestCaseInfo);
            }
            return result;
        }
    
        private void addTestParameters(final TransactionTestCaseRegistry currentTestCaseInfo, final Collection<TransactionTestParameter> testParams) {
            if (TransactionTestConstants.MYSQL.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
                addParametersByVersions(ENV.getMysqlVersions(), testParams, currentTestCaseInfo);
            } else if (TransactionTestConstants.POSTGRESQL.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
                addParametersByVersions(ENV.getPostgresqlVersions(), testParams, currentTestCaseInfo);
            } else if (TransactionTestConstants.OPENGAUSS.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
                addParametersByVersions(ENV.getOpenGaussVersions(), testParams, currentTestCaseInfo);
            }
        }
    
        private void addParametersByVersions(final List<String> databaseVersion, final Collection<TransactionTestParameter> testParams, final TransactionTestCaseRegistry currentTestCaseInfo) {
            for (String each : databaseVersion) {
                testParams.addAll(addParametersByTestCaseClasses(each, currentTestCaseInfo));
            }
        }
    
        private Collection<TransactionTestParameter> addParametersByTestCaseClasses(final String version, final TransactionTestCaseRegistry currentTestCaseInfo) {
            Map<String, TransactionTestParameter> testParams = new LinkedHashMap<>();
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
                addParametersByTransactionTypes(version, currentTestCaseInfo, each, annotation, testParams, scenario);
            }
            return testParams.values();
        }
    
        private void addParametersByTransactionTypes(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                                            final Class<? extends BaseTransactionTestCase> caseClass, final TransactionTestCase annotation,
                                                            final Map<String, TransactionTestParameter> testParams, final String scenario) {
            if (AdapterType.PROXY.getValue().equals(currentTestCaseInfo.getRunningAdaptor())) {
                List<TransactionType> allowTransactionTypes = ENV.getAllowTransactionTypes().isEmpty() ? Arrays.stream(TransactionType.values()).collect(Collectors.toList())
                        : ENV.getAllowTransactionTypes().stream().map(TransactionType::valueOf).collect(Collectors.toList());
                List<String> allowProviders = ENV.getAllowXAProviders().isEmpty() ? ALL_XA_PROVIDERS : ENV.getAllowXAProviders();
                addParameters(version, currentTestCaseInfo, caseClass, allowTransactionTypes, allowProviders, testParams, scenario);
            } else {
                for (TransactionType each : annotation.transactionTypes()) {
                    if (!ENV.getAllowTransactionTypes().isEmpty() && !ENV.getAllowTransactionTypes().contains(each.toString())) {
                        log.info("Collect transaction test case, need to run transaction types don't contain this, skip: {}-{}.", caseClass.getName(), each);
                        continue;
                    }
                    addParametersByTransactionProvidersInJDBC(version, currentTestCaseInfo, caseClass, each, testParams, scenario);
                }
            }
        }
    
        private void addParametersByTransactionProvidersInJDBC(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                                                      final Class<? extends BaseTransactionTestCase> caseClass, final TransactionType each,
                                                                      final Map<String, TransactionTestParameter> testParams, final String scenario) {
            if (TransactionType.LOCAL.equals(each)) {
                addParameters(version, currentTestCaseInfo, caseClass, Collections.singletonList(each), Collections.singletonList(""), testParams, scenario);
            } else if (TransactionType.XA.equals(each)) {
                List<String> allowProviders = ENV.getAllowXAProviders().isEmpty() ? ALL_XA_PROVIDERS : ENV.getAllowXAProviders();
                for (String provider : allowProviders) {
                    addParameters(version, currentTestCaseInfo, caseClass, Collections.singletonList(each), Collections.singletonList(provider), testParams, scenario);
                }
            }
        }
    
        private void addParameters(final String version, final TransactionTestCaseRegistry currentTestCaseInfo,
                                          final Class<? extends BaseTransactionTestCase> caseClass, final List<TransactionType> transactionTypes, final List<String> providers,
                                          final Map<String, TransactionTestParameter> testParams, final String scenario) {
            String uniqueKey = getUniqueKey(currentTestCaseInfo.getDbType(), currentTestCaseInfo.getRunningAdaptor(), transactionTypes, providers, scenario);
            testParams.putIfAbsent(uniqueKey, new TransactionTestParameter(getSqlDatabaseType(currentTestCaseInfo.getDbType()), currentTestCaseInfo.getRunningAdaptor(), transactionTypes, providers,
                    getStorageContainerImage(currentTestCaseInfo.getDbType(), version), scenario, new LinkedList<>()));
            testParams.get(uniqueKey).getTransactionTestCaseClasses().add(caseClass);
        }
    
        private String getUniqueKey(final String dbType, final String runningAdapter, final List<TransactionType> transactionTypes, final List<String> providers, final String scenario) {
            return dbType + File.separator + runningAdapter + File.separator + transactionTypes + File.separator + providers + File.separator + scenario;
        }
    
        private DatabaseType getSqlDatabaseType(final String databaseType) {
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
    
        private String getStorageContainerImage(final String databaseType, final String version) {
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
    }
}
