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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.NativeComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.integration.data.pipeline.framework.watcher.ScalingWatcher;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    protected static final String SCHEMA_NAME = "test";
    
    protected static final String PROXY_DATABASE = "sharding_db";
    
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
    
    private final ExtraSQLCommand extraSQLCommand;
    
    private final DatabaseType databaseType;
    
    private final String username;
    
    private final String password;
    
    private DataSource sourceDataSource;
    
    private DataSource proxyDataSource;
    
    @Setter
    private Thread increaseTaskThread;
    
    public BaseITCase(final ScalingParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            composedContainer = new DockerComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        } else {
            composedContainer = new NativeComposedContainer(parameterized.getDatabaseType());
        }
        composedContainer.start();
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            DockerStorageContainer storageContainer = ((DockerComposedContainer) composedContainer).getStorageContainer();
            username = storageContainer.getUsername();
            password = storageContainer.getPassword();
        } else {
            username = ENV.getActualDataSourceUsername(databaseType);
            password = ENV.getActualDataSourcePassword(databaseType);
        }
        createProxyDatabase(parameterized.getDatabaseType());
        if (ENV.getItEnvType() == ITEnvTypeEnum.NATIVE) {
            cleanUpDataSource();
        }
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource(parameterized.getScenario())), ExtraSQLCommand.class);
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
            if (ENV.getItEnvType() == ITEnvTypeEnum.NATIVE) {
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
        sourceDataSource = getDataSource(getActualJdbcUrlTemplate(DS_0, false), username, password);
        proxyDataSource = getDataSource(composedContainer.getProxyJdbcUrl(PROXY_DATABASE), ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD);
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
    
    protected void addResource(final String distSQL) throws SQLException {
        proxyExecuteWithLog(distSQL, 2);
    }
    
    protected String getActualJdbcUrlTemplate(final String databaseName, final boolean isInContainer) {
        if (ITEnvTypeEnum.DOCKER == ENV.getItEnvType()) {
            DockerStorageContainer storageContainer = ((DockerComposedContainer) composedContainer).getStorageContainer();
            if (isInContainer) {
                return DataSourceEnvironment.getURL(getDatabaseType(), getDatabaseType().getType().toLowerCase() + ".host", storageContainer.getExposedPort(), databaseName);
            } else {
                return DataSourceEnvironment.getURL(getDatabaseType(), storageContainer.getHost(), storageContainer.getFirstMappedPort(), databaseName);
            }
        }
        return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
    }
    
    protected void createSourceOrderTable() throws SQLException {
        sourceExecuteWithLog(extraSQLCommand.getCreateTableOrder());
    }
    
    protected void createSourceTableIndexList(final String schema) throws SQLException {
        if (DatabaseTypeUtil.isPostgreSQL(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE INDEX IF NOT EXISTS idx_user_id ON %s.t_order_copy ( user_id )", schema));
        } else if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE INDEX idx_user_id ON %s.t_order_copy ( user_id )", schema));
        }
    }
    
    protected void createSourceCommentOnList(final String schema) throws SQLException {
        sourceExecuteWithLog(String.format("COMMENT ON COLUMN %s.t_order_copy.user_id IS 'user id'", schema));
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
    
    protected void connectionExecuteWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("connection execute:{}", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(2, TimeUnit.SECONDS);
    }
    
    protected List<Map<String, Object>> queryForListWithLog(final String sql) {
        log.info("proxy query for list:{}", sql);
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
    
    protected void waitJobFinished(final String distSQL) throws InterruptedException {
        if (null != getIncreaseTaskThread()) {
            TimeUnit.SECONDS.timedJoin(getIncreaseTaskThread(), 60);
        }
        Set<String> actualStatus;
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> listJobStatus = queryForListWithLog(distSQL);
            log.info("show status result: {}", listJobStatus);
            actualStatus = listJobStatus.stream().map(each -> each.get("status").toString()).collect(Collectors.toSet());
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
    
    protected void assertGreaterThanOrderTableInitRows(final int tableInitRows, final String schema) throws SQLException {
        proxyExecuteWithLog("REFRESH TABLE METADATA", 2);
        String countSQL = StringUtils.isBlank(schema) ? "SELECT COUNT(*) as count FROM t_order" : String.format("SELECT COUNT(*) as count FROM %s.t_order", schema);
        Map<String, Object> actual = queryForListWithLog(countSQL).get(0);
        assertTrue("actual count " + actual.get("count"), Integer.parseInt(actual.get("count").toString()) > tableInitRows);
    }
}
