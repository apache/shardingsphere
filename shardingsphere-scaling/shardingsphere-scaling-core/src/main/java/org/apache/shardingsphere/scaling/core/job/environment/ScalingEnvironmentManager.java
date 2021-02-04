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

package org.apache.shardingsphere.scaling.core.job.environment;

import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceWrapper;
import org.apache.shardingsphere.scaling.core.common.sqlbuilder.ScalingSQLBuilderFactory;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scaling environment manager.
 */
public final class ScalingEnvironmentManager {
    
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();
    
    /**
     * Reset target table.
     *
     * @param jobContext job context
     * @throws SQLException SQL exception
     */
    public void resetTargetTable(final JobContext jobContext) throws SQLException {
        Set<String> tables = jobContext.getTaskConfigs().stream().flatMap(each -> each.getDumperConfig().getTableNameMap().values().stream()).collect(Collectors.toSet());
        try (DataSourceWrapper dataSource = dataSourceFactory.newInstance(jobContext.getJobConfig().getRuleConfig().getTarget().unwrap());
             Connection connection = dataSource.getConnection()) {
            for (String each : tables) {
                String sql = ScalingSQLBuilderFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getDatabaseType()).buildTruncateSQL(each);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.execute();
                }
            }
        }
    }
}
