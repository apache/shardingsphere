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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ScalingITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.NativeComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.integration.data.pipeline.framework.watcher.ScalingWatcher;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.junit.Rule;
import org.opengauss.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
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
    
    protected static final String DS_1 = "scaling_it_1";
    
    protected static final String DS_2 = "scaling_it_2";
    
    protected static final String DS_3 = "scaling_it_3";
    
    protected static final String DS_4 = "scaling_it_4";
    
    protected static final Executor SCALING_EXECUTOR = Executors.newFixedThreadPool(5);
    
    protected static final int TABLE_INIT_ROW_COUNT = 3000;
    
    @Rule
    @Getter(AccessLevel.NONE)
    public ScalingWatcher scalingWatcher;
    
    private final BaseComposedContainer composedContainer;
    
    private final CommonSQLCommand commonSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private JdbcTemplate jdbcTemplate;
    
    @Setter
    private Thread increaseTaskThread;
    
    public BaseITCase(final ScalingParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.DOCKER) {
            composedContainer = new DockerComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        } else {
            composedContainer = new NativeComposedContainer(parameterized.getDatabaseType());
        }
        composedContainer.start();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerComposedContainer) composedContainer).getStorageContainer();
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
        commonSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
        scalingWatcher = new ScalingWatcher(composedContainer, jdbcTemplate);
    }
    
    private void cleanUpDataSource() {
        for (String each : Arrays.asList(DS_0, DS_1, DS_2, DS_3, DS_4)) {
            composedContainer.cleanUpDatabase(each);
        }
    }
    
    protected void createProxyDatabase(final DatabaseType databaseType) {
        String defaultDatabaseName = "";
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            defaultDatabaseName = "postgres";
        }
        String jdbcUrl = composedContainer.getProxyJdbcUrl(defaultDatabaseName);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "proxy", "Proxy@123")) {
            if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NATIVE) {
                try {
                    executeWithLog(connection, "DROP DATABASE sharding_db");
                } catch (final SQLException ex) {
                    log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
                }
            }
            executeWithLog(connection, "CREATE DATABASE sharding_db");
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        jdbcTemplate = new JdbcTemplate(getProxyDataSource("sharding_db"));
    }
    
    protected DataSource getProxyDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(getDatabaseType()));
        result.setJdbcUrl(composedContainer.getProxyJdbcUrl(databaseName));
        result.setUsername("proxy");
        result.setPassword("Proxy@123");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    protected boolean waitShardingAlgorithmEffect(final int maxWaitTimes) {
        long startTime = System.currentTimeMillis();
        int waitTimes = 0;
        do {
            List<Map<String, Object>> result = queryForListWithLog("SHOW SHARDING ALGORITHMS");
            if (result.size() >= 3) {
                log.info("waitShardingAlgorithmEffect time consume: {}", System.currentTimeMillis() - startTime);
                return true;
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            waitTimes++;
        } while (waitTimes <= maxWaitTimes);
        return false;
    }
    
    @SneakyThrows
    protected void addSourceResource() {
        // TODO if mysql can append database firstly, they can be combined
        if (databaseType instanceof MySQLDatabaseType) {
            try (Connection connection = DriverManager.getConnection(getComposedContainer().getProxyJdbcUrl(""), "proxy", "Proxy@123")) {
                connection.createStatement().execute("USE sharding_db");
                addSourceResource0(connection);
            }
        } else {
            try (Connection connection = DriverManager.getConnection(getComposedContainer().getProxyJdbcUrl("sharding_db"), "proxy", "Proxy@123")) {
                addSourceResource0(connection);
            }
        }
        List<Map<String, Object>> resources = queryForListWithLog("SHOW DATABASE RESOURCES FROM sharding_db");
        assertThat(resources.size(), is(2));
    }
    
    private void addSourceResource0(final Connection connection) throws SQLException {
        String addSourceResource = commonSQLCommand.getSourceAddResourceTemplate().replace("${user}", username)
                .replace("${password}", password)
                .replace("${ds0}", getActualJdbcUrlTemplate(DS_0))
                .replace("${ds1}", getActualJdbcUrlTemplate(DS_1));
        executeWithLog(connection, addSourceResource);
    }
    
    @SneakyThrows
    protected void addTargetResource() {
        String addTargetResource = commonSQLCommand.getTargetAddResourceTemplate().replace("${user}", username)
                .replace("${password}", password)
                .replace("${ds2}", getActualJdbcUrlTemplate(DS_2))
                .replace("${ds3}", getActualJdbcUrlTemplate(DS_3))
                .replace("${ds4}", getActualJdbcUrlTemplate(DS_4));
        executeWithLog(addTargetResource);
        List<Map<String, Object>> resources = queryForListWithLog("SHOW DATABASE RESOURCES from sharding_db");
        assertThat(resources.size(), is(5));
        assertBeforeApplyScalingMetadataCorrectly();
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName) {
        if (ScalingITEnvTypeEnum.DOCKER == ENV.getItEnvType()) {
            DockerStorageContainer storageContainer = ((DockerComposedContainer) composedContainer).getStorageContainer();
            return DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getPort(), databaseName);
        }
        return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    protected void initShardingAlgorithm() {
        executeWithLog(getCommonSQLCommand().getCreateDatabaseShardingAlgorithm());
        executeWithLog(getCommonSQLCommand().getCreateOrderShardingAlgorithm());
        executeWithLog(getCommonSQLCommand().getCreateOrderItemShardingAlgorithm());
    }
    
    protected void createOrderTableRule() {
        executeWithLog(commonSQLCommand.getCreateOrderTableRule());
    }
    
    protected void createOrderItemTableRule() {
        executeWithLog(commonSQLCommand.getCreateOrderItemTableRule());
    }
    
    protected void bindingShardingRule() {
        executeWithLog("CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item)");
    }
    
    protected void createScalingRule() {
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NATIVE) {
            try {
                List<Map<String, Object>> scalingList = jdbcTemplate.queryForList("SHOW SCALING LIST");
                for (Map<String, Object> each : scalingList) {
                    String id = each.get("id").toString();
                    executeWithLog(String.format("CLEAN SCALING '%s'", id), 0);
                }
            } catch (final DataAccessException ex) {
                log.error("Failed to show scaling list. {}", ex.getMessage());
            }
        }
        executeWithLog("CREATE SHARDING SCALING RULE scaling_manual (INPUT(SHARDING_SIZE=1000), DATA_CONSISTENCY_CHECKER(TYPE(NAME='DATA_MATCH')))");
    }
    
    protected void createSchema(final String schemaName) {
        if (DatabaseTypeUtil.isPostgreSQL(databaseType)) {
            executeWithLog(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
            return;
        }
        if (DatabaseTypeUtil.isOpenGauss(databaseType)) {
            try {
                executeWithLog(String.format("CREATE SCHEMA %s", schemaName));
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
    
    protected void executeWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("connection execute:{}", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
    
    private void executeWithLog(final String sql, final int sleepSeconds) {
        log.info("jdbcTemplate execute:{}", sql);
        jdbcTemplate.execute(sql);
        ThreadUtil.sleep(Math.max(sleepSeconds, 0), TimeUnit.SECONDS);
    }
    
    protected void executeWithLog(final String sql) {
        executeWithLog(sql, 2);
    }
    
    protected List<Map<String, Object>> queryForListWithLog(final String sql) {
        int retryNumber = 0;
        while (retryNumber <= 3) {
            try {
                return jdbcTemplate.queryForList(sql);
            } catch (final DataAccessException ex) {
                log.error("data access error", ex);
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            retryNumber++;
        }
        throw new RuntimeException("can't get result from proxy");
    }
    
    protected void startIncrementTask(final BaseIncrementTask baseIncrementTask) {
        setIncreaseTaskThread(new Thread(baseIncrementTask));
        getIncreaseTaskThread().start();
    }
    
    protected void stopScalingSourceWriting(final String jobId) {
        executeWithLog(String.format("STOP SCALING SOURCE WRITING '%s'", jobId));
    }
    
    protected void stopScaling(final String jobId) {
        executeWithLog(String.format("STOP SCALING '%s'", jobId), 5);
    }
    
    protected void startScaling(final String jobId) {
        executeWithLog(String.format("START SCALING '%s'", jobId), 10);
    }
    
    protected void applyScaling(final String jobId) {
        assertBeforeApplyScalingMetadataCorrectly();
        executeWithLog(String.format("APPLY SCALING '%s'", jobId));
    }
    
    protected void assertBeforeApplyScalingMetadataCorrectly() {
        List<Map<String, Object>> previewResults = queryForListWithLog("PREVIEW SELECT COUNT(1) FROM t_order");
        assertThat("data_source_name name not correct, it's effective early, search watcher failed get more info",
                previewResults.stream().map(each -> each.get("data_source_name")).collect(Collectors.toSet()), is(new HashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    protected String getScalingJobId() {
        List<Map<String, Object>> scalingListMap = queryForListWithLog("SHOW SCALING LIST");
        String jobId = scalingListMap.get(0).get("id").toString();
        log.info("jobId: {}", jobId);
        return jobId;
    }
    
    protected void waitScalingFinished(final String jobId) throws InterruptedException {
        if (null != increaseTaskThread) {
            TimeUnit.SECONDS.timedJoin(increaseTaskThread, 60);
        }
        log.info("jobId: {}", jobId);
        Set<String> actualStatus = null;
        for (int i = 0; i < 20; i++) {
            List<Map<String, Object>> showScalingStatusResult = showScalingStatus(jobId);
            log.info("show scaling status result: {}", showScalingStatusResult);
            actualStatus = showScalingStatusResult.stream().map(each -> each.get("status").toString()).collect(Collectors.toSet());
            assertFalse(CollectionUtils.containsAny(actualStatus, Arrays.asList(JobStatus.PREPARING_FAILURE.name(), JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name(),
                    JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name())));
            if (actualStatus.size() == 1 && actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                break;
            } else if (actualStatus.size() >= 1 && actualStatus.containsAll(new HashSet<>(Arrays.asList("", JobStatus.EXECUTE_INCREMENTAL_TASK.name())))) {
                log.warn("one of the shardingItem was not started correctly");
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
        }
        assertThat(actualStatus, is(Collections.singleton(JobStatus.EXECUTE_INCREMENTAL_TASK.name())));
    }
    
    protected void assertGreaterThanInitTableInitRows(final int tableInitRows, final String schema) {
        String countSQL = StringUtils.isBlank(schema) ? "SELECT COUNT(*) as count FROM t_order" : String.format("SELECT COUNT(*) as count FROM %s.t_order", schema);
        Map<String, Object> actual = jdbcTemplate.queryForMap(countSQL);
        assertTrue("actual count " + actual.get("count"), Integer.parseInt(actual.get("count").toString()) > tableInitRows);
    }
    
    protected List<Map<String, Object>> showScalingStatus(final String jobId) {
        return queryForListWithLog(String.format("SHOW SCALING STATUS '%s'", jobId));
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
        stopScalingSourceWriting(jobId);
        List<Map<String, Object>> checkScalingResults = queryForListWithLog(String.format("CHECK SCALING '%s' BY TYPE (NAME='DATA_MATCH')", jobId));
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
            if (incrementalIdleSeconds <= 3) {
                return false;
            }
        }
        return true;
    }
    
    protected void assertPreviewTableSuccess(final String tableName, final List<String> expect) {
        List<Map<String, Object>> actualResults = queryForListWithLog(String.format("PREVIEW SELECT COUNT(1) FROM %s", tableName));
        List<String> dataSourceNames = actualResults.stream().map(each -> String.valueOf(each.get("data_source_name"))).sorted().collect(Collectors.toList());
        Collections.sort(expect);
        assertThat(dataSourceNames, is(expect));
    }
}
