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

package org.apache.shardingsphere.integration.data.pipeline.cases;

import com.google.common.collect.Sets;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.compose.LocalComposedContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.DockerDatabaseContainer;
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
import java.util.HashMap;
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

@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final String ADD_RESOURCE_TEMPLATE = "ADD RESOURCE %s (URL='%s',USER=root,PASSWORD=root)";
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final BaseComposedContainer composedContainer;
    
    private final CommonSQLCommand commonSQLCommand;
    
    private JdbcTemplate jdbcTemplate;
    
    public BaseITCase(final ScalingParameterized parameterized) {
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            composedContainer = new DockerComposedContainer(parameterized.getDatabaseType(), parameterized.getDatabaseVersion());
        } else {
            composedContainer = new LocalComposedContainer(parameterized.getDatabaseType(), parameterized.getDatabaseVersion());
        }
        composedContainer.start();
        commonSQLCommand = JAXB.unmarshal(BaseITCase.class.getClassLoader().getResource("env/common/command.xml"), CommonSQLCommand.class);
        createProxyDatabase(parameterized.getDatabaseType());
    }
    
    @SneakyThrows
    protected void createProxyDatabase(final DatabaseType databaseType) {
        JdbcUrlAppender jdbcUrlAppender = new JdbcUrlAppender();
        Properties queryProperties = createQueryProperties();
        String defaultDatabaseName = DatabaseTypeUtil.isPostgreSQL(databaseType) ? "postgres" : "";
        try (Connection connection = DriverManager.getConnection(jdbcUrlAppender.appendQueryProperties(composedContainer.getProxyJdbcUrl(defaultDatabaseName), queryProperties), "root", "root")) {
            connection.createStatement().execute("CREATE DATABASE sharding_db");
        }
        jdbcTemplate = new JdbcTemplate(getProxyDataSource("sharding_db"));
    }
    
    protected abstract Properties createQueryProperties();
    
    /**
     * Get proxy database data source.
     *
     * @param databaseName database name
     * @return proxy database connection
     */
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
    
    protected String getActualJdbcUrlTemplate(final String databaseName) {
        final DockerDatabaseContainer databaseContainer = composedContainer.getDatabaseContainer();
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            return String.format("jdbc:%s://%s:%s/%s", databaseContainer.getDatabaseType().getName().toLowerCase(), "db.host", databaseContainer.getPort(), databaseName);
        } else {
            return String.format("jdbc:%s://%s:%s/%s", databaseContainer.getDatabaseType().getName().toLowerCase(), "127.0.0.1", databaseContainer.getFirstMappedPort(), databaseName);
        }
    }
    
    protected void addResource(final Connection connection) throws SQLException {
        Properties queryProperties = createQueryProperties();
        connection.createStatement().execute(String.format(ADD_RESOURCE_TEMPLATE, "ds_0", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_0"), queryProperties)));
        connection.createStatement().execute(String.format(ADD_RESOURCE_TEMPLATE, "ds_1", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_1"), queryProperties)));
        connection.createStatement().execute(String.format(ADD_RESOURCE_TEMPLATE, "ds_2", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_2"), queryProperties)));
        connection.createStatement().execute(String.format(ADD_RESOURCE_TEMPLATE, "ds_3", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_3"), queryProperties)));
        connection.createStatement().execute(String.format(ADD_RESOURCE_TEMPLATE, "ds_4", JDBC_URL_APPENDER.appendQueryProperties(getActualJdbcUrlTemplate("ds_4"), queryProperties)));
    }
    
    protected void initShardingRule() throws InterruptedException {
        for (String sql : getCommonSQLCommand().getCreateShardingAlgorithm()) {
            getJdbcTemplate().execute(sql);
            // TODO sleep to wait for sharding algorithm table created，otherwise, the next sql will fail.
            TimeUnit.SECONDS.sleep(1);
        }
        getJdbcTemplate().execute(getCommonSQLCommand().getCreateShardingTable());
        getJdbcTemplate().execute("CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item)");
        getJdbcTemplate().execute("CREATE SHARDING SCALING RULE scaling_manual (DATA_CONSISTENCY_CHECKER(TYPE(NAME=DATA_MATCH)))");
    }
    
    /**
     * Check data match consistency.
     *
     * @param jdbcTemplate jdbc template
     * @param jobId job id
     * @throws InterruptedException interrupted exception
     */
    protected void checkMatchConsistency(final JdbcTemplate jdbcTemplate, final String jobId) throws InterruptedException {
        Map<String, String> actualStatusMap = new HashMap<>(2, 1);
        for (int i = 0; i < 100; i++) {
            List<Map<String, Object>> showScalingStatusResMap = jdbcTemplate.queryForList(String.format("SHOW SCALING STATUS %s", jobId));
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
            } else {
                TimeUnit.SECONDS.sleep(2);
            }
        }
        assertThat(actualStatusMap.values().stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet()).size(), is(1));
        jdbcTemplate.execute(String.format("STOP SCALING SOURCE WRITING %s", jobId));
        List<Map<String, Object>> checkScalingResList = jdbcTemplate.queryForList(String.format("CHECK SCALING %s BY TYPE (NAME=DATA_MATCH)", jobId));
        for (Map<String, Object> entry : checkScalingResList) {
            assertTrue(Boolean.parseBoolean(entry.get("records_content_matched").toString()));
        }
        jdbcTemplate.execute(String.format("APPLY SCALING %s", jobId));
        // TODO make sure the scaling job was applied
        ThreadUtil.sleep(2000);
        List<Map<String, Object>> previewResList = jdbcTemplate.queryForList("PREVIEW SELECT COUNT(1) FROM t_order");
        Set<Object> originalSourceList = previewResList.stream().map(result -> result.get("data_source_name")).collect(Collectors.toSet());
        assertThat(originalSourceList, is(Sets.newHashSet("ds_2", "ds_3", "ds_4")));
    }
    
    @After
    public void stopContainer() {
        composedContainer.stop();
    }
}
