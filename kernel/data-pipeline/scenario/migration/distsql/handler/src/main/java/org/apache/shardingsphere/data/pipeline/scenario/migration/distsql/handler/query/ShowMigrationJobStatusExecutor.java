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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.core.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationStatusStatement;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Show migration job status executor.
 */
public final class ShowMigrationJobStatusExecutor implements DistSQLQueryExecutor<ShowMigrationStatusStatement> {
    
    @Override
    public Collection<String> getColumnNames(final ShowMigrationStatusStatement sqlStatement) {
        return Arrays.asList("item", "data_source", "tables", "status", "active", "processed_records_count", "inventory_finished_percentage", "incremental_idle_seconds", "error_message");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationStatusStatement sqlStatement, final ContextManager contextManager) {
        Collection<TransmissionJobItemInfo> jobItemInfos = new TransmissionJobManager(new MigrationJobType()).getJobItemInfos(sqlStatement.getJobId());
        long currentTimeMillis = System.currentTimeMillis();
        return jobItemInfos.stream().map(each -> getRow(each, currentTimeMillis)).collect(Collectors.toList());
    }
    
    private LocalDataQueryResultRow getRow(final TransmissionJobItemInfo jobItemInfo, final long currentTimeMillis) {
        TransmissionJobItemProgress jobItemProgress = jobItemInfo.getJobItemProgress();
        if (null == jobItemProgress) {
            return new LocalDataQueryResultRow(jobItemInfo.getShardingItem(), "", "", "", "", "", "", "", jobItemInfo.getErrorMessage());
        }
        return new LocalDataQueryResultRow(jobItemInfo.getShardingItem(), jobItemProgress.getDataSourceName(), jobItemInfo.getTableNames(), jobItemProgress.getStatus(), jobItemProgress.isActive(),
                jobItemProgress.getProcessedRecordsCount(), jobItemInfo.getInventoryFinishedPercentage(), getIncrementalIdleSeconds(jobItemProgress, jobItemInfo, currentTimeMillis),
                jobItemInfo.getErrorMessage());
    }
    
    private Optional<Long> getIncrementalIdleSeconds(final TransmissionJobItemProgress jobItemProgress, final TransmissionJobItemInfo jobItemInfo, final long currentTimeMillis) {
        if (!jobItemProgress.isActive()) {
            return Optional.empty();
        }
        if (jobItemProgress.getIncremental().getIncrementalLatestActiveTimeMillis() > 0L) {
            long latestActiveTimeMillis = Math.max(jobItemInfo.getStartTimeMillis(), jobItemProgress.getIncremental().getIncrementalLatestActiveTimeMillis());
            return Optional.of(TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - latestActiveTimeMillis));
        }
        return Optional.empty();
    }
    
    @Override
    public Class<ShowMigrationStatusStatement> getType() {
        return ShowMigrationStatusStatement.class;
    }
}
