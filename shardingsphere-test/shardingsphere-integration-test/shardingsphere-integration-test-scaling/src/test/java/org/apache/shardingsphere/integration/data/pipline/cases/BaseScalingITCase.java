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

package org.apache.shardingsphere.integration.data.pipline.cases;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.data.pipline.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.container.compose.LocalComposedContainer;
import org.apache.shardingsphere.integration.data.pipline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipline.env.enums.ITEnvTypeEnum;
import org.junit.Before;
import org.testcontainers.shaded.com.google.common.base.Splitter;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseScalingITCase {
    
    @Getter(AccessLevel.NONE)
    private final BaseComposedContainer composedContainer;
    
    @Getter(AccessLevel.PROTECTED)
    private CommonSQLCommand commonSQLCommand;
    
    public BaseScalingITCase(final DatabaseType databaseType) {
        if (StringUtils.equalsIgnoreCase(IntegrationTestEnvironment.getInstance().getItEnvType(), ITEnvTypeEnum.DOCKER.name())) {
            composedContainer = new DockerComposedContainer(databaseType);
        } else {
            composedContainer = new LocalComposedContainer(databaseType);
        }
    }
    
    @Before
    public void init() throws SQLException {
        composedContainer.start();
        commonSQLCommand = JAXB.unmarshal(BaseScalingITCase.class.getClassLoader().getResource("env/common/command.xml"), CommonSQLCommand.class);
        initScalingEnvironment();
    }
    
    @SneakyThrows
    protected void initScalingEnvironment() {
        try (Connection connection = getProxyConnection("")) {
            connection.createStatement().execute(commonSQLCommand.getCreateDatabase());
            connection.createStatement().execute(commonSQLCommand.getUseDatabase());
            int dbIndex = 0;
            for (String dbName : listSourceDatabaseName()) {
                connection.createStatement().execute(String.format(commonSQLCommand.getAddResource(), dbIndex, getDatabaseUrl(), dbName));
                dbIndex++;
            }
            for (String value : listTargetDatabaseName()) {
                connection.createStatement().execute(String.format(commonSQLCommand.getAddResource(), dbIndex, getDatabaseUrl(), value));
                dbIndex++;
            }
            for (String sql : Splitter.on(";").splitToList(commonSQLCommand.getCreateShardingAlgorithm())) {
                connection.createStatement().execute(sql);
            }
            // TODO sleep to wait for sharding algorithm table created，otherwise, the next sql will fail.
            TimeUnit.SECONDS.sleep(1);
            connection.createStatement().execute(commonSQLCommand.getCreateShardingTable());
            connection.createStatement().execute(commonSQLCommand.getCreateShardingBinding());
            connection.createStatement().execute(commonSQLCommand.getCreateShardingScalingRule());
        }
    }
    
    /**
     * Get proxy database connection.
     *
     * @param dataSourceName data source names
     * @return proxy database connection
     */
    @SneakyThrows(SQLException.class)
    public Connection getProxyConnection(final String dataSourceName) {
        return composedContainer.getProxyConnection(dataSourceName);
    }
    
    /**
     * Get database url, such as  ip:port.
     *
     * @return database url
     */
    public String getDatabaseUrl() {
        if (StringUtils.equalsIgnoreCase(IntegrationTestEnvironment.getInstance().getItEnvType(), ITEnvTypeEnum.DOCKER.name())) {
            return Joiner.on(":").join("db.host", composedContainer.getDatabaseContainer().getPort());
        } else {
            return Joiner.on(":").join(composedContainer.getDatabaseContainer().getHost(), composedContainer.getDatabaseContainer().getFirstMappedPort());
        }
    }
    
    /**
     * Query actual source database name.
     *
     * @return actual source database name list
     */
    public List<String> listSourceDatabaseName() {
        return composedContainer.getDatabaseContainer().getSourceDatabaseNames();
    }
    
    /**
     * Query actual target database name.
     *
     * @return actual target database name list
     */
    public List<String> listTargetDatabaseName() {
        return composedContainer.getDatabaseContainer().getTargetDatabaseNames();
    }
    
    /**
     * Check data match consistency.
     * @param connection proxy database connection
     * @param jobId job id
     * @throws InterruptedException if interrupted
     * @throws SQLException if any SQL exception
     */
    protected void checkMatchConsistency(final Connection connection, final String jobId) throws InterruptedException, SQLException {
        Map<String, String> actualStatusMap = new HashMap<>(2, 1);
        for (int i = 0; i < 100; i++) {
            ResultSet statusResult = connection.createStatement().executeQuery(String.format(commonSQLCommand.getShowScalingStatus(), jobId));
            boolean finished = true;
            while (statusResult.next()) {
                String datasourceName = statusResult.getString(2);
                String status = statusResult.getString(3);
                actualStatusMap.put(datasourceName, status);
                assertThat(status, not(JobStatus.PREPARING_FAILURE.name()));
                assertThat(status, not(JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name()));
                assertThat(status, not(JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name()));
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
        connection.createStatement().execute(String.format(getCommonSQLCommand().getStopScalingSourceWriting(), jobId));
        ResultSet checkScalingResult = connection.createStatement().executeQuery(String.format(commonSQLCommand.getCheckScalingDataMatch(), jobId));
        while (checkScalingResult.next()) {
            assertTrue(checkScalingResult.getBoolean(5));
        }
        connection.createStatement().execute(String.format(getCommonSQLCommand().getApplyScaling(), jobId));
        ResultSet previewResult = connection.createStatement().executeQuery(getCommonSQLCommand().getPreviewSelectOrder());
        List<String> actualTargetNodes = Lists.newLinkedList();
        while (previewResult.next()) {
            actualTargetNodes.add(previewResult.getString(1));
        }
        assertThat(actualTargetNodes, is(Lists.newArrayList("ds_2", "ds_3", "ds_4")));
    }
    
    /**
     * Initialize table data.
     * @param connection proxy database connection
     * @param insertOrderSQL insert order sql
     * @param insertOrderItemSQL insert order item sql
     * @throws SQLException if any SQL exception
     */
    protected abstract void initTableData(Connection connection, String insertOrderSQL, String insertOrderItemSQL) throws SQLException;
}
