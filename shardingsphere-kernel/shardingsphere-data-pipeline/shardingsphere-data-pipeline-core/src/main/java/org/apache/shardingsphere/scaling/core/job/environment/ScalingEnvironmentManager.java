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

import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.scaling.core.job.sqlbuilder.ScalingSQLBuilderFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Scaling environment manager.
 */
public final class ScalingEnvironmentManager {
    
    private final PipelineDataSourceFactory dataSourceFactory = new PipelineDataSourceFactory();
    
    /**
     * Reset target table.
     *
     * @param jobContext job context
     * @throws SQLException SQL exception
     */
    // TODO seems it should be removed, dangerous to use
    public void resetTargetTable(final RuleAlteredJobContext jobContext) throws SQLException {
        Collection<String> tables = jobContext.getTaskConfig().getDumperConfig().getTableNameMap().values();
        YamlPipelineDataSourceConfiguration target = jobContext.getJobConfig().getPipelineConfig().getTarget();
        try (PipelineDataSourceWrapper dataSource = dataSourceFactory.newInstance(PipelineDataSourceConfigurationFactory.newInstance(target.getType(), target.getParameter()));
             Connection connection = dataSource.getConnection()) {
            for (String each : tables) {
                String sql = ScalingSQLBuilderFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getTargetDatabaseType()).buildTruncateSQL(each);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.execute();
                }
            }
        }
    }
}
