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

package org.apache.shardingsphere.data.pipeline.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.distsql.handler.type.ral.update.UpdatableRALExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.data.pipeline.migration.distsql.statement.CheckMigrationStatement;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Check migration job executor.
 */
public final class CheckMigrationJobExecutor implements UpdatableRALExecutor<CheckMigrationStatement> {
    
    private final ConsistencyCheckJobAPI checkJobAPI = new ConsistencyCheckJobAPI(new ConsistencyCheckJobType());
    
    private final PipelineJobType migrationJobType = new MigrationJobType();
    
    @Override
    public void executeUpdate(final CheckMigrationStatement sqlStatement) throws SQLException {
        AlgorithmSegment typeStrategy = sqlStatement.getTypeStrategy();
        String algorithmTypeName = null == typeStrategy ? null : typeStrategy.getName();
        Properties algorithmProps = null == typeStrategy ? null : typeStrategy.getProps();
        String jobId = sqlStatement.getJobId();
        MigrationJobConfiguration jobConfig = new PipelineJobConfigurationManager(migrationJobType).getJobConfiguration(jobId);
        verifyInventoryFinished(jobConfig);
        checkJobAPI.start(new CreateConsistencyCheckJobParameter(jobId, algorithmTypeName, algorithmProps, jobConfig.getSourceDatabaseType(), jobConfig.getTargetDatabaseType()));
    }
    
    private void verifyInventoryFinished(final MigrationJobConfiguration jobConfig) {
        TransmissionJobManager transmissionJobManager = new TransmissionJobManager(migrationJobType);
        ShardingSpherePreconditions.checkState(PipelineJobProgressDetector.isInventoryFinished(jobConfig.getJobShardingCount(), transmissionJobManager.getJobProgress(jobConfig).values()),
                () -> new PipelineInvalidParameterException("Inventory is not finished."));
    }
    
    @Override
    public Class<CheckMigrationStatement> getType() {
        return CheckMigrationStatement.class;
    }
}
