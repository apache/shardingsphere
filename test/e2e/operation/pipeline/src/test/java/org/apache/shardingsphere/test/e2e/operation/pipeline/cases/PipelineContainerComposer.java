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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.connector.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.single.yaml.config.YamlSingleRuleConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.pipeline.command.ExtraSQLCommand;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.PipelineBaseContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.docker.PipelineDockerContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose.natived.PipelineNativeContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.ProxyDatabaseTypeUtils;
import org.awaitility.Awaitility;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pipeline container composer.
 */
@Getter
@Slf4j
public final class PipelineContainerComposer implements AutoCloseable {
    
    public static final String SCHEMA_NAME = "test";
    
    public static final String DS_0 = "pipeline_e2e_0";
    
    public static final String DS_1 = "pipeline_e2e_1";
    
    public static final String DS_2 = "pipeline_e2e_2";
    
    public static final String DS_3 = "pipeline_e2e_3";
    
    public static final String DS_4 = "pipeline_e2e_4";
    
    public static final int TABLE_INIT_ROW_COUNT = 3000;
    
    private static final String PROXY_DATABASE = "sharding_db";
    
    private final PipelineBaseContainerComposer containerComposer;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private final DataSource sourceDataSource;
    
    private final DataSource proxyDataSource;
    
    private Thread increaseTaskThread;
    
    public PipelineContainerComposer(final PipelineTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        Type type = E2ETestEnvironment.getInstance().getRunEnvironment().getType();
        containerComposer = Type.DOCKER == type
                ? new PipelineDockerContainerComposer(testParam.getDatabaseType(), testParam.getDatabaseContainerImage(), testParam.getStorageContainerCount())
                : new PipelineNativeContainerComposer(testParam.getDatabaseType());
        if (Type.DOCKER == type) {
            username = StorageContainerConstants.OPERATION_USER;
            password = StorageContainerConstants.OPERATION_PASSWORD;
        } else {
            username = E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getUser();
            password = E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getPassword();
        }
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(testParam.getScenario())), ExtraSQLCommand.class);
        containerComposer.start();
        sourceDataSource = StorageContainerUtils.generateDataSource(getActualJdbcUrlTemplate(DS_0, false), username, password, 2);
        proxyDataSource = StorageContainerUtils.generateDataSource(
                appendExtraParameter(containerComposer.getProxyJdbcUrl(PROXY_DATABASE)), ProxyContainerConstants.USER, ProxyContainerConstants.PASSWORD, 2);
        init();
    }
    
    @SneakyThrows(SQLException.class)
    private void init() {
        String jdbcUrl = containerComposer.getProxyJdbcUrl(databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? "postgres" : "");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, ProxyContainerConstants.USER, ProxyContainerConstants.PASSWORD)) {
            cleanUpPipelineJobs(connection);
            cleanUpProxyDatabase(connection);
            // Compatible with "drop database if exists sharding_db;" failed for now
            cleanUpProxyDatabase(connection);
            createProxyDatabase(connection);
        }
        cleanUpDataSource();
    }
    
    private void cleanUpPipelineJobs(final Connection connection) throws SQLException {
        if (Type.NATIVE != E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            return;
        }
        for (PipelineJobType<?> each : ShardingSphereServiceLoader.getServiceInstances(PipelineJobType.class)) {
            if (!each.getOption().isTransmissionJob()) {
                continue;
            }
            cleanUpPipelineJobsWithType(connection, each);
        }
    }
    
    private void cleanUpPipelineJobsWithType(final Connection connection, final PipelineJobType<?> jobType) throws SQLException {
        String jobTypeName = jobType.getType();
        for (Map<String, Object> each : queryJobs(connection, jobTypeName)) {
            String jobId = each.get("id").toString();
            List<Map<String, Object>> jobInfos = queryForListWithLog(String.format("SHOW %s STATUS '%s'", jobTypeName, jobId));
            String status = !jobInfos.isEmpty() ? jobInfos.get(0).get("status").toString() : "";
            String sql = String.format("%s %s '%s'", getOperationType(jobType, status), jobTypeName, jobId);
            log.info("Clean up job, sql: {}", sql);
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }
    
    private String getOperationType(final PipelineJobType<?> jobType, final String status) {
        return isSupportCommitRollback(jobType) ? (JobStatus.FINISHED.name().equals(status) ? "COMMIT" : "ROLLBACK") : "DROP";
    }
    
    private boolean isSupportCommitRollback(final PipelineJobType<?> jobType) {
        return !(jobType instanceof CDCJobType);
    }
    
    private List<Map<String, Object>> queryJobs(final Connection connection, final String jobTypeName) {
        String sql = String.format("SHOW %s LIST", jobTypeName);
        try (Statement statement = connection.createStatement()) {
            log.info("Execute SQL: {}", sql);
            return transformResultSetToList(statement.executeQuery(sql));
        } catch (final SQLException ex) {
            log.warn("{} execute failed, message {}", sql, ex.getMessage());
            return Collections.emptyList();
        }
    }
    
    private void cleanUpProxyDatabase(final Connection connection) {
        if (Type.NATIVE != E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("DROP DATABASE IF EXISTS %s", PROXY_DATABASE));
        } catch (final SQLException ex) {
            log.warn("Drop proxy database failed, error={}", ex.getMessage());
        }
        sleepSeconds(2);
    }
    
    private void createProxyDatabase(final Connection connection) throws SQLException {
        String sql = String.format("CREATE DATABASE %s", PROXY_DATABASE);
        log.info("Create proxy database {}", PROXY_DATABASE);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            sleepSeconds(2);
        }
    }
    
    private void cleanUpDataSource() {
        if (Type.NATIVE != E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            return;
        }
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(databaseType);
        for (String each : Arrays.asList(DS_0, DS_1, DS_2, DS_3, DS_4)) {
            containerComposer.cleanUpDatabase(databaseTypeRegistry.formatIdentifierPattern(each));
        }
    }
    
    /**
     * Sleep seconds.
     *
     * @param seconds seconds
     */
    public void sleepSeconds(final int seconds) {
        if (seconds <= 0) {
            return;
        }
        // Awaitility: WaitConstraint defaultWaitConstraint = AtMostWaitConstraint.TEN_SECONDS
        Awaitility.waitAtMost(seconds + 1, TimeUnit.SECONDS).pollDelay(seconds, TimeUnit.SECONDS).until(() -> true);
    }
    
    /**
     * Append extra parameter.
     *
     * @param jdbcUrl JDBC URL
     * @return appended JDBC URL
     */
    public String appendExtraParameter(final String jdbcUrl) {
        if (databaseType instanceof MySQLDatabaseType) {
            return new JdbcUrlAppender().appendQueryProperties(jdbcUrl, PropertiesBuilder.build(new Property("rewriteBatchedStatements", Boolean.TRUE.toString())));
        }
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            return new JdbcUrlAppender().appendQueryProperties(jdbcUrl, PropertiesBuilder.build(new Property("stringtype", "unspecified"),
                    new Property("bitToString", Boolean.TRUE.toString()), new Property("TimeZone", "UTC")));
        }
        return jdbcUrl;
    }
    
    /**
     * Register storage unit.
     *
     * @param storageUnitName storage unit name
     * @throws SQLException SQL exception
     */
    public void registerStorageUnit(final String storageUnitName) throws SQLException {
        String username = ProxyDatabaseTypeUtils.isOracleBranch(databaseType) ? storageUnitName : getUsername();
        String registerStorageUnitTemplate = "REGISTER STORAGE UNIT ${ds} ( URL='${url}', USER='${user}', PASSWORD='${password}')".replace("${ds}", storageUnitName)
                .replace("${user}", username)
                .replace("${password}", getPassword())
                .replace("${url}", getActualJdbcUrlTemplate(storageUnitName, Type.DOCKER == E2ETestEnvironment.getInstance().getRunEnvironment().getType()));
        proxyExecuteWithLog(registerStorageUnitTemplate, 0);
        int timeout = databaseType instanceof OpenGaussDatabaseType ? 60 : 10;
        Awaitility.await().ignoreExceptions().atMost(timeout, TimeUnit.SECONDS).pollInterval(3L, TimeUnit.SECONDS).until(() -> showStorageUnitsName().contains(storageUnitName));
    }
    
    /**
     * Show storage units names.
     *
     * @return storage units names
     */
    public List<String> showStorageUnitsName() {
        List<String> result = queryForListWithLog(proxyDataSource, "SHOW STORAGE UNITS").stream().map(each -> String.valueOf(each.get("name"))).collect(Collectors.toList());
        log.info("Show storage units name: {}", result);
        return result;
    }
    
    /**
     * Get actual JDBC URL template.
     *
     * @param databaseName database name
     * @param isInContainer is in container
     * @param storageContainerIndex storage container index
     * @return actual JDBC URL template
     */
    public String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer, final int storageContainerIndex) {
        StorageContainerOption option = DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType);
        if (Type.DOCKER == E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            DockerStorageContainer storageContainer = ((PipelineDockerContainerComposer) containerComposer).getStorageContainers().get(storageContainerIndex);
            String host;
            int port;
            if (isInContainer) {
                host = storageContainer.getNetworkAliases().get(0);
                port = storageContainer.getExposedPort();
            } else {
                host = storageContainer.getHost();
                port = storageContainer.getMappedPort();
            }
            return option.getConnectOption().getURL(host, port, storageContainer.getToBeConnectedDataSourceName(databaseName));
        }
        return option.getConnectOption().getURL("127.0.0.1", E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getPort(databaseType), databaseName);
    }
    
    /**
     * Get actual JDBC URL template.
     *
     * @param databaseName database name
     * @param isInContainer is in container
     * @return actual JDBC URL template
     */
    public String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer) {
        if (ProxyDatabaseTypeUtils.isOracleBranch(databaseType)) {
            return getActualJdbcUrlTemplate(databaseName, isInContainer, 0);
        }
        return appendExtraParameter(getActualJdbcUrlTemplate(databaseName, isInContainer, 0));
    }
    
    /**
     * Create schema.
     *
     * @param connection connection
     * @param seconds sleep seconds
     * @throws SQLException SQL exception
     */
    public void createSchema(final Connection connection, final int seconds) throws SQLException {
        if (!new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format("CREATE SCHEMA %s", SCHEMA_NAME));
        }
        sleepSeconds(seconds);
    }
    
    /**
     * Create source order table.
     *
     * @param sourceTableName source table name
     * @throws SQLException SQL exception
     */
    public void createSourceOrderTable(final String sourceTableName) throws SQLException {
        sourceExecuteWithLog(extraSQLCommand.getCreateTableOrder(sourceTableName));
    }
    
    /**
     * Create source table index list.
     *
     * @param schema schema
     * @param sourceTableName source table name
     * @throws SQLException SQL exception
     */
    public void createSourceTableIndexList(final String schema, final String sourceTableName) throws SQLException {
        if (databaseType instanceof PostgreSQLDatabaseType) {
            sourceExecuteWithLog(String.format("CREATE INDEX IF NOT EXISTS idx_user_id ON %s.%s ( user_id )", schema, sourceTableName));
        } else if (databaseType instanceof OpenGaussDatabaseType) {
            sourceExecuteWithLog(String.format("CREATE INDEX idx_user_id ON %s.%s ( user_id )", schema, sourceTableName));
        }
    }
    
    /**
     * Create source comment on list.
     *
     * @param schema schema
     * @param sourceTableName source table name
     * @throws SQLException SQL exception
     */
    public void createSourceCommentOnList(final String schema, final String sourceTableName) throws SQLException {
        sourceExecuteWithLog(String.format("COMMENT ON COLUMN %s.%s.user_id IS 'user id'", schema, sourceTableName));
    }
    
    /**
     * Create source order item table.
     *
     * @throws SQLException SQL exception
     */
    public void createSourceOrderItemTable() throws SQLException {
        sourceExecuteWithLog(extraSQLCommand.getCreateTableOrderItem());
    }
    
    /**
     * Source execute with log.
     *
     * @param sql SQL
     * @throws SQLException SQL exception
     */
    public void sourceExecuteWithLog(final String sql) throws SQLException {
        log.info("source execute :{}", sql);
        try (
                Connection connection = sourceDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    /**
     * Proxy execute with log.
     *
     * @param sql SQL
     * @param sleepSeconds sleep seconds
     * @throws SQLException SQL exception
     */
    public void proxyExecuteWithLog(final String sql, final int sleepSeconds) throws SQLException {
        log.info("proxy execute: {}", sql);
        proxyExecute(sql, sleepSeconds);
    }
    
    /**
     * Proxy execute.
     *
     * @param sql SQL
     * @param sleepSeconds sleep seconds
     * @throws SQLException SQL exception
     */
    public void proxyExecute(final String sql, final int sleepSeconds) throws SQLException {
        List<String> sqlList = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(sql);
        try (Connection connection = proxyDataSource.getConnection()) {
            for (String each : sqlList) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(each);
                }
            }
        }
        Awaitility.await().timeout(Duration.ofMinutes(1L)).pollDelay(Math.max(sleepSeconds, 0L), TimeUnit.SECONDS).until(() -> true);
    }
    
    /**
     * Wait job prepare success.
     *
     * @param distSQL dist SQL
     */
    public void waitJobPrepareSuccess(final String distSQL) {
        for (int i = 0; i < 5; i++) {
            List<Map<String, Object>> jobStatus = queryForListWithLog(distSQL);
            Set<String> statusSet = jobStatus.stream().map(each -> String.valueOf(each.get("status"))).collect(Collectors.toSet());
            if (statusSet.contains(JobStatus.PREPARING.name()) || statusSet.contains(JobStatus.RUNNING.name())) {
                sleepSeconds(2);
                continue;
            }
            break;
        }
    }
    
    /**
     * Wait job status reached.
     *
     * @param distSQL dist SQL
     * @param jobStatus job status
     * @param maxSleepSeconds max sleep seconds
     * @throws IllegalStateException if job status not reached
     */
    public void waitJobStatusReached(final String distSQL, final JobStatus jobStatus, final int maxSleepSeconds) {
        for (int i = 0, count = maxSleepSeconds / 2 + (0 == maxSleepSeconds % 2 ? 0 : 1); i < count; i++) {
            List<Map<String, Object>> resultList = queryForListWithLog(distSQL);
            log.info("Job status result: {}", resultList);
            Set<String> statusSet = resultList.stream().map(each -> String.valueOf(each.get("status"))).collect(Collectors.toSet());
            if (statusSet.stream().allMatch(each -> each.equals(jobStatus.name()))) {
                return;
            }
            sleepSeconds(2);
        }
        throw new IllegalStateException("Job status not reached: " + jobStatus);
    }
    
    /**
     * Query for list with log.
     *
     * @param sql SQL
     * @return query result
     * @throws RuntimeException runtime exception
     */
    public List<Map<String, Object>> queryForListWithLog(final String sql) {
        return queryForListWithLog(proxyDataSource, sql);
    }
    
    /**
     * Query for list with log.
     *
     * @param dataSource data source
     * @param sql SQL
     * @return query result
     * @throws RuntimeException runtime exception
     */
    public List<Map<String, Object>> queryForListWithLog(final DataSource dataSource, final String sql) {
        log.info("Query SQL: {}", sql);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return transformResultSetToList(statement.getResultSet());
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Transform result set to list.
     *
     * @param resultSet result set
     * @return transformed result
     * @throws SQLException SQL exception
     */
    public List<Map<String, Object>> transformResultSetToList(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        List<Map<String, Object>> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>(columnCount, 1F);
            for (int i = 1; i <= columnCount; i++) {
                row.put(resultSetMetaData.getColumnLabel(i).toLowerCase(), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
    
    /**
     * Start increment task.
     *
     * @param task increment task
     */
    public void startIncrementTask(final Runnable task) {
        increaseTaskThread = new Thread(task);
        increaseTaskThread.start();
    }
    
    /**
     * Assert order record exists in proxy.
     *
     * @param dataSource data source
     * @param tableName table name
     * @param orderId order id
     */
    public void assertOrderRecordExist(final DataSource dataSource, final String tableName, final Object orderId) {
        String sql;
        if (orderId instanceof String) {
            sql = String.format("SELECT 1 FROM %s WHERE order_id = '%s' AND user_id>0", tableName, orderId);
        } else {
            sql = String.format("SELECT 1 FROM %s WHERE order_id = %s AND user_id>0", tableName, orderId);
        }
        assertOrderRecordExist(dataSource, sql);
    }
    
    /**
     * Assert proxy order record exist.
     *
     * @param dataSource data source
     * @param sql SQL
     */
    public void assertOrderRecordExist(final DataSource dataSource, final String sql) {
        boolean recordExist = false;
        for (int i = 0; i < 5; i++) {
            List<Map<String, Object>> result = queryForListWithLog(dataSource, sql);
            recordExist = !result.isEmpty();
            if (recordExist) {
                break;
            }
            sleepSeconds(2);
        }
        assertTrue(recordExist, "Order record does not exist");
    }
    
    /**
     * Get target table records count.
     *
     * @param dataSource data source
     * @param tableName table name
     * @return target table records count
     */
    public int getTargetTableRecordsCount(final DataSource dataSource, final String tableName) {
        List<Map<String, Object>> targetList = queryForListWithLog(dataSource, "SELECT COUNT(1) AS count FROM " + tableName);
        assertFalse(targetList.isEmpty());
        return ((Number) targetList.get(0).get("count")).intValue();
    }
    
    /**
     * Assert greater than order table init rows.
     *
     * @param dataSource data source
     * @param tableInitRows table init rows
     * @param schema schema
     */
    public void assertGreaterThanOrderTableInitRows(final DataSource dataSource, final int tableInitRows, final String schema) {
        String tableName = Strings.isNullOrEmpty(schema) ? "t_order" : String.format("%s.t_order", schema);
        int recordsCount = getTargetTableRecordsCount(dataSource, tableName);
        assertTrue(recordsCount > tableInitRows, "actual count " + recordsCount);
    }
    
    /**
     * Generate ShardingSphere data source from proxy.
     *
     * @return ShardingSphere data source
     */
    // TODO proxy support for some fields still needs to be optimized, such as binary of MySQL, after these problems are optimized, Proxy dataSource can be used.
    public DataSource generateShardingSphereDataSourceFromProxy() {
        Awaitility.await().atMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> null != getYamlRootConfig().getRules());
        YamlRootConfiguration rootConfig = getYamlRootConfig();
        ShardingSpherePreconditions.checkNotNull(rootConfig.getDataSources(), () -> new IllegalStateException("dataSources is null"));
        ShardingSpherePreconditions.checkNotNull(rootConfig.getRules(), () -> new IllegalStateException("rules is null"));
        if (Type.DOCKER == E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            DockerStorageContainer storageContainer = ((PipelineDockerContainerComposer) containerComposer).getStorageContainers().get(0);
            String sourceUrl = String.join(":", storageContainer.getNetworkAliases().get(0), Integer.toString(storageContainer.getExposedPort()));
            String targetUrl = String.join(":", storageContainer.getHost(), Integer.toString(storageContainer.getMappedPort()));
            for (Map<String, Object> each : rootConfig.getDataSources().values()) {
                each.put("url", each.get("url").toString().replaceFirst(sourceUrl, targetUrl));
            }
        }
        for (Map<String, Object> each : rootConfig.getDataSources().values()) {
            each.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        }
        YamlSingleRuleConfiguration singleRuleConfig = new YamlSingleRuleConfiguration();
        singleRuleConfig.setTables(Collections.singletonList("*.*"));
        rootConfig.getRules().add(singleRuleConfig);
        return new PipelineDataSource(new ShardingSpherePipelineDataSourceConfiguration(rootConfig));
    }
    
    private YamlRootConfiguration getYamlRootConfig() {
        return YamlEngine.unmarshal(queryForListWithLog("EXPORT DATABASE CONFIGURATION").get(0).get("result").toString(), YamlRootConfiguration.class);
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
