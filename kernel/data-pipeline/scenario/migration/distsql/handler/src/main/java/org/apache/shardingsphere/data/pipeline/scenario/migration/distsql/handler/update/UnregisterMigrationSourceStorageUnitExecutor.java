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

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.UnregisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Unregister migration source storage unit executor.
 */
public final class UnregisterMigrationSourceStorageUnitExecutor implements DistSQLUpdateExecutor<UnregisterMigrationSourceStorageUnitStatement> {
    
    private final MigrationJobAPI jobAPI = (MigrationJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION");
    
    @Override
    public void executeUpdate(final UnregisterMigrationSourceStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        jobAPI.dropMigrationSourceResources(new PipelineContextKey(InstanceType.PROXY), sqlStatement.getNames());
    }
    
    @Override
    public Class<UnregisterMigrationSourceStorageUnitStatement> getType() {
        return UnregisterMigrationSourceStorageUnitStatement.class;
    }
}
