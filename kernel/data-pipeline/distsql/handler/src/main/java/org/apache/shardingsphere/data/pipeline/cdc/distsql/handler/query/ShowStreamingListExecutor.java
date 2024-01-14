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

package org.apache.shardingsphere.data.pipeline.cdc.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.ShowStreamingListStatement;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobType;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.distsql.handler.type.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show streaming list executor.
 */
public final class ShowStreamingListExecutor implements QueryableRALExecutor<ShowStreamingListStatement> {
    
    private final PipelineJobManager pipelineJobManager = new PipelineJobManager(new CDCJobType());
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStreamingListStatement sqlStatement, final ShardingSphereMetaData metaData) {
        return pipelineJobManager.getJobInfos(new PipelineContextKey(InstanceType.PROXY)).stream().map(each -> new LocalDataQueryResultRow(each.getJobMetaData().getJobId(),
                each.getDatabaseName(), each.getTableName(),
                each.getJobMetaData().getJobItemCount(), each.getJobMetaData().isActive() ? Boolean.TRUE.toString() : Boolean.FALSE.toString(),
                each.getJobMetaData().getCreateTime(), Optional.ofNullable(each.getJobMetaData().getStopTime()).orElse(""))).collect(Collectors.toList());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("id", "database", "tables", "job_item_count", "active", "create_time", "stop_time");
    }
    
    @Override
    public Class<ShowStreamingListStatement> getType() {
        return ShowStreamingListStatement.class;
    }
}
