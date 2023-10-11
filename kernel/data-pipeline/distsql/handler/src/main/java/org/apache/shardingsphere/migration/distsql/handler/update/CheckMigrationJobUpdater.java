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

package org.apache.shardingsphere.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Check migration job updater.
 */
public final class CheckMigrationJobUpdater implements RALUpdater<CheckMigrationStatement> {
    
    private final ConsistencyCheckJobAPI checkJobAPI = new ConsistencyCheckJobAPI();
    
    private final MigrationJobAPI migrationJobAPI = new MigrationJobAPI();
    
    @Override
    public void executeUpdate(final String databaseName, final CheckMigrationStatement sqlStatement) throws SQLException {
        AlgorithmSegment typeStrategy = sqlStatement.getTypeStrategy();
        String algorithmTypeName = null == typeStrategy ? null : typeStrategy.getName();
        Properties algorithmProps = null == typeStrategy ? null : typeStrategy.getProps();
        String jobId = sqlStatement.getJobId();
        MigrationJobConfiguration jobConfig = migrationJobAPI.getJobConfiguration(jobId);
        verifyInventoryFinished(jobConfig);
        checkJobAPI.createJobAndStart(new CreateConsistencyCheckJobParameter(jobId, algorithmTypeName, algorithmProps, jobConfig.getSourceDatabaseType(), jobConfig.getTargetDatabaseType()));
    }
    
    private void verifyInventoryFinished(final MigrationJobConfiguration jobConfig) {
        ShardingSpherePreconditions.checkState(PipelineJobProgressDetector.isInventoryFinished(jobConfig.getJobShardingCount(), migrationJobAPI.getJobProgress(jobConfig).values()),
                () -> new PipelineInvalidParameterException("Inventory is not finished."));
    }
    
    @Override
    public Class<CheckMigrationStatement> getType() {
        return CheckMigrationStatement.class;
    }
}
