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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.base;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.data.pipeline.command.ExtraSQLCommand;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.container.compose.NativeContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.watcher.PipelineWatcher;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtil;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Rule;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Getter(AccessLevel.PROTECTED)
@Slf4j
public abstract class PipelineBaseE2EIT {
    
    protected static final PipelineE2EEnvironment ENV = PipelineE2EEnvironment.getInstance();
    
    protected static final String SCHEMA_NAME = "test";
    
    protected static final String PROXY_DATABASE = "sharding_db";
    
    protected static final String DS_0 = "pipeline_it_0";
    
    protected static final String DS_1 = "pipeline_it_1";
    
    protected static final String DS_2 = "pipeline_it_2";
    
    protected static final String DS_3 = "pipeline_it_3";
    
    protected static final String DS_4 = "pipeline_it_4";
    
    protected static final int TABLE_INIT_ROW_COUNT = 3000;
    
    @Rule
    @Getter(AccessLevel.NONE)
    public PipelineWatcher pipelineWatcher;
    
    private final BaseContainerComposer containerComposer;
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private DataSource sourceDataSource;
    
    private DataSource proxyDataSource;
    
    private Thread increaseTaskThread;
    
    public PipelineBaseE2EIT(final PipelineTestParameter testParam) {
        databaseType = testParam.getDatabaseType();
        containerComposer = ENV.getItEnvType() == PipelineEnvTypeEnum.DOCKER
                ? new DockerContainerComposer(testParam.getDatabaseType(), testParam.getStorageContainerImage())
                : new NativeContainerComposer(testParam.getDatabaseType());
        containerComposer.start();
        if (ENV.getItEnvType() == PipelineEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainer();
            username = storageContainer.getUsername();
            password = storageContainer.getPassword();
        } else {
            username = ENV.getActualDataSourceUsername(databaseType);
            password = ENV.getActualDataSourcePassword(databaseType);
        }
        createProxyDatabase(testParam.getDatabaseType());
        if (PipelineEnvTypeEnum.NATIVE == ENV.getItEnvType()) {
            cleanUpDataSource();
        }
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(PipelineBaseE2EIT.class.getClassLoader().getResource(testParam.getScenario())), ExtraSQLCommand.class);
        pipelineWatcher = new PipelineWatcher(containerComposer);
    }
    
    private void cleanUpDataSource() {
        for (String each : Arrays.asList(DS_0, DS_1, DS_2, DS_3, DS_4)) {
            containerComposer.cleanUpDatabase(each);
        }
    }
    
    protected void createProxyDatabase(final DatabaseType databaseType) {
        String defaultDatabaseName = "";
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            defaultDatabaseName = "postgres";
        }
        String jdbcUrl = containerComposer.getProxyJdbcUrl(defaultDatabaseName);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD)) {
            if (PipelineEnvTypeEnum.NATIVE == ENV.getItEnvType()) {
                try {
                    connectionExecuteWithLog(connection, String.format("DROP DATABASE %s", PROXY_DATABASE));
                } catch (final SQLException ex) {
                    log.warn("Drop proxy database failed, maybe it's not exist. error msg={}", ex.getMessage());
                }
            }
            connectionExecuteWithLog(connection, String.format("CREATE DATABASE %s", PROXY_DATABASE));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        sourceDataSource = StorageContainerUtil.generateDataSource(appendExtraParam(getActualJdbcUrlTemplate(DS_0, false)), username, password);
        proxyDataSource = StorageContainerUtil.generateDataSource(containerComposer.getProxyJdbcUrl(PROXY_DATABASE), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD);
    }
    
    protected void addResource(final String distSQL) throws SQLException {
        proxyExecuteWithLog(distSQL, 2);
    }
    
    protected String appendExtraParam(final String jdbcUrl) {
        return DatabaseTypeUtil.isMySQL(getDatabaseType())
                ? new JdbcUrlAppender().appendQueryProperties(jdbcUrl, PropertiesBuilder.build(new Property("rewriteBatchedStatements", Boolean.TRUE.toString())))
                : jdbcUrl;
    }
    
    protected String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer) {
        if (PipelineEnvTypeEnum.DOCKER == ENV.getItEnvType()) {
            DockerStorageContainer storageContainer = ((DockerContainerComposer) containerComposer).getStorageContainer();
            return isInContainer
                    ? DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getExposedPort(), databaseName)
                    : DataSourceEnvironment.getURL(getDatabaseType(), storageContainer.getHost(), storageContainer.getFirstMappedPort(), databaseName);
        }
        return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    protected abstract String getSourceTableOrderName();
    
    protected String getTargetTableOrderName() {
        return "t_order";
    }
    
    protected void createSourceOrderTable() throws SQLException {
        sourceExecuteWithLog(getExtraSQLCommand().getCreateTableOrder(getSourceTableOrderName()));
    }
    
    protected void createSourceTableIndexList(final String schema) throws SQLException {
        if (DatabaseTypeUtil.isPostgreSQL(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE INDEX IF NOT EXISTS idx_user_id ON %s.%s ( user_id )", schema, getSourceTableOrderName()));
        } else if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE INDEX idx_user_id ON %s.%s ( user_id )", schema, getSourceTableOrderName()));
        }
    }
    
    protected void createSourceCommentOnList(final String schema) throws SQLException {
        sourceExecuteWithLog(String.format("COMMENT ON COLUMN %s.%s.user_id IS 'user id'", schema, getSourceTableOrderName()));
    }
    
    protected void createSourceOrderItemTable() throws SQLException {
        sourceExecuteWithLog(extraSQLCommand.getCreateTableOrderItem());
    }
    
    protected void sourceExecuteWithLog(final String sql) throws SQLException {
        log.info("source execute :{}", sql);
        try (Connection connection = sourceDataSource.getConnection()) {
            connection.createStatement().execute(sql);
        }
    }
    
    protected void proxyExecuteWithLog(final String sql, final int sleepSeconds) throws SQLException {
        log.info("proxy execute :{}", sql);
        try (Connection connection = proxyDataSource.getConnection()) {
            connection.createStatement().execute(sql);
        }
        ThreadUtil.sleep(Math.max(sleepSeconds, 0), TimeUnit.SECONDS);
    }
    
    protected void waitJobPrepareSuccess(final String distSQL) {
        for (int i = 0; i < 5; i++) {
            List<Map<String, Object>> jobStatus = queryForListWithLog(distSQL);
            Set<String> statusSet = jobStatus.stream().map(each -> String.valueOf(each.get("status"))).collect(Collectors.toSet());
            if (statusSet.contains(JobStatus.PREPARING.name()) || statusSet.contains(JobStatus.RUNNING.name())) {
                ThreadUtil.sleep(2, TimeUnit.SECONDS);
            }
        }
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
                List<Map<String, Object>> result = transformResultSetToList(resultSet);
                log.info("proxy query for list, sql: {}, result: {}", sql, result);
                return result;
            } catch (final SQLException ex) {
                log.error("data access error", ex);
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
            retryNumber++;
        }
        throw new RuntimeException("can't get result from proxy");
    }
    
    private List<Map<String, Object>> transformResultSetToList(final ResultSet resultSet) throws SQLException {
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
    
    protected void startIncrementTask(final BaseIncrementTask baseIncrementTask) {
        increaseTaskThread = new Thread(baseIncrementTask);
        increaseTaskThread.start();
    }
    
    // TODO use DAO to query via DistSQL
    protected List<Map<String, Object>> waitIncrementTaskFinished(final String distSQL) throws InterruptedException {
        if (null != increaseTaskThread) {
            TimeUnit.SECONDS.timedJoin(increaseTaskThread, 30);
        }
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> listJobStatus = queryForListWithLog(distSQL);
            log.info("show status result: {}", listJobStatus);
            Set<String> actualStatus = new HashSet<>();
            Collection<Integer> incrementalIdleSecondsList = new LinkedList<>();
            for (Map<String, Object> each : listJobStatus) {
                assertTrue("error_message is not null", Strings.isNullOrEmpty(each.get("error_message").toString()));
                actualStatus.add(each.get("status").toString());
                String incrementalIdleSeconds = each.get("incremental_idle_seconds").toString();
                incrementalIdleSecondsList.add(Strings.isNullOrEmpty(incrementalIdleSeconds) ? 0 : Integer.parseInt(incrementalIdleSeconds));
            }
            assertFalse("status is JobStatus.PREPARING_FAILURE", actualStatus.contains(JobStatus.PREPARING_FAILURE.name()));
            assertFalse("status is JobStatus.EXECUTE_INVENTORY_TASK_FAILURE", actualStatus.contains(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name()));
            assertFalse("status is JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE", actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name()));
            if (Collections.min(incrementalIdleSecondsList) <= 5) {
                ThreadUtil.sleep(3, TimeUnit.SECONDS);
                continue;
            }
            if (actualStatus.size() == 1 && actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                return listJobStatus;
            }
        }
        return Collections.emptyList();
    }
    
    protected void assertProxyOrderRecordExist(final Object id, final String tableName) {
        boolean recordExist = false;
        for (int i = 0; i < 5; i++) {
            String sql = String.format("select * from %s where order_id = %s", tableName, id);
            List<Map<String, Object>> result = queryForListWithLog(sql);
            recordExist = !result.isEmpty();
            if (recordExist) {
                break;
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
        }
        assertTrue("The insert record must exist after the stop", recordExist);
    }
    
    protected void assertGreaterThanOrderTableInitRows(final int tableInitRows, final String schema) throws SQLException {
        proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        String countSQL = Strings.isNullOrEmpty(schema) ? "SELECT COUNT(*) as count FROM t_order" : String.format("SELECT COUNT(*) as count FROM %s.t_order", schema);
        Map<String, Object> actual = queryForListWithLog(countSQL).get(0);
        assertTrue("actual count " + actual.get("count"), Integer.parseInt(actual.get("count").toString()) > tableInitRows);
    }
}
