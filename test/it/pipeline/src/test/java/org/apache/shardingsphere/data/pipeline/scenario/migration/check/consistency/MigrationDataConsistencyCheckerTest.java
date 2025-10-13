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

package org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckIgnoredType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationDataConsistencyCheckerTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
    }
    
    @Test
    void assertFixtureCheck() throws SQLException {
        MigrationJobConfiguration jobConfig = createJobConfiguration(true);
        Map<String, TableDataConsistencyCheckResult> checkResultMap = check(jobConfig, "FIXTURE");
        TableDataConsistencyCheckResult actual = checkResultMap.get("t_order");
        assertTrue(actual.isMatched());
        assertFalse(actual.isIgnored());
    }
    
    @Test
    void assertDataMatchCheck() throws SQLException {
        MigrationJobConfiguration jobConfig = createJobConfiguration(false);
        Map<String, TableDataConsistencyCheckResult> checkResultMap = check(jobConfig, "DATA_MATCH");
        TableDataConsistencyCheckResult actual = checkResultMap.get("t_order");
        assertFalse(actual.isMatched());
        assertTrue(actual.isIgnored());
        assertThat(actual.getIgnoredType(), is(TableDataConsistencyCheckIgnoredType.NO_UNIQUE_KEY));
    }
    
    private Map<String, TableDataConsistencyCheckResult> check(final MigrationJobConfiguration jobConfig, final String algorithmType) {
        JobConfigurationPOJO jobConfigurationPOJO = new JobConfigurationPOJO();
        jobConfigurationPOJO.setJobParameter(YamlEngine.marshal(new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration(jobConfig)));
        jobConfigurationPOJO.setJobName(jobConfig.getJobId());
        jobConfigurationPOJO.setShardingTotalCount(1);
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        getClusterPersistRepository().persist(String.format("/pipeline/jobs/%s/config", jobConfig.getJobId()), YamlEngine.marshal(jobConfigurationPOJO));
        governanceFacade.getJobItemFacade().getProcess().persist(jobConfig.getJobId(), 0, "");
        return new MigrationDataConsistencyChecker(jobConfig, new TransmissionProcessContext(jobConfig.getJobId(), null),
                createConsistencyCheckJobItemProgressContext(jobConfig.getJobId())).check(algorithmType, null);
    }
    
    private ClusterPersistRepository getClusterPersistRepository() {
        return (ClusterPersistRepository) PipelineContextManager.getContext(PipelineContextUtils.getContextKey()).getPersistServiceFacade().getRepository();
    }
    
    private ConsistencyCheckJobItemProgressContext createConsistencyCheckJobItemProgressContext(final String jobId) {
        return new ConsistencyCheckJobItemProgressContext(jobId, 0, "H2");
    }
    
    private MigrationJobConfiguration createJobConfiguration(final boolean orderHasUniqueKey) throws SQLException {
        MigrationJobItemContext jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        initTableData(jobItemContext.getTaskConfig().getDumperContext().getCommonContext().getDataSourceConfig(), orderHasUniqueKey);
        initTableData(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig(), orderHasUniqueKey);
        return jobItemContext.getJobConfig();
    }
    
    private void initTableData(final PipelineDataSourceConfiguration dataSourceConfig, final boolean orderHasUniqueKey) throws SQLException {
        try (
                PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
                PipelineDataSource dataSource = dataSourceManager.getDataSource(dataSourceConfig);
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute(String.format("CREATE TABLE t_order (order_id INT %s, user_id INT(11))", orderHasUniqueKey ? "PRIMARY KEY" : ""));
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 1), (999, 10)");
        }
    }
}
