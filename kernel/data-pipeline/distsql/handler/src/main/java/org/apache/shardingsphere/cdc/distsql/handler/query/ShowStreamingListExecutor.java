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

package org.apache.shardingsphere.cdc.distsql.handler.query;

import org.apache.shardingsphere.cdc.distsql.statement.ShowStreamingListStatement;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show streaming list executor.
 */
public final class ShowStreamingListExecutor implements QueryableRALExecutor<ShowStreamingListStatement> {
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStreamingListStatement sqlStatement) {
        return jobAPI.list(PipelineContextKey.buildForProxy()).stream().map(each -> new LocalDataQueryResultRow(each.getJobMetaData().getJobId(),
                ((TableBasedPipelineJobInfo) each).getDatabaseName(), ((TableBasedPipelineJobInfo) each).getTable(),
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
