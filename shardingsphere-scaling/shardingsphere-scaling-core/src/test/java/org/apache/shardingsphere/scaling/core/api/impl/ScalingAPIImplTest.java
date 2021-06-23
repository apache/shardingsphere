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

package org.apache.shardingsphere.scaling.core.api.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ScalingAPIImplTest {
    
    private static ScalingAPI scalingAPI;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", null);
        ScalingContext.getInstance().init(mockServerConfig());
        scalingAPI = ScalingAPIFactory.getScalingAPI();
    }
    
    @Test
    public void assertStartAndList() {
        Optional<Long> jobId = scalingAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobInfo jobInfo = getNonNullJobInfo(jobId.get());
        assertTrue(jobInfo.isActive());
        assertThat(jobInfo.getTables(), is("t_order"));
        assertThat(jobInfo.getShardingTotalCount(), is(1));
    }
    
    private Optional<JobInfo> getJobInfo(final long jobId) {
        return scalingAPI.list().stream().filter(each -> each.getJobId() == jobId).reduce((a, b) -> a);
    }
    
    private JobInfo getNonNullJobInfo(final long jobId) {
        Optional<JobInfo> result = getJobInfo(jobId);
        assertTrue(result.isPresent());
        return result.get();
    }
    
    @Test
    public void assertStartOrStopById() {
        Optional<Long> jobId = scalingAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
        scalingAPI.stop(jobId.get());
        assertFalse(getNonNullJobInfo(jobId.get()).isActive());
        scalingAPI.start(jobId.get());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
    }
    
    @Test
    public void assertRemove() {
        Optional<Long> jobId = scalingAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        assertTrue(getJobInfo(jobId.get()).isPresent());
        scalingAPI.remove(jobId.get());
        assertFalse(getJobInfo(jobId.get()).isPresent());
    }
    
    @Test
    public void assertGetProgress() {
        Optional<Long> jobId = scalingAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        Map<Integer, JobProgress> jobProgressMap = scalingAPI.getProgress(jobId.get());
        assertThat(jobProgressMap.size(), is(1));
    }
    
    @Test
    public void assertDataConsistencyCheck() {
        Optional<Long> jobId = scalingAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobConfiguration jobConfig = scalingAPI.getJobConfig(jobId.get());
        initTableData(jobConfig.getRuleConfig());
        Map<String, DataConsistencyCheckResult> checkResultMap = scalingAPI.dataConsistencyCheck(jobId.get());
        assertThat(checkResultMap.size(), is(1));
        assertTrue(checkResultMap.get("t_order").isCountValid());
        assertFalse(checkResultMap.get("t_order").isDataValid());
        assertThat(checkResultMap.get("t_order").getTargetCount(), is(2L));
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertResetTargetTable() {
        Optional<Long> jobId = scalingAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobConfiguration jobConfig = scalingAPI.getJobConfig(jobId.get());
        initTableData(jobConfig.getRuleConfig());
        scalingAPI.reset(jobId.get());
        Map<String, DataConsistencyCheckResult> checkResultMap = scalingAPI.dataConsistencyCheck(jobId.get());
        assertThat(checkResultMap.get("t_order").getTargetCount(), is(0L));
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", null);
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setGovernanceConfig(new GovernanceConfiguration("test", new RegistryCenterConfiguration("Zookeeper", EmbedTestingServer.getConnectionString(), new Properties()), true));
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final RuleConfiguration ruleConfig) {
        initTableData(ruleConfig.getSource().unwrap().toDataSource());
        initTableData(ruleConfig.getTarget().unwrap().toDataSource());
    }
    
    private void initTableData(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
}
