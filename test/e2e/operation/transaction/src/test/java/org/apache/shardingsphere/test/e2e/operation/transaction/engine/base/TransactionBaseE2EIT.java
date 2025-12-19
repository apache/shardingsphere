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

package org.apache.shardingsphere.test.e2e.operation.transaction.engine.base;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Adapter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase.TransactionTestCaseParameter;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.command.CommonSQLCommand;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.test.e2e.operation.transaction.env.TransactionE2EEnvironment;
import org.apache.shardingsphere.test.e2e.operation.transaction.env.enums.TransactionTestCaseRegistry;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.container.compose.TransactionDockerContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.framework.param.TransactionTestParameter;
import org.apache.shardingsphere.test.e2e.operation.transaction.util.TestCaseClassScanner;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

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
    
    private static final List<String> ALL_XA_PROVIDERS = Arrays.asList(TransactionTestConstants.ATOMIKOS, TransactionTestConstants.NARAYANA);
    
    private static final List<Class<? extends BaseTransactionTestCase>> TEST_CASES = TestCaseClassScanner.scan();
    
    private static final E2ETestEnvironment ENV = E2ETestEnvironment.getInstance();
    
    private static final TransactionE2EEnvironment TRANSACTION_ENV = TransactionE2EEnvironment.getInstance();
    
    private final CommonSQLCommand commonSQL;
    
    protected TransactionBaseE2EIT() {
        commonSQL = loadCommonSQLCommand();
    }
    
    private CommonSQLCommand loadCommonSQLCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertTransaction(final TransactionTestParameter testParam) throws SQLException {
        if (null == testParam) {
            return;
        }
        try (TransactionContainerComposer containerComposer = new TransactionContainerComposer(testParam)) {
            try {
                callTestCases(testParam, containerComposer);
            } finally {
                closeDataSource(containerComposer);
            }
        }
    }
    
    private void callTestCases(final TransactionTestParameter testParam, final TransactionContainerComposer containerComposer) throws SQLException {
        if (Adapter.PROXY.getValue().equalsIgnoreCase(testParam.getAdapter())) {
            for (TransactionType each : testParam.getTransactionTypes()) {
                if (TransactionType.LOCAL == each) {
                    log.info("Call transaction IT {}, alter transaction rule {}.", testParam, "");
                    alterTransactionRule(each, "", containerComposer);
                    doCallTestCases(testParam, each, "", containerComposer);
                } else if (TransactionType.XA == each) {
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
    
    @SneakyThrows(Exception.class)
    private static void closeDataSource(final TransactionContainerComposer containerComposer) {
        DataSource dataSource = containerComposer.getDataSource();
        if (dataSource instanceof AutoCloseable) {
            ((AutoCloseable) dataSource).close();
        }
    }
    
    private void alterTransactionRule(final TransactionType transactionType, final String providerType, final TransactionContainerComposer containerComposer) throws SQLException {
        if (transactionType == TransactionType.LOCAL) {
            alterLocalTransactionRule(containerComposer);
        } else if (transactionType == TransactionType.XA) {
            alterXaTransactionRule(providerType, containerComposer);
        }
    }
    
    private void doCallTestCases(final TransactionTestParameter testParam, final TransactionContainerComposer containerComposer) {
        for (Class<? extends BaseTransactionTestCase> each : testParam.getTransactionTestCaseClasses()) {
            log.info("Transaction IT {} -> {} test begin.", testParam, each.getSimpleName());
            try {
                each.getConstructor(TransactionTestCaseParameter.class).newInstance(
                        new TransactionTestCaseParameter(this, containerComposer.getDataSource(), testParam.getTransactionTypes().get(0))).execute(containerComposer);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error(String.format("Transaction IT %s -> %s test failed", testParam, each.getSimpleName()), ex);
                throw new RuntimeException(ex);
            }
            log.info("Transaction IT {} -> {} test end.", testParam, each.getSimpleName());
        }
    }
    
    private void doCallTestCases(final TransactionTestParameter testParam, final TransactionType transactionType, final String provider, final TransactionContainerComposer containerComposer) {
        for (Class<? extends BaseTransactionTestCase> each : testParam.getTransactionTestCaseClasses()) {
            if (!Arrays.asList(each.getAnnotation(TransactionTestCase.class).transactionTypes()).contains(transactionType)) {
                continue;
            }
            log.info("Call transaction IT {} -> {} -> {} -> {} test begin.", testParam, transactionType, provider, each.getSimpleName());
            try {
                each.getConstructor(TransactionTestCaseParameter.class).newInstance(
                        new TransactionTestCaseParameter(this, containerComposer.getDataSource(), transactionType)).execute(containerComposer);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error(String.format("Transaction IT %s -> %s test failed", testParam, each.getSimpleName()), ex);
                throw new RuntimeException(ex);
            }
            log.info("Call transaction IT {} -> {} -> {} -> {} test end.", testParam, transactionType, provider, each.getSimpleName());
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
        executeWithLog(connection, "DROP TABLE IF EXISTS account;");
    }
    
    private void alterLocalTransactionRule(final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getDataSource().getConnection()) {
            if (isExpectedTransactionRule(connection, TransactionType.LOCAL, "")) {
                return;
            }
            String alterLocalTransactionRule = commonSQL.getAlterLocalTransactionRule();
            executeWithLog(connection, alterLocalTransactionRule);
        }
        assertTrue(waitExpectedTransactionRule(TransactionType.LOCAL, "", containerComposer));
    }
    
    private void alterXaTransactionRule(final String providerType, final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getDataSource().getConnection()) {
            if (isExpectedTransactionRule(connection, TransactionType.XA, providerType)) {
                return;
            }
            String alterXaTransactionRule = commonSQL.getAlterXATransactionRule().replace("${providerType}", providerType);
            executeWithLog(connection, alterXaTransactionRule);
        }
        assertTrue(waitExpectedTransactionRule(TransactionType.XA, providerType, containerComposer));
    }
    
    private boolean isExpectedTransactionRule(final Connection connection, final TransactionType expectedTransType, final String expectedProviderType) throws SQLException {
        Map<String, String> transactionRuleMap = executeShowTransactionRule(connection);
        return Objects.equals(transactionRuleMap.get(TransactionTestConstants.DEFAULT_TYPE), expectedTransType.toString())
                && Objects.equals(transactionRuleMap.get(TransactionTestConstants.PROVIDER_TYPE), expectedProviderType);
    }
    
    private boolean waitExpectedTransactionRule(final TransactionType expectedTransType, final String expectedProviderType, final TransactionContainerComposer containerComposer) throws SQLException {
        Awaitility.await().pollDelay(5L, TimeUnit.SECONDS).until(() -> true);
        try (Connection connection = containerComposer.getDataSource().getConnection()) {
            int waitTimes = 0;
            do {
                if (isExpectedTransactionRule(connection, expectedTransType, expectedProviderType)) {
                    return true;
                }
                Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
                waitTimes++;
            } while (waitTimes <= 3);
            return false;
        }
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
    public void addResource(final Connection connection, final String databaseName, final TransactionContainerComposer containerComposer) throws SQLException {
        String addSourceResource = commonSQL.getSourceAddNewResourceTemplate()
                .replace("${user}", ENV.getNativeDatabaseEnvironment().getUser())
                .replace("${password}", ENV.getNativeDatabaseEnvironment().getPassword())
                .replace("${ds2}", getActualJdbcUrlTemplate(databaseName, containerComposer));
        executeWithLog(connection, addSourceResource);
        int resourceCount = countWithLog("SHOW STORAGE UNITS FROM sharding_db", containerComposer);
        Awaitility.await().pollDelay(5L, TimeUnit.SECONDS).until(() -> true);
        assertThat(resourceCount, is(3));
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName, final TransactionContainerComposer containerComposer) {
        StorageContainerConnectOption option = DatabaseTypedSPILoader.getService(StorageContainerOption.class, containerComposer.getDatabaseType()).getConnectOption();
        if (Type.DOCKER == ENV.getRunEnvironment().getType()) {
            DockerStorageContainer storageContainer = (DockerStorageContainer) ((TransactionDockerContainerComposer) containerComposer.getContainerComposer()).getStorageContainer();
            return option.getURL(containerComposer.getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getExposedPort(), databaseName);
        }
        return option.getURL("127.0.0.1", ENV.getNativeDatabaseEnvironment().getPort(containerComposer.getDatabaseType()), databaseName);
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
        log.info("Connection execute: {}.", sql);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private int countWithLog(final String sql, final TransactionContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getDataSource().getConnection()) {
            int retryNumber = 0;
            while (retryNumber <= 3) {
                try (
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(sql)) {
                    int result = 0;
                    while (resultSet.next()) {
                        result++;
                    }
                    return result;
                } catch (final SQLException ex) {
                    log.error("Data access error.", ex);
                }
                Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
                retryNumber++;
            }
        }
        throw new RuntimeException("Can't get result from proxy.");
    }
    
    private static boolean isEnabled() {
        return !TRANSACTION_ENV.getCases().isEmpty() && null != ENV.getRunEnvironment().getType();
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            TransactionE2ESettings settings = context.getRequiredTestClass().getAnnotation(TransactionE2ESettings.class);
            Preconditions.checkNotNull(settings, "Annotation TransactionE2ESettings is required.");
            return getTransactionTestParameters(settings.value()).stream().map(Arguments::of);
        }
        
        private Collection<TransactionTestParameter> getTransactionTestParameters(final Class<? extends TransactionBaseE2EIT> testCaseClass) {
            TransactionTestCaseRegistry registry = TRANSACTION_ENV.getTransactionTestCaseRegistryMap().get(testCaseClass.getName());
            Collection<TransactionTestParameter> result = getTestParameters(registry);
            // TODO zhangcheng make sure the test cases should not empty
            if (result.isEmpty()) {
                result.add(null);
            }
            return result;
        }
        
        private Collection<TransactionTestParameter> getTestParameters(final TransactionTestCaseRegistry registry) {
            return ENV.getDockerEnvironment().getDatabaseImages(registry.getDatabaseType()).stream().flatMap(each -> getTestParameters(registry, each).stream()).collect(Collectors.toList());
        }
        
        private Collection<TransactionTestParameter> getTestParameters(final TransactionTestCaseRegistry registry, final String databaseVersion) {
            if (TEST_CASES.isEmpty()) {
                log.warn("Transaction test cases are empty.");
            }
            if (Strings.isNullOrEmpty(databaseVersion)) {
                return Collections.emptyList();
            }
            Map<String, TransactionTestParameter> result = new LinkedHashMap<>();
            for (Class<? extends BaseTransactionTestCase> each : TEST_CASES) {
                if (!TRANSACTION_ENV.getCases().isEmpty() && !TRANSACTION_ENV.getCases().contains(each.getSimpleName())) {
                    log.info("Collect transaction test case, need to run cases don't contain this, skip: {}.", each.getName());
                    continue;
                }
                TransactionTestCase transactionTestCase = each.getAnnotation(TransactionTestCase.class);
                if (null == transactionTestCase) {
                    log.info("Collect transaction test case, annotation is null, skip: {}.", each.getName());
                    continue;
                }
                Optional<String> databaseType = Arrays.stream(transactionTestCase.dbTypes()).filter(dbTypes -> registry.getDatabaseType().getType().equalsIgnoreCase(dbTypes)).findAny();
                if (!databaseType.isPresent()) {
                    log.info("Collect transaction test case, dbType is not matched, skip: {}.", each.getName());
                    continue;
                }
                Optional<String> runAdapters = Arrays.stream(transactionTestCase.adapters()).filter(registry.getRunningAdaptor()::equalsIgnoreCase).findAny();
                if (!runAdapters.isPresent()) {
                    log.info("Collect transaction test case, runAdapter is not matched, skip: {}.", each.getName());
                    continue;
                }
                setTestParameters(result, registry, databaseVersion, transactionTestCase, transactionTestCase.scenario(), each);
            }
            return result.values();
        }
        
        private void setTestParameters(final Map<String, TransactionTestParameter> testParams, final TransactionTestCaseRegistry registry, final String databaseVersion,
                                       final TransactionTestCase transactionTestCase, final String scenario, final Class<? extends BaseTransactionTestCase> caseClass) {
            if (Adapter.PROXY.getValue().equals(registry.getRunningAdaptor())) {
                List<TransactionType> allowedTransactionTypes = TRANSACTION_ENV.getTransactionTypes().isEmpty() ? Arrays.stream(TransactionType.values()).collect(Collectors.toList())
                        : TRANSACTION_ENV.getTransactionTypes().stream().map(TransactionType::valueOf).collect(Collectors.toList());
                List<String> xaProviders = TRANSACTION_ENV.getXaProviders().isEmpty() ? ALL_XA_PROVIDERS : TRANSACTION_ENV.getXaProviders();
                setTestParameters(testParams, registry, databaseVersion, allowedTransactionTypes, xaProviders, scenario, caseClass);
                return;
            }
            for (TransactionType each : transactionTestCase.transactionTypes()) {
                if (!TRANSACTION_ENV.getTransactionTypes().isEmpty() && !TRANSACTION_ENV.getTransactionTypes().contains(each.toString())) {
                    log.info("Collect transaction test case, need to run transaction types don't contain this, skip: {}-{}.", caseClass.getName(), each);
                    continue;
                }
                setTestParameters(testParams, registry, databaseVersion, each, scenario, caseClass);
            }
        }
        
        private void setTestParameters(final Map<String, TransactionTestParameter> testParams, final TransactionTestCaseRegistry registry, final String databaseVersion,
                                       final TransactionType transactionType, final String scenario, final Class<? extends BaseTransactionTestCase> caseClass) {
            if (TransactionType.LOCAL == transactionType) {
                setTestParameters(testParams, registry, databaseVersion, Collections.singletonList(transactionType), Collections.singletonList(""), scenario, caseClass);
                return;
            }
            if (TransactionType.XA == transactionType) {
                for (String each : TRANSACTION_ENV.getXaProviders().isEmpty() ? ALL_XA_PROVIDERS : TRANSACTION_ENV.getXaProviders()) {
                    setTestParameters(testParams, registry, databaseVersion, Collections.singletonList(transactionType), Collections.singletonList(each), scenario, caseClass);
                }
            }
        }
        
        private void setTestParameters(final Map<String, TransactionTestParameter> testParams, final TransactionTestCaseRegistry registry, final String databaseVersion,
                                       final List<TransactionType> transactionTypes, final List<String> providers, final String scenario, final Class<? extends BaseTransactionTestCase> caseClass) {
            String key = getUniqueKey(registry.getDatabaseType(), registry.getRunningAdaptor(), transactionTypes, providers, scenario);
            testParams.putIfAbsent(key, new TransactionTestParameter(registry.getDatabaseType(),
                    registry.getRunningAdaptor(), ENV.getDockerEnvironment().getProxyPortBindings(), transactionTypes, providers, databaseVersion, scenario, new LinkedList<>()));
            testParams.get(key).getTransactionTestCaseClasses().add(caseClass);
        }
        
        private String getUniqueKey(final DatabaseType databaseType, final String runningAdapter, final List<TransactionType> transactionTypes, final List<String> providers, final String scenario) {
            return String.join(File.separator, databaseType.getType(), runningAdapter, transactionTypes.toString(), providers.toString(), scenario);
        }
    }
}
