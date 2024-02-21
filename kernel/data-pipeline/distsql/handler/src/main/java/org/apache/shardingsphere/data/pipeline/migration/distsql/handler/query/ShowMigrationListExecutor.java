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

package org.apache.shardingsphere.data.pipeline.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.migration.distsql.statement.queryable.ShowMigrationListStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Show migration list executor.
 */
public final class ShowMigrationListExecutor implements DistSQLQueryExecutor<ShowMigrationListStatement> {
    
    private final PipelineJobManager pipelineJobManager = new PipelineJobManager(new MigrationJobType());
    
    @Override
    public Collection<String> getColumnNames(final ShowMigrationListStatement sqlStatement) {
        return Arrays.asList("id", "tables", "job_item_count", "active", "create_time", "stop_time");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationListStatement sqlStatement, final ContextManager contextManager) {
        return pipelineJobManager.getJobInfos(new PipelineContextKey(InstanceType.PROXY)).stream().map(each -> new LocalDataQueryResultRow(each.getJobMetaData().getJobId(), each.getTableName(),
                each.getJobMetaData().getJobItemCount(), each.getJobMetaData().isActive(), each.getJobMetaData().getCreateTime(), each.getJobMetaData().getStopTime())).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShowMigrationListStatement> getType() {
        return ShowMigrationListStatement.class;
    }
}
