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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.CheckMigrationStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Properties;

/**
 * Check migration job executor.
 */
public final class CheckMigrationJobExecutor implements DistSQLUpdateExecutor<CheckMigrationStatement> {
    
    private final ConsistencyCheckJobAPI checkJobAPI = new ConsistencyCheckJobAPI(new ConsistencyCheckJobType());
    
    private final MigrationJobType jobType = new MigrationJobType();
    
    @Override
    public void executeUpdate(final CheckMigrationStatement sqlStatement, final ContextManager contextManager) {
        AlgorithmSegment typeStrategy = sqlStatement.getTypeStrategy();
        String algorithmTypeName = null == typeStrategy ? null : typeStrategy.getName();
        Properties algorithmProps = null == typeStrategy ? null : typeStrategy.getProps();
        String jobId = sqlStatement.getJobId();
        MigrationJobConfiguration jobConfig = new PipelineJobConfigurationManager(jobType.getOption()).getJobConfiguration(jobId);
        verifyInventoryFinished(jobConfig);
        checkJobAPI.start(new CreateConsistencyCheckJobParameter(jobId, algorithmTypeName, algorithmProps, jobConfig.getSourceDatabaseType(), jobConfig.getTargetDatabaseType()));
    }
    
    private void verifyInventoryFinished(final MigrationJobConfiguration jobConfig) {
        TransmissionJobManager transmissionJobManager = new TransmissionJobManager(jobType);
        ShardingSpherePreconditions.checkState(PipelineJobProgressDetector.isInventoryFinished(jobConfig.getJobShardingCount(), transmissionJobManager.getJobProgress(jobConfig).values()),
                () -> new PipelineInvalidParameterException("Inventory is not finished."));
    }
    
    @Override
    public Class<CheckMigrationStatement> getType() {
        return CheckMigrationStatement.class;
    }
}
