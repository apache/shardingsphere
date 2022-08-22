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

package org.apache.shardingsphere.integration.data.pipeline.cases.base;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.MigrationDistSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ScalingITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.MigrationComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.NativeComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.integration.data.pipeline.framework.watcher.ScalingWatcher;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.junit.Rule;
import org.opengauss.util.PSQLException;
import org.springframework.jdbc.BadSqlGrammarException;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    protected static final String DS_0 = "scaling_it_0";
    
    protected static final String DS_2 = "scaling_it_2";
    
    protected static final String DS_3 = "scaling_it_3";
    
    protected static final String DS_4 = "scaling_it_4";
    
    protected static final Executor SCALING_EXECUTOR = Executors.newFixedThreadPool(5);
    
    protected static final int TABLE_INIT_ROW_COUNT = 3000;
    
    @Rule
    @Getter(AccessLevel.NONE)
    public ScalingWatcher scalingWatcher;
    
    private final BaseComposedContainer composedContainer;
    
    private final MigrationDistSQLCommand migrationDistSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private DataSource sourceDataSource;
    
    private DataSource proxyDataSource;
    
    @Setter
    private Thread increaseTaskThread;
    
    public BaseITCase(final ScalingParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.DOCKER) {
            composedContainer = new MigrationComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        } else {
            composedContainer = new NativeComposedContainer(parameterized.getDatabaseType());
        }
        composedContainer.start();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((MigrationComposedContainer) composedContainer).getStorageContainer();
            username = storageContainer.getUsername();
            password = storageContainer.getUnifiedPassword();
        } else {
            username = ENV.getActualDataSourceUsername(databaseType);
            password = ENV.getActualDataSourcePassword(databaseType);
        }
        createProxyDatabase(parameterized.getDatabaseType());
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NATIVE) {
            cleanUpDataSource();
        }
        migrationDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), MigrationDistSQLCommand.class);
        scalingWatcher = new ScalingWatcher(composedContainer);
    }
    
    private void cleanUpDataSource() {
        for (String each : Arrays.asList(DS_0, DS_2, DS_3, DS_4)) {
            composedContainer.cleanUpDatabase(each);
        }
    }
    
    protected void createProxyDatabase(final DatabaseType databaseType) {
        String defaultDatabaseName = "";
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            defaultDatabaseName = "postgres";
        }
        String jdbcUrl = composedContainer.getProxyJdbcUrl(defaultDatabaseName);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD)) {
            if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NATIVE) {
                try {
                    connectionExecuteWithLog(connection, "DROP DATABASE sharding_db");
                } catch (final SQLException ex) {
                    log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
                }
            }
            connectionExecuteWithLog(connection, "CREATE DATABASE sharding_db");
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        sourceDataSource = getDataSource(getActualJdbcUrlTemplate(DS_0, false), username, password);
        proxyDataSource = getDataSource(composedContainer.getProxyJdbcUrl("sharding_db"), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD);
    }
    
    private DataSource getDataSource(final String jdbcUrl, final String username, final String password) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(getDatabaseType()));
        result.setJdbcUrl(jdbcUrl);
        result.setUsername(username);
        result.setPassword(password);
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    protected void addSourceResource() {
        try (Connection connection = DriverManager.getConnection(getComposedContainer().getProxyJdbcUrl("sharding_db"), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD)) {
            addSourceResource0(connection);
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void addSourceResource0(final Connection connection) {
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NATIVE) {
            try {
                connectionExecuteWithLog(connection, "DROP MIGRATION SOURCE RESOURCE ds_0");
            } catch (final SQLException ex) {
                log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        String addSourceResource = migrationDistSQLCommand.getAddMigrationSourceResourceTemplate().replace("${user}", username)
                .replace("${password}", password)
                .replace("${ds0}", getActualJdbcUrlTemplate(DS_0, true));
        connectionExecuteWithLog(connection, addSourceResource);
    }
    
    @SneakyThrows
    protected void addTargetResource() {
        String addTargetResource = migrationDistSQLCommand.getAddMigrationTargetResourceTemplate().replace("${user}", username)
                .replace("${password}", password)
                .replace("${ds2}", getActualJdbcUrlTemplate(DS_2, true))
                .replace("${ds3}", getActualJdbcUrlTemplate(DS_3, true))
                .replace("${ds4}", getActualJdbcUrlTemplate(DS_4, true));
        proxyExecuteWithLog(addTargetResource, 2);
        List<Map<String, Object>> resources = queryForListWithLog("SHOW DATABASE RESOURCES from sharding_db");
        assertThat(resources.size(), is(3));
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer) {
        if (ScalingITEnvTypeEnum.DOCKER == ENV.getItEnvType()) {
            DockerStorageContainer storageContainer = ((MigrationComposedContainer) composedContainer).getStorageContainer();
            if (isInContainer) {
                return DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getPort(), databaseName);
            } else {
                return DataSourceEnvironment.getURL(getDatabaseType(), storageContainer.getHost(), storageContainer.getFirstMappedPort(), databaseName);
            }
        }
        return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    protected void createTargetOrderTableRule() {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableRule(), 3);
    }
    
    protected void createTargetOrderItemTableRule() {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderItemTableRule(), 3);
    }
    
    protected void startMigrationOrder() {
        proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderSingleTable(), 5);
    }
    
    protected void startMigrationOrderItem() {
        proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderItemSingleTable(), 5);
    }
    
    // TODO use new DistSQL
    protected void createScalingRule() {
    }
    
    protected void createSourceSchema(final String schemaName) {
        if (DatabaseTypeUtil.isPostgreSQL(databaseType)) {
            sourceExecuteWithLog(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
            return;
        }
        if (DatabaseTypeUtil.isOpenGauss(databaseType)) {
            try {
                sourceExecuteWithLog(String.format("CREATE SCHEMA %s", schemaName));
            } catch (final BadSqlGrammarException ex) {
                // only used for native mode.
                if (ex.getCause() instanceof PSQLException && "42P06".equals(((PSQLException) ex.getCause()).getSQLState())) {
                    log.info("Schema {} already exists.", schemaName);
                } else {
                    throw ex;
                }
            }
        }
    }
    
    @SneakyThrows(SQLException.class)
    protected void sourceExecuteWithLog(final String sql) {
        log.info("source execute :{}", sql);
        try (Connection connection = sourceDataSource.getConnection()) {
            connection.createStatement().execute(sql);
        }
    }
    
    @SneakyThrows(SQLException.class)
    protected void proxyExecuteWithLog(final String sql, final int sleepSeconds) {
        log.info("proxy execute :{}", sql);
        try (Connection connection = proxyDataSource.getConnection()) {
            connection.createStatement().execute(sql);
        }
        ThreadUtil.sleep(Math.max(sleepSeconds, 0), TimeUnit.SECONDS);
    }
    
    protected void connectionExecuteWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("connection execute:{}", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(2, TimeUnit.SECONDS);
    }
    
    protected List<Map<String, Object>> queryForListWithLog(final String sql) {
        int retryNumber = 0;
        while (retryNumber <= 3) {
            try (Connection connection = proxyDataSource.getConnection()) {
                ResultSet resultSet = connection.createStatement().executeQuery(sql);
                return resultSetToList(resultSet);
            } catch (final SQLException ex) {
                log.error("data access error", ex);
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
            retryNumber++;
        }
        throw new RuntimeException("can't get result from proxy");
    }
    
    protected List<Map<String, Object>> resultSetToList(final ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> results = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columns; i++) {
                row.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
    
    protected void startIncrementTask(final BaseIncrementTask baseIncrementTask) {
        setIncreaseTaskThread(new Thread(baseIncrementTask));
        getIncreaseTaskThread().start();
    }
    
    protected void stopMigration(final String jobId) {
        proxyExecuteWithLog(String.format("STOP MIGRATION '%s'", jobId), 5);
    }
    
    // TODO reopen later
    protected void startMigrationByJob(final String jobId) {
        proxyExecuteWithLog(String.format("START MIGRATION '%s'", jobId), 10);
    }
    
    protected List<String> listJobId() {
        List<Map<String, Object>> jobList = queryForListWithLog("SHOW MIGRATION LIST");
        return jobList.stream().map(a -> a.get("id").toString()).collect(Collectors.toList());
    }
    
    protected String getJobIdByTableName(final String tableName) {
        List<Map<String, Object>> jobList = queryForListWithLog("SHOW MIGRATION LIST");
        return jobList.stream().filter(a -> a.get("tables").toString().equals(tableName)).findFirst().orElseThrow(() -> new RuntimeException("not find target table")).get("id").toString();
    }
    
    @SneakyThrows(InterruptedException.class)
    protected void waitMigrationFinished(final String jobId) {
        if (null != increaseTaskThread) {
            TimeUnit.SECONDS.timedJoin(increaseTaskThread, 60);
        }
        log.info("jobId: {}", jobId);
        Set<String> actualStatus;
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> showScalingStatusResult = showScalingStatus(jobId);
            log.info("show migration status result: {}", showScalingStatusResult);
            actualStatus = showScalingStatusResult.stream().map(each -> each.get("status").toString()).collect(Collectors.toSet());
            assertFalse(CollectionUtils.containsAny(actualStatus, Arrays.asList(JobStatus.PREPARING_FAILURE.name(), JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name(),
                    JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name())));
            if (actualStatus.size() == 1 && actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                break;
            } else if (actualStatus.size() >= 1 && actualStatus.containsAll(new HashSet<>(Arrays.asList("", JobStatus.EXECUTE_INCREMENTAL_TASK.name())))) {
                log.warn("one of the shardingItem was not started correctly");
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
        }
    }
    
    protected void assertGreaterThanInitTableInitRows(final int tableInitRows, final String schema) {
        String countSQL = StringUtils.isBlank(schema) ? "SELECT COUNT(*) as count FROM t_order" : String.format("SELECT COUNT(*) as count FROM %s.t_order", schema);
        Map<String, Object> actual = queryForListWithLog(countSQL).get(0);
        assertTrue("actual count " + actual.get("count"), Integer.parseInt(actual.get("count").toString()) > tableInitRows);
    }
    
    protected List<Map<String, Object>> showScalingStatus(final String jobId) {
        return queryForListWithLog(String.format("SHOW MIGRATION STATUS '%s'", jobId));
    }
    
    protected void assertCheckScalingSuccess(final String jobId) {
        for (int i = 0; i < 10; i++) {
            if (checkJobIncrementTaskFinished(jobId)) {
                break;
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
        }
        boolean secondCheckJobResult = checkJobIncrementTaskFinished(jobId);
        log.info("second check job result: {}", secondCheckJobResult);
        proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        List<Map<String, Object>> checkScalingResults = queryForListWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='DATA_MATCH')", jobId));
        log.info("checkScalingResults: {}", checkScalingResults);
        for (Map<String, Object> entry : checkScalingResults) {
            assertTrue(Boolean.parseBoolean(entry.get("records_content_matched").toString()));
        }
    }
    
    private boolean checkJobIncrementTaskFinished(final String jobId) {
        List<Map<String, Object>> listScalingStatus = showScalingStatus(jobId);
        log.info("listScalingStatus result: {}", listScalingStatus);
        for (Map<String, Object> entry : listScalingStatus) {
            if (JobStatus.EXECUTE_INCREMENTAL_TASK.name().equalsIgnoreCase(entry.get("status").toString())) {
                return false;
            }
            int incrementalIdleSeconds = Integer.parseInt(entry.get("incremental_idle_seconds").toString());
            if (incrementalIdleSeconds < 10) {
                return false;
            }
        }
        return true;
    }
}
