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

package org.apache.shardingsphere.scaling.core.check;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.rule.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
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
        ShardingScalingJob shardingScalingJob = mockShardingScalingJob();
        DataConsistencyChecker dataConsistencyChecker = DataConsistencyCheckerFactory.newInstance("H2", shardingScalingJob);
        initTableData(shardingScalingJob.getSyncConfigs().get(0).getDumperConfig().getDataSourceConfig());
        initTableData(shardingScalingJob.getSyncConfigs().get(0).getImporterConfig().getDataSourceConfig());
        Map<String, DataConsistencyCheckResult> resultMap = dataConsistencyChecker.countCheck();
        assertTrue(resultMap.get("t1").isCountValid());
        assertThat(resultMap.get("t1").getSourceCount(), is(resultMap.get("t1").getTargetCount()));
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DataSourceConfiguration dataSourceConfig) {
        DataSource dataSource = new DataSourceManager().getDataSource(dataSourceConfig);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t1");
            statement.execute("CREATE TABLE t1 (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t1 (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @SneakyThrows(IOException.class)
    private ShardingScalingJob mockShardingScalingJob() {
        return ScalingConfigurationUtil.initJob("/config.json");
    }
}
