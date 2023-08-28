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

package org.apache.shardingsphere.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationListStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Show migration list executor.
 */
public final class ShowMigrationListExecutor implements QueryableRALExecutor<ShowMigrationListStatement> {
    
    private final MigrationJobAPI jobAPI = new MigrationJobAPI();
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationListStatement sqlStatement) {
        return jobAPI.list(PipelineContextKey.buildForProxy()).stream().map(each -> new LocalDataQueryResultRow(each.getJobMetaData().getJobId(),
                ((TableBasedPipelineJobInfo) each).getTable(), each.getJobMetaData().getJobItemCount(),
                each.getJobMetaData().isActive() ? Boolean.TRUE.toString() : Boolean.FALSE.toString(),
                each.getJobMetaData().getCreateTime(), each.getJobMetaData().getStopTime())).collect(Collectors.toList());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("id", "tables", "job_item_count", "active", "create_time", "stop_time");
    }
    
    @Override
    public Class<ShowMigrationListStatement> getType() {
        return ShowMigrationListStatement.class;
    }
}
