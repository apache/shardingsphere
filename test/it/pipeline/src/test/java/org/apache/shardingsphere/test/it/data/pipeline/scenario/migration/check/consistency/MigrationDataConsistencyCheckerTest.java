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

package org.apache.shardingsphere.test.it.data.pipeline.scenario.migration.check.consistency;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency.MigrationDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationDataConsistencyCheckerTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertCountAndDataCheck() throws SQLException {
        MigrationJobConfiguration jobConfig = createJobConfiguration();
        JobConfigurationPOJO jobConfigurationPOJO = new JobConfigurationPOJO();
        jobConfigurationPOJO.setJobParameter(YamlEngine.marshal(new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration(jobConfig)));
        jobConfigurationPOJO.setJobName(jobConfig.getJobId());
        jobConfigurationPOJO.setShardingTotalCount(1);
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey());
        getClusterPersistRepository().persist(String.format("/pipeline/jobs/%s/config", jobConfig.getJobId()), YamlEngine.marshal(jobConfigurationPOJO));
        governanceFacade.getJobItemFacade().getProcess().persist(jobConfig.getJobId(), 0, "");
        Map<String, TableDataConsistencyCheckResult> actual = new MigrationDataConsistencyChecker(jobConfig, new TransmissionProcessContext(jobConfig.getJobId(), null),
                createConsistencyCheckJobItemProgressContext(jobConfig.getJobId())).check("FIXTURE", null);
        String checkKey = "t_order";
        assertTrue(actual.get(checkKey).isMatched());
        assertTrue(actual.get(checkKey).isMatched());
    }
    
    private ClusterPersistRepository getClusterPersistRepository() {
        ContextManager contextManager = PipelineContextManager.getContext(PipelineContextUtils.getContextKey()).getContextManager();
        return (ClusterPersistRepository) contextManager.getMetaDataContexts().getPersistService().getRepository();
    }
    
    private ConsistencyCheckJobItemProgressContext createConsistencyCheckJobItemProgressContext(final String jobId) {
        return new ConsistencyCheckJobItemProgressContext(jobId, 0, "H2");
    }
    
    private MigrationJobConfiguration createJobConfiguration() throws SQLException {
        MigrationJobItemContext jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        initTableData(jobItemContext.getTaskConfig().getDumperContext().getCommonContext().getDataSourceConfig());
        initTableData(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
        return jobItemContext.getJobConfig();
    }
    
    private void initTableData(final PipelineDataSourceConfiguration dataSourceConfig) throws SQLException {
        try (
                PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
                PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dataSourceConfig);
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT(11))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 1), (999, 10)");
        }
    }
}
