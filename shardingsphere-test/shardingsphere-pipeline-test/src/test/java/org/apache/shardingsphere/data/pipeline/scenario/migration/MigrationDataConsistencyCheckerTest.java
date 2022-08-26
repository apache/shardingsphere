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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.DataConsistencyCalculateAlgorithmFixture;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MigrationDataConsistencyCheckerTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertCountAndDataCheck() throws SQLException {
        Map<String, DataConsistencyCheckResult> actual = new MigrationDataConsistencyChecker(createJobConfiguration(), null).check(new DataConsistencyCalculateAlgorithmFixture());
        assertTrue(actual.get("t_order").getCountCheckResult().isMatched());
        assertThat(actual.get("t_order").getCountCheckResult().getSourceRecordsCount(), is(actual.get("t_order").getCountCheckResult().getTargetRecordsCount()));
        assertTrue(actual.get("t_order").getContentCheckResult().isMatched());
    }
    
    private MigrationJobConfiguration createJobConfiguration() throws SQLException {
        MigrationJobItemContext jobItemContext = PipelineContextUtil.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration());
        initTableData(jobItemContext.getTaskConfig().getDumperConfig().getDataSourceConfig());
        initTableData(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
        return jobItemContext.getJobConfig();
    }
    
    private void initTableData(final PipelineDataSourceConfiguration dataSourceConfig) throws SQLException {
        try (
                Connection connection = new DefaultPipelineDataSourceManager().getDataSource(dataSourceConfig).getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT(11))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 1), (999, 10)");
        }
    }
}
