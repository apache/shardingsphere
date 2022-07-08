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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Scaling environment manager.
 */
@Slf4j
public final class ScalingEnvironmentManager {
    
    /**
     * Cleanup target tables.
     *
     * @param jobConfig job configuration
     * @throws SQLException SQL exception
     */
    // TODO seems it should be removed, dangerous to use
    public void cleanupTargetTables(final RuleAlteredJobConfiguration jobConfig) throws SQLException {
        Collection<String> tables = jobConfig.splitLogicTableNames();
        log.info("cleanupTargetTables, tables={}", tables);
        PipelineDataSourceConfiguration target = jobConfig.getTarget();
        PipelineSQLBuilder pipelineSQLBuilder = PipelineSQLBuilderFactory.getInstance(jobConfig.getTargetDatabaseType());
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(TableNameSchemaNameMapping.convert(jobConfig.getSchemaTablesMap()));
        try (
                PipelineDataSourceWrapper dataSource = PipelineDataSourceFactory.newInstance(PipelineDataSourceConfigurationFactory.newInstance(target.getType(), target.getParameter()));
                Connection connection = dataSource.getConnection()) {
            for (String each : tables) {
                String sql = pipelineSQLBuilder.buildTruncateSQL(tableNameSchemaNameMapping.getSchemaName(each), each);
                log.info("cleanupTargetTables, tableName={}, sql={}", each, sql);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.execute();
                }
            }
        }
    }
}
