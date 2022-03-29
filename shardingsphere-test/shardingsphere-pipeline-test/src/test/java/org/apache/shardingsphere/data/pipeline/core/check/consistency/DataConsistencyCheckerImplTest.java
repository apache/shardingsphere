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

package org.apache.shardingsphere.data.pipeline.core.check.consistency;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataConsistencyCheckerImplTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test
    public void assertCountAndDataCheck() {
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(JobConfigurationBuilder.createJobConfiguration());
        initTableData(jobContext.getTaskConfig().getDumperConfig().getDataSourceConfig());
        initTableData(jobContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
        DataConsistencyChecker dataConsistencyChecker = EnvironmentCheckerFactory.newInstance(jobContext.getJobConfig());
        Map<String, DataConsistencyCheckResult> resultMap = dataConsistencyChecker.checkRecordsCount();
        assertTrue(resultMap.get("t_order").isRecordsCountMatched());
        assertThat(resultMap.get("t_order").getSourceRecordsCount(), is(resultMap.get("t_order").getTargetRecordsCount()));
        Map<String, Boolean> dataCheckResultMap = dataConsistencyChecker.checkRecordsContent(new FixtureDataConsistencyCheckAlgorithm());
        assertTrue(dataCheckResultMap.get("t_order"));
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final PipelineDataSourceConfiguration dataSourceConfig) {
        DataSource dataSource = new PipelineDataSourceManager().getDataSource(dataSourceConfig);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @Test(expected = InvocationTargetException.class)
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertCheckDatabaseTypeSupported() {
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(JobConfigurationBuilder.createJobConfiguration());
        DataConsistencyChecker dataConsistencyChecker = EnvironmentCheckerFactory.newInstance(jobContext.getJobConfig());
        Method method = dataConsistencyChecker.getClass().getDeclaredMethod("checkDatabaseTypeSupportedOrNot", Collection.class, String.class);
        method.setAccessible(true);
        method.invoke(dataConsistencyChecker, Arrays.asList("MySQL", "PostgreSQL"), "H2");
    }
}
