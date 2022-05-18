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
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.NativeComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.DockerDatabaseContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingCaseHelper;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.integration.data.pipeline.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.junit.After;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final BaseComposedContainer composedContainer;
    
    private final CommonSQLCommand commonSQLCommand;
    
    private final DatabaseType databaseType;
    
    private JdbcTemplate jdbcTemplate;
    
    @Setter
    private Thread increaseTaskThread;
    
    public BaseITCase(final ScalingParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            composedContainer = new DockerComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        } else {
            composedContainer = new NativeComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        }
        composedContainer.start();
        commonSQLCommand = JAXB.unmarshal(BaseITCase.class.getClassLoader().getResource("env/common/command.xml"), CommonSQLCommand.class);
        createProxyDatabase(parameterized.getDatabaseType());
    }
    
    @SneakyThrows
    protected void createProxyDatabase(final DatabaseType databaseType) {
        JdbcUrlAppender jdbcUrlAppender = new JdbcUrlAppender();
        Properties queryProps = ScalingCaseHelper.getQueryPropertiesByDatabaseType(databaseType);
        String defaultDatabaseName = DatabaseTypeUtil.isPostgreSQL(databaseType) ? "postgres" : "";
        try (Connection connection = DriverManager.getConnection(jdbcUrlAppender.appendQueryProperties(composedContainer.getProxyJdbcUrl(defaultDatabaseName), queryProps), "root", "root")) {
            connection.createStatement().execute("CREATE DATABASE sharding_db");
        }
        jdbcTemplate = new JdbcTemplate(getProxyDataSource("sharding_db"));
    }
    
    private DataSource getProxyDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(composedContainer.getDatabaseContainer().getDatabaseType()));
        result.setJdbcUrl(composedContainer.getProxyJdbcUrl(databaseName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    protected boolean waitShardingAlgorithmEffect(final int maxWaitTimes) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        int waitTimes = 0;
        do {
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SHOW SHARDING ALGORITHMS");
            if (result.size() >= 3) {
                log.info("waitShardingAlgorithmEffect time consume: {}", System.currentTimeMillis() - startTime);
                return true;
            }
            TimeUnit.SECONDS.sleep(2);
            waitTimes++;
        } while (waitTimes <= maxWaitTimes);
        return false;
    }
    
    @SneakyThrows
    protected void addSourceResource() {
        Properties queryProps = ScalingCaseHelper.getQueryPropertiesByDatabaseType(databaseType);
        // TODO if mysql can append database firstly, they can be combined
        if (databaseType instanceof MySQLDatabaseType) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl(""), queryProps), "root", "root")) {
                connection.createStatement().execute("USE sharding_db");
                addSourceResource0(connection);
            }
        } else {
            try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl("sharding_db"), queryProps), "root", "root")) {
                addSourceResource0(connection);
            }
        }
    }
    
    private void addSourceResource0(final Connection connection) throws SQLException {
        Properties queryProps = ScalingCaseHelper.getQueryPropertiesByDatabaseType(databaseType);
        String addSourceResource = commonSQLCommand.getSourceAddResourceTemplate().replace("${user}", ScalingCaseHelper.getUsername(databaseType))
                .replace("${password}", ScalingCaseHelper.getPassword(databaseType))
                .replace("${ds0}", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_0"), queryProps))
                .replace("${ds1}", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_1"), queryProps));
        connection.createStatement().execute(addSourceResource);
    }
    
    @SneakyThrows
    protected void addTargetResource() {
        Properties queryProps = ScalingCaseHelper.getQueryPropertiesByDatabaseType(databaseType);
        String addTargetResource = commonSQLCommand.getTargetAddResourceTemplate().replace("${user}", ScalingCaseHelper.getUsername(databaseType))
                .replace("${password}", ScalingCaseHelper.getPassword(databaseType))
                .replace("${ds2}", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_2"), queryProps))
                .replace("${ds3}", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_3"), queryProps))
                .replace("${ds4}", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_4"), queryProps));
        getJdbcTemplate().execute(addTargetResource);
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName) {
        final DockerDatabaseContainer databaseContainer = composedContainer.getDatabaseContainer();
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            return String.format("jdbc:%s://%s:%s/%s", databaseContainer.getDatabaseType().getType().toLowerCase(), "db.host", databaseContainer.getPort(), databaseName);
        } else {
            return String.format("jdbc:%s://%s:%s/%s", databaseContainer.getDatabaseType().getType().toLowerCase(), "127.0.0.1", databaseContainer.getFirstMappedPort(), databaseName);
        }
    }
    
    protected void initShardingAlgorithm() throws InterruptedException {
        jdbcTemplate.execute(getCommonSQLCommand().getCreateDatabaseShardingAlgorithm());
        TimeUnit.SECONDS.sleep(2);
        jdbcTemplate.execute(getCommonSQLCommand().getCreateOrderShardingAlgorithm());
        TimeUnit.SECONDS.sleep(2);
        jdbcTemplate.execute(getCommonSQLCommand().getCreateOrderItemShardingAlgorithm());
    }
    
    protected void createAllSharingTableRule() {
        jdbcTemplate.execute(commonSQLCommand.getCreateAllSharingTableRule());
    }
    
    protected void createOrderSharingTableRule() {
        jdbcTemplate.execute(commonSQLCommand.getCreateOrderShardingTableRule());
    }
    
    protected void bindingShardingRule() {
        jdbcTemplate.execute("CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item)");
    }
    
    protected void createScalingRule() {
        jdbcTemplate.execute("CREATE SHARDING SCALING RULE scaling_manual (INPUT(SHARDING_SIZE=1000), DATA_CONSISTENCY_CHECKER(TYPE(NAME=DATA_MATCH)))");
    }
    
    protected void createSchema(final String schemaName) {
        jdbcTemplate.execute(String.format("CREATE SCHEMA %s", schemaName));
    }
    
    protected void startIncrementTask(final BaseIncrementTask baseIncrementTask) {
        setIncreaseTaskThread(new Thread(baseIncrementTask));
        getIncreaseTaskThread().start();
    }
    
    protected void assertOriginalSourceSuccess() {
        List<Map<String, Object>> previewResults = getJdbcTemplate().queryForList("PREVIEW SELECT COUNT(1) FROM t_order");
        Set<Object> originalSources = previewResults.stream().map(each -> each.get("data_source_name")).collect(Collectors.toSet());
        assertThat(originalSources, is(new HashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    /**
     * Check data match consistency.
     *
     * @throws InterruptedException interrupted exception
     */
    protected void assertCheckMatchConsistencySuccess() throws InterruptedException {
        if (null != increaseTaskThread) {
            increaseTaskThread.join(60 * 1000L);
        }
        String jobId = String.valueOf(getJdbcTemplate().queryForMap("SHOW SCALING LIST").get("id"));
        Map<String, String> actualStatusMap = new HashMap<>(2, 1);
        for (int i = 0; i < 100; i++) {
            List<Map<String, Object>> showScalingStatusResMap = jdbcTemplate.queryForList(String.format("SHOW SCALING STATUS %s", jobId));
            log.info("actualStatusMap: {}", actualStatusMap);
            boolean finished = true;
            for (Map<String, Object> entry : showScalingStatusResMap) {
                String status = entry.get("status").toString();
                assertThat(status, not(JobStatus.PREPARING_FAILURE.name()));
                assertThat(status, not(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name()));
                assertThat(status, not(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name()));
                String datasourceName = entry.get("data_source").toString();
                actualStatusMap.put(datasourceName, status);
                if (!Objects.equals(status, JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                    finished = false;
                    break;
                }
            }
            if (finished) {
                break;
            }
            TimeUnit.SECONDS.sleep(2);
        }
        assertThat(actualStatusMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet()).size(), is(1));
        jdbcTemplate.execute(String.format("STOP SCALING SOURCE WRITING %s", jobId));
        List<Map<String, Object>> checkScalingResults = jdbcTemplate.queryForList(String.format("CHECK SCALING %s BY TYPE (NAME=DATA_MATCH)", jobId));
        log.info("checkScalingResults: {}", checkScalingResults);
        for (Map<String, Object> entry : checkScalingResults) {
            assertTrue(Boolean.parseBoolean(entry.get("records_content_matched").toString()));
        }
        jdbcTemplate.execute(String.format("APPLY SCALING %s", jobId));
        // TODO make sure the scaling job was applied
        TimeUnit.SECONDS.sleep(2);
        List<Map<String, Object>> previewResults = jdbcTemplate.queryForList("PREVIEW SELECT COUNT(1) FROM t_order");
        Set<Object> originalSources = previewResults.stream().map(each -> each.get("data_source_name")).collect(Collectors.toSet());
        log.info("originalSources: {}", originalSources);
        assertThat(originalSources, is(new HashSet<>(Arrays.asList("ds_2", "ds_3", "ds_4"))));
    }
    
    @After
    public void stopContainer() {
        if (composedContainer instanceof DockerComposedContainer) {
            log.info(((DockerComposedContainer) composedContainer).getProxyContainer().getLogs());
        }
        composedContainer.stop();
    }
}
