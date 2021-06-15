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

package org.apache.shardingsphere.scaling.core.job.check;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AbstractDataConsistencyCheckerTest {
    
    @Test
    public void assertCountCheck() {
        JobContext jobContext = new JobContext(ResourceUtil.mockJobConfig());
        DataConsistencyChecker dataConsistencyChecker = EnvironmentCheckerFactory.newInstance(jobContext);
        initTableData(jobContext.getTaskConfigs().get(0).getDumperConfig().getDataSourceConfig());
        initTableData(jobContext.getTaskConfigs().get(0).getImporterConfig().getDataSourceConfig());
        Map<String, DataConsistencyCheckResult> resultMap = dataConsistencyChecker.countCheck();
        assertTrue(resultMap.get("t_order").isCountValid());
        assertThat(resultMap.get("t_order").getSourceCount(), is(resultMap.get("t_order").getTargetCount()));
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final ScalingDataSourceConfiguration dataSourceConfig) {
        DataSource dataSource = new DataSourceManager().getDataSource(dataSourceConfig);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
}
