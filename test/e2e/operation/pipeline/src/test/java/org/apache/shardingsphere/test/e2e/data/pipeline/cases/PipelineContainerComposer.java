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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.api.job.type.CDCJobType;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.infra.database.core.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.command.ExtraSQLCommand;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
    
    public static final String DS_0 = "pipeline_it_0";
    
    public static final String DS_1 = "pipeline_it_1";
    
    public static final String DS_2 = "pipeline_it_2";
    
    public static final String DS_3 = "pipeline_it_3";
    
    public static final String DS_4 = "pipeline_it_4";
    
    public static final int TABLE_INIT_ROW_COUNT = 3000;
    
    private static final String PROXY_DATABASE = "sharding_db";
    
    private final BaseContainerComposer containerComposer;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private final DataSource sourceDataSource;
    
    private final DataSource proxyDataSource;
    
    private Thread increaseTaskThread;
    
    public PipelineContainerComposer(final PipelineTestParameter testParam, final JobType jobType) {
        databaseType = testParam.getDatabaseType();
        containerComposer = PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.DOCKER
                ? new DockerContainerComposer(testParam.getDatabaseType(), testParam.getStorageContainerImage(), testParam.getStorageContainerCount())
                : new NativeContainerComposer(testParam.getDatabaseType());
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainers().get(0);
            username = storageContainer.getUsername();
            password = storageContainer.getPassword();
        } else {
            username = PipelineE2EEnvironment.getInstance().getActualDataSourceUsername(databaseType);
            password = PipelineE2EEnvironment.getInstance().getActualDataSourcePassword(databaseType);
        }
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(testParam.getScenario())), ExtraSQLCommand.class);
        containerComposer.start();
        sourceDataSource = StorageContainerUtils.generateDataSource(getActualJdbcUrlTemplate(DS_0, false), username, password);
        proxyDataSource = StorageContainerUtils.generateDataSource(
                appendExtraParameter(containerComposer.getProxyJdbcUrl(PROXY_DATABASE)), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD);
        init(jobType);
    }
    
    @SneakyThrows(SQLException.class)
    private void init(final JobType jobType) {
        String jdbcUrl = containerComposer.getProxyJdbcUrl(databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType ? "postgres" : "");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD)) {
            cleanUpPipelineJobs(connection, jobType);
            cleanUpProxyDatabase(connection);
            createProxyDatabase(connection);
        }
        cleanUpDataSource();
    }
    
    private void cleanUpPipelineJobs(final Connection connection, final JobType jobType) throws SQLException {
        if (PipelineEnvTypeEnum.NATIVE != PipelineE2EEnvironment.getInstance().getItEnvType()) {
            return;
        }
        String jobTypeName = jobType.getType();
        for (Map<String, Object> each : queryJobs(connection, jobTypeName)) {
            String jobId = each.get("id").toString();
            Map<String, Object> jobInfo = queryForListWithLog(String.format("SHOW %s STATUS '%s'", jobTypeName, jobId)).get(0);
            String status = jobInfo.get("status").toString();
            if (JobStatus.FINISHED.name().equals(status)) {
                connection.createStatement().execute(String.format((jobType instanceof CDCJobType ? "DROP" : "COMMIT") + " %s '%s'", jobTypeName, jobId));
            } else {
                connection.createStatement().execute(String.format((jobType instanceof CDCJobType ? "DROP" : "ROLLBACK") + " %s '%s'", jobTypeName, jobId));
            }
        }
    }
    
    private List<Map<String, Object>> queryJobs(final Connection connection, final String jobTypeName) {
        try {
            return transformResultSetToList(connection.createStatement().executeQuery(String.format("SHOW %s LIST", jobTypeName)));
        } catch (final SQLException ex) {
            log.warn("{} execute failed, message {}", String.format("SHOW %s LIST", jobTypeName), ex.getMessage());
            return Collections.emptyList();
        }
    }
    
    private void cleanUpProxyDatabase(final Connection connection) {
        if (PipelineEnvTypeEnum.NATIVE != PipelineE2EEnvironment.getInstance().getItEnvType()) {
            return;
        }
        try {
            connection.createStatement().execute(String.format("DROP DATABASE IF EXISTS %s", PROXY_DATABASE));
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
        } catch (final SQLException ex) {
            log.warn("Drop proxy database failed, error={}", ex.getMessage());
        }
    }
    
    private void createProxyDatabase(final Connection connection) throws SQLException {
        String sql = String.format("CREATE DATABASE %s", PROXY_DATABASE);
        log.info("Create proxy database {}", PROXY_DATABASE);
        connection.createStatement().execute(sql);
        Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void cleanUpDataSource() {
        if (PipelineEnvTypeEnum.NATIVE != PipelineE2EEnvironment.getInstance().getItEnvType()) {
            return;
        }
        for (String each : Arrays.asList(DS_0, DS_1, DS_2, DS_3, DS_4)) {
            containerComposer.cleanUpDatabase(each);
        }
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
        String registerStorageUnitTemplate = "REGISTER STORAGE UNIT ${ds} ( URL='${url}', USER='${user}', PASSWORD='${password}')".replace("${ds}", storageUnitName)
                .replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${url}", getActualJdbcUrlTemplate(storageUnitName, true));
        proxyExecuteWithLog(registerStorageUnitTemplate, 1);
        Awaitility.await().ignoreExceptions().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> showStorageUnitsName().contains(storageUnitName));
    }
    
    /**
     * Add resource.
     *
     * @param distSQL dist SQL
     * @throws SQLException SQL exception
     */
    // TODO Use registerStorageUnit instead, and remove the method, keep it now
    public void addResource(final String distSQL) throws SQLException {
        proxyExecuteWithLog(distSQL, 2);
    }
    
    /**
     * Show storage units names.
     *
     * @return storage units names
     */
    public List<String> showStorageUnitsName() {
        return queryForListWithLog(proxyDataSource, "SHOW STORAGE UNITS").stream().map(each -> String.valueOf(each.get("name"))).collect(Collectors.toList());
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
        if (PipelineEnvTypeEnum.DOCKER == PipelineE2EEnvironment.getInstance().getItEnvType()) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainers().get(storageContainerIndex);
            return isInContainer
                    ? DataSourceEnvironment.getURL(databaseType, storageContainer.getNetworkAliases().get(0), storageContainer.getExposedPort(), databaseName)
                    : storageContainer.getJdbcUrl(databaseName);
        }
        return DataSourceEnvironment.getURL(databaseType, "127.0.0.1", PipelineE2EEnvironment.getInstance().getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    /**
     * Get actual JDBC URL template.
     *
     * @param databaseName database name
     * @param isInContainer is in container
     * @return actual JDBC URL template
     */
    public String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer) {
        return appendExtraParameter(getActualJdbcUrlTemplate(databaseName, isInContainer, 0));
    }
    
    /**
     * Create schema.
     *
     * @param connection connection
     * @param sleepSeconds sleep seconds
     * @throws SQLException SQL exception
     */
    public void createSchema(final Connection connection, final int sleepSeconds) throws SQLException {
        if (!databaseType.isSchemaAvailable()) {
            return;
        }
        connection.createStatement().execute(String.format("CREATE SCHEMA %s", SCHEMA_NAME));
        if (sleepSeconds > 0) {
            Awaitility.await().pollDelay(sleepSeconds, TimeUnit.SECONDS).until(() -> true);
        }
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
        try (Connection connection = sourceDataSource.getConnection()) {
            connection.createStatement().execute(sql);
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
        log.info("proxy execute :{}", sql);
        try (Connection connection = proxyDataSource.getConnection()) {
            connection.createStatement().execute(sql);
        }
        Awaitility.await().pollDelay(Math.max(sleepSeconds, 0L), TimeUnit.SECONDS).until(() -> true);
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
                Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
            }
        }
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
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            return transformResultSetToList(resultSet);
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
        int columns = resultSetMetaData.getColumnCount();
        List<Map<String, Object>> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columns; i++) {
                row.put(resultSetMetaData.getColumnLabel(i).toLowerCase(), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
    
    /**
     * Start increment task.
     *
     * @param baseIncrementTask base increment task
     */
    public void startIncrementTask(final BaseIncrementTask baseIncrementTask) {
        increaseTaskThread = new Thread(baseIncrementTask);
        increaseTaskThread.start();
    }
    
    /**
     * Wait increment task finished.
     *
     * @param distSQL dist SQL
     * @return result
     */
    // TODO use DAO to query via DistSQL
    public List<Map<String, Object>> waitIncrementTaskFinished(final String distSQL) {
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> listJobStatus = queryForListWithLog(distSQL);
            log.info("show status result: {}", listJobStatus);
            Set<String> actualStatus = new HashSet<>();
            Collection<Integer> incrementalIdleSecondsList = new LinkedList<>();
            for (Map<String, Object> each : listJobStatus) {
                assertTrue(Strings.isNullOrEmpty((String) each.get("error_message")), "error_message: `" + each.get("error_message") + "`");
                actualStatus.add(each.get("status").toString());
                String incrementalIdleSeconds = each.get("incremental_idle_seconds").toString();
                incrementalIdleSecondsList.add(Strings.isNullOrEmpty(incrementalIdleSeconds) ? 0 : Integer.parseInt(incrementalIdleSeconds));
            }
            if (Collections.min(incrementalIdleSecondsList) <= 5) {
                Awaitility.await().pollDelay(3L, TimeUnit.SECONDS).until(() -> true);
                continue;
            }
            if (actualStatus.size() == 1 && actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                return listJobStatus;
            }
        }
        return Collections.emptyList();
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
            sql = String.format("SELECT 1 FROM %s WHERE order_id = '%s'", tableName, orderId);
        } else {
            sql = String.format("SELECT 1 FROM %s WHERE order_id = %s", tableName, orderId);
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
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
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
        Awaitility.await().atMost(5L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> null != getYamlRootConfig().getRules());
        YamlRootConfiguration rootConfig = getYamlRootConfig();
        ShardingSpherePreconditions.checkNotNull(rootConfig.getDataSources(), () -> new IllegalStateException("dataSources is null"));
        ShardingSpherePreconditions.checkNotNull(rootConfig.getRules(), () -> new IllegalStateException("rules is null"));
        if (PipelineEnvTypeEnum.DOCKER == PipelineE2EEnvironment.getInstance().getItEnvType()) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainers().get(0);
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
        return PipelineDataSourceFactory.newInstance(new ShardingSpherePipelineDataSourceConfiguration(rootConfig));
    }
    
    private YamlRootConfiguration getYamlRootConfig() {
        return YamlEngine.unmarshal(queryForListWithLog("EXPORT DATABASE CONFIGURATION").get(0).get("result").toString(), YamlRootConfiguration.class);
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
