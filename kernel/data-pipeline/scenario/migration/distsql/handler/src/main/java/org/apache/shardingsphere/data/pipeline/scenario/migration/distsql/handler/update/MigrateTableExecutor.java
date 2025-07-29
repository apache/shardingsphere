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

import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.job.MissingRequiredTargetDatabaseException;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationSourceTargetEntry;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.segment.MigrationSourceTargetSegment;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.MigrateTableStatement;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Migrate table executor.
 */
@Setter
@DistSQLExecutorClusterModeRequired
public final class MigrateTableExecutor implements DistSQLUpdateExecutor<MigrateTableStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final MigrateTableStatement sqlStatement, final ContextManager contextManager) {
        String targetDatabaseName = null == sqlStatement.getTargetDatabaseName() ? database.getName() : sqlStatement.getTargetDatabaseName();
        ShardingSpherePreconditions.checkState(contextManager.getMetaDataContexts().getMetaData().containsDatabase(targetDatabaseName),
                () -> new MissingRequiredTargetDatabaseException(targetDatabaseName));
        MigrationJobAPI jobAPI = (MigrationJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION");
        jobAPI.schedule(new PipelineContextKey(InstanceType.PROXY), getMigrationSourceTargetEntries(sqlStatement), targetDatabaseName);
    }
    
    private Collection<MigrationSourceTargetEntry> getMigrationSourceTargetEntries(final MigrateTableStatement sqlStatement) {
        Collection<MigrationSourceTargetEntry> result = new LinkedList<>();
        for (MigrationSourceTargetSegment each : sqlStatement.getSourceTargetEntries()) {
            DataNode dataNode = new DataNode(each.getSourceDatabaseName(), each.getSourceTableName());
            dataNode.setSchemaName(each.getSourceSchemaName());
            result.add(new MigrationSourceTargetEntry(dataNode, each.getTargetTableName()));
        }
        return result;
    }
    
    @Override
    public Class<MigrateTableStatement> getType() {
        return MigrateTableStatement.class;
    }
}
