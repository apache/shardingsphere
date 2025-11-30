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

import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.core.pojo.CDCJobItemInfo;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingStatusStatement;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Show streaming job status executor.
 */
public final class ShowStreamingJobStatusExecutor implements DistSQLQueryExecutor<ShowStreamingStatusStatement> {
    
    private final CDCJobAPI jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
    
    @Override
    public Collection<String> getColumnNames(final ShowStreamingStatusStatement sqlStatement) {
        return Arrays.asList("item", "data_source", "status", "active", "processed_records_count", "inventory_finished_percentage", "incremental_idle_seconds", "confirmed_position",
                "current_position", "error_message");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStreamingStatusStatement sqlStatement, final ContextManager contextManager) {
        Collection<CDCJobItemInfo> jobItemInfos = jobAPI.getJobItemInfos(sqlStatement.getJobId());
        long currentTimeMillis = System.currentTimeMillis();
        return jobItemInfos.stream().map(each -> getRow(each, currentTimeMillis)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow getRow(final CDCJobItemInfo cdcJobItemInfo, final long currentTimeMillis) {
        TransmissionJobItemInfo transmissionJobItemInfo = cdcJobItemInfo.getTransmissionJobItemInfo();
        TransmissionJobItemProgress jobItemProgress = transmissionJobItemInfo.getJobItemProgress();
        if (null == jobItemProgress) {
            return new LocalDataQueryResultRow(transmissionJobItemInfo.getShardingItem(), "", "", "", "", "", "", "", "", transmissionJobItemInfo.getErrorMessage());
        }
        return new LocalDataQueryResultRow(transmissionJobItemInfo.getShardingItem(), jobItemProgress.getDataSourceName(), jobItemProgress.getStatus(), jobItemProgress.isActive(),
                jobItemProgress.getProcessedRecordsCount(), transmissionJobItemInfo.getInventoryFinishedPercentage(),
                getIncrementalIdleSeconds(jobItemProgress, transmissionJobItemInfo, currentTimeMillis), cdcJobItemInfo.getConfirmedPosition(), cdcJobItemInfo.getCurrentPosition(),
                transmissionJobItemInfo.getErrorMessage());
    }
    
    private static Optional<Long> getIncrementalIdleSeconds(final TransmissionJobItemProgress jobItemProgress, final TransmissionJobItemInfo transmissionJobItemInfo, final long currentTimeMillis) {
        if (jobItemProgress.getIncremental().getIncrementalLatestActiveTimeMillis() > 0L) {
            long latestActiveTimeMillis = Math.max(transmissionJobItemInfo.getStartTimeMillis(), jobItemProgress.getIncremental().getIncrementalLatestActiveTimeMillis());
            return Optional.of(TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - latestActiveTimeMillis));
        }
        return Optional.empty();
    }
    
    @Override
    public Class<ShowStreamingStatusStatement> getType() {
        return ShowStreamingStatusStatement.class;
    }
}
