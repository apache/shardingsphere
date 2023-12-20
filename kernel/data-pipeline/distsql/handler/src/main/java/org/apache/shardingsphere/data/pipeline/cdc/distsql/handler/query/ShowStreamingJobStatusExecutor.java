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
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.ShowStreamingStatusStatement;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Show streaming job status executor.
 */
public final class ShowStreamingJobStatusExecutor implements QueryableRALExecutor<ShowStreamingStatusStatement> {
    
    private final CDCJobAPI jobAPI = (CDCJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING");
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStreamingStatusStatement sqlStatement) {
        List<CDCJobItemInfo> jobItemInfos = jobAPI.getJobItemInfos(sqlStatement.getJobId());
        long currentTimeMillis = System.currentTimeMillis();
        long startTimeMillis = Long.parseLong(Optional.ofNullable(PipelineJobIdUtils.getElasticJobConfigurationPOJO(sqlStatement.getJobId()).getProps().getProperty("start_time_millis")).orElse("0"));
        return jobItemInfos.stream().map(each -> getRow(each, startTimeMillis, currentTimeMillis)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow getRow(final CDCJobItemInfo jobItemInfo, final long startTimeMillis, final long currentTimeMillis) {
        if (null == jobItemInfo.getStatus()) {
            return new LocalDataQueryResultRow(jobItemInfo.getShardingItem(), "", "", "", "", "", "", jobItemInfo.getErrorMessage());
        }
        String incrementalIdleSeconds = "";
        if (null != jobItemInfo.getIncremental() && jobItemInfo.getIncremental().getIncrementalLatestActiveTimeMillis() > 0) {
            long incrementalLatestActiveTimeMillis = jobItemInfo.getIncremental().getIncrementalLatestActiveTimeMillis();
            long latestActiveTimeMillis = Math.max(startTimeMillis, incrementalLatestActiveTimeMillis);
            incrementalIdleSeconds = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - latestActiveTimeMillis));
        }
        return new LocalDataQueryResultRow(jobItemInfo.getShardingItem(), jobItemInfo.getDataSourceName(), jobItemInfo.getStatus().toString(),
                jobItemInfo.isActive() ? Boolean.TRUE.toString() : Boolean.FALSE.toString(), jobItemInfo.getProcessedRecordsCount(), jobItemInfo.getInventoryFinishedPercentage(),
                incrementalIdleSeconds, jobItemInfo.getConfirmedPosition(), jobItemInfo.getCurrentPosition(), jobItemInfo.getErrorMessage());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("item", "data_source", "status", "active", "processed_records_count", "inventory_finished_percentage", "incremental_idle_seconds", "confirmed_position",
                "current_position", "error_message");
    }
    
    @Override
    public Class<ShowStreamingStatusStatement> getType() {
        return ShowStreamingStatusStatement.class;
    }
}
