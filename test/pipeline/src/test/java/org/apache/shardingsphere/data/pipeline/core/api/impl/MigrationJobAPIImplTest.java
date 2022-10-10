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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateMigrationJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobItemContext;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class MigrationJobAPIImplTest {
    
    private static MigrationJobAPI jobAPI;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        jobAPI = MigrationJobAPIFactory.getInstance();
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:h2:mem:test_ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("username", "root");
        props.put("password", "root");
        Map<String, DataSourceProperties> expect = new LinkedHashMap<>(1, 1);
        expect.put("ds_0", new DataSourceProperties("com.zaxxer.hikari.HikariDataSource", props));
        jobAPI.addMigrationSourceResources(expect);
    }
    
    @AfterClass
    public static void afterClass() {
        jobAPI.dropMigrationSourceResources(Collections.singletonList("ds_0"));
    }
    
    @Test
    public void assertStartAndList() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        PipelineJobInfo jobInfo = getNonNullJobInfo(jobId.get());
        assertTrue(jobInfo.getJobDetail().isActive());
        assertThat(((TableBasedPipelineJobInfo) jobInfo).getTable(), is("t_order"));
        assertThat(jobInfo.getJobDetail().getShardingTotalCount(), is(1));
    }
    
    private Optional<? extends PipelineJobInfo> getJobInfo(final String jobId) {
        return jobAPI.list().stream().filter(each -> Objects.equals(each.getJobDetail().getJobId(), jobId)).reduce((a, b) -> a);
    }
    
    private PipelineJobInfo getNonNullJobInfo(final String jobId) {
        Optional<? extends PipelineJobInfo> result = getJobInfo(jobId);
        assertTrue(result.isPresent());
        return result.get();
    }
    
    @Test
    public void assertStartOrStopById() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        assertTrue(getNonNullJobInfo(jobId.get()).getJobDetail().isActive());
        jobAPI.stop(jobId.get());
        assertFalse(getNonNullJobInfo(jobId.get()).getJobDetail().isActive());
        jobAPI.startDisabledJob(jobId.get());
        assertTrue(getNonNullJobInfo(jobId.get()).getJobDetail().isActive());
    }
    
    @Test
    public void assertRollback() throws SQLException {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        MigrationJobConfiguration jobConfig = jobAPI.getJobConfiguration(jobId.get());
        initTableData(jobConfig);
        jobAPI.rollback(jobId.get());
        assertFalse(getJobInfo(jobId.get()).isPresent());
    }
    
    @Test
    public void assertCommit() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        MigrationJobConfiguration jobConfig = jobAPI.getJobConfiguration(jobId.get());
        initTableData(jobConfig);
        jobAPI.commit(jobId.get());
        assertFalse(getJobInfo(jobId.get()).isPresent());
    }
    
    @Test
    public void assertGetProgress() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgressMap = jobAPI.getJobProgress(jobId.get());
        assertThat(jobProgressMap.size(), is(1));
    }
    
    @Test
    public void assertDataConsistencyCheck() throws NoSuchFieldException, IllegalAccessException {
        MigrationJobConfiguration jobConfiguration = JobConfigurationBuilder.createJobConfiguration();
        ReflectionUtil.setFieldValue(jobConfiguration, "uniqueKeyColumn", new PipelineColumnMetaData(1, "order_id", 4, "", false, true, true));
        Optional<String> jobId = jobAPI.start(jobConfiguration);
        assertTrue(jobId.isPresent());
        initTableData(jobAPI.getJobConfiguration(jobId.get()));
        Map<String, DataConsistencyCheckResult> checkResultMap = jobAPI.dataConsistencyCheck(jobId.get());
        assertThat(checkResultMap.size(), is(1));
    }
    
    @Test
    public void assertDataConsistencyCheckWithAlgorithm() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        initTableData(jobAPI.getJobConfiguration(jobId.get()));
        Map<String, DataConsistencyCheckResult> checkResultMap = jobAPI.dataConsistencyCheck(jobId.get(), "FIXTURE", null);
        assertThat(checkResultMap.size(), is(1));
        assertTrue(checkResultMap.get("t_order").getCountCheckResult().isMatched());
        assertThat(checkResultMap.get("t_order").getCountCheckResult().getTargetRecordsCount(), is(2L));
        assertTrue(checkResultMap.get("t_order").getContentCheckResult().isMatched());
    }
    
    @Test
    public void assertAggregateEmptyDataConsistencyCheckResults() {
        assertFalse(jobAPI.aggregateDataConsistencyCheckResults("foo_job", Collections.emptyMap()));
    }
    
    @Test
    public void assertAggregateDifferentCountDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyCountCheckResult notEqualCountCheckResult = new DataConsistencyCountCheckResult(100, 95);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(false);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(notEqualCountCheckResult, equalContentCheckResult));
        assertFalse(jobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    public void assertAggregateDifferentContentDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(true);
        DataConsistencyContentCheckResult notEqualContentCheckResult = new DataConsistencyContentCheckResult(false);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(equalCountCheckResult, notEqualContentCheckResult));
        assertFalse(jobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    public void assertAggregateSameDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(true);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        assertTrue(jobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    public void assertSwitchClusterConfigurationSucceed() {
        final MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        Optional<String> jobId = jobAPI.start(jobConfig);
        assertTrue(jobId.isPresent());
        MigrationJobItemContext jobItemContext = PipelineContextUtil.mockMigrationJobItemContext(jobConfig);
        jobAPI.persistJobItemProgress(jobItemContext);
        jobAPI.updateJobItemStatus(jobId.get(), jobItemContext.getShardingItem(), JobStatus.EXECUTE_INVENTORY_TASK);
        Map<Integer, InventoryIncrementalJobItemProgress> progress = jobAPI.getJobProgress(jobId.get());
        for (Entry<Integer, InventoryIncrementalJobItemProgress> entry : progress.entrySet()) {
            assertSame(entry.getValue().getStatus(), JobStatus.EXECUTE_INVENTORY_TASK);
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final MigrationJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
        initTableData(PipelineDataSourceCreatorFactory.getInstance(sourceDataSourceConfig.getType()).createPipelineDataSource(sourceDataSourceConfig.getDataSourceConfiguration()));
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        initTableData(PipelineDataSourceCreatorFactory.getInstance(targetDataSourceConfig.getType()).createPipelineDataSource(targetDataSourceConfig.getDataSourceConfiguration()));
    }
    
    private void initTableData(final DataSource pipelineDataSource) throws SQLException {
        try (
                Connection connection = pipelineDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT(11))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 0), (999, 15)");
        }
    }
    
    @Test
    public void assertRenewJobStatus() {
        final MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        MigrationJobItemContext jobItemContext = PipelineContextUtil.mockMigrationJobItemContext(jobConfig);
        jobAPI.persistJobItemProgress(jobItemContext);
        jobAPI.updateJobItemStatus(jobConfig.getJobId(), 0, JobStatus.FINISHED);
        InventoryIncrementalJobItemProgress actual = jobAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertNotNull(actual);
        assertThat(actual.getStatus(), is(JobStatus.FINISHED));
    }
    
    @Test
    public void assertAddMigrationSourceResources() {
        PipelineDataSourcePersistService persistService = new PipelineDataSourcePersistService();
        Map<String, DataSourceProperties> actual = persistService.load(JobType.MIGRATION);
        assertTrue(actual.containsKey("ds_0"));
    }
    
    @Test
    public void assertCreateJobConfig() throws SQLException {
        initIntPrimaryEnvironment();
        CreateMigrationJobParameter parameter = new CreateMigrationJobParameter("ds_0", null, "t_order", "logic_db", "t_order");
        String jobId = jobAPI.createJobAndStart(parameter);
        MigrationJobConfiguration jobConfig = jobAPI.getJobConfiguration(jobId);
        assertThat(jobConfig.getSourceResourceName(), is("ds_0"));
        assertThat(jobConfig.getSourceTableName(), is("t_order"));
        assertThat(jobConfig.getTargetDatabaseName(), is("logic_db"));
        assertThat(jobConfig.getTargetTableName(), is("t_order"));
    }
    
    private void initIntPrimaryEnvironment() throws SQLException {
        Map<String, DataSourceProperties> metaDataDataSource = new PipelineDataSourcePersistService().load(JobType.MIGRATION);
        DataSourceProperties dataSourceProperties = metaDataDataSource.get("ds_0");
        DataSource dataSource = DataSourcePoolCreator.create(dataSourceProperties);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id int(10))");
        }
    }
    
    @Test
    public void assertShowMigrationSourceResources() {
        Collection<Collection<Object>> actual = jobAPI.listMigrationSourceResources();
        assertThat(actual.size(), is(1));
        Collection<Object> objects = actual.iterator().next();
        assertThat(objects.toArray()[0], is("ds_0"));
    }
}
