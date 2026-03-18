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

import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StopMigrationStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Stop migration executor.
 */
public final class StopMigrationExecutor implements DistSQLUpdateExecutor<StopMigrationStatement> {
    
    private final PipelineJobManager jobManager = new PipelineJobManager(new MigrationJobType());
    
    @Override
    public void executeUpdate(final StopMigrationStatement sqlStatement, final ContextManager contextManager) {
        jobManager.stop(sqlStatement.getJobId());
    }
    
    @Override
    public Class<StopMigrationStatement> getType() {
        return StopMigrationStatement.class;
    }
}
