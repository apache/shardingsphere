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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationCheckStatusStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Show migration check status executor.
 */
public final class ShowMigrationCheckStatusExecutor implements QueryableRALExecutor<ShowMigrationCheckStatusStatement> {
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI(new ConsistencyCheckJobType());
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationCheckStatusStatement sqlStatement) {
        List<ConsistencyCheckJobItemInfo> infos = jobAPI.getJobItemInfos(sqlStatement.getJobId());
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (ConsistencyCheckJobItemInfo each : infos) {
            result.add(convert(each));
        }
        return result;
    }
    
    private LocalDataQueryResultRow convert(final ConsistencyCheckJobItemInfo info) {
        String checkResult = null == info.getCheckSuccess() ? "" : info.getCheckSuccess().toString();
        return new LocalDataQueryResultRow(Optional.ofNullable(info.getTableNames()).orElse(""), checkResult, Optional.ofNullable(info.getCheckFailedTableNames()).orElse(""),
                info.isActive() ? Boolean.TRUE.toString() : Boolean.FALSE.toString(),
                String.valueOf(info.getInventoryFinishedPercentage()), info.getInventoryRemainingSeconds(), info.getIncrementalIdleSeconds(),
                Optional.ofNullable(info.getCheckBeginTime()).orElse(""), Optional.ofNullable(info.getCheckEndTime()).orElse(""), info.getDurationSeconds(),
                info.getAlgorithmType(), Optional.ofNullable(info.getAlgorithmProps()).orElse(""), Optional.ofNullable(info.getErrorMessage()).orElse(""));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("tables", "result", "check_failed_tables", "active", "inventory_finished_percentage", "inventory_remaining_seconds", "incremental_idle_seconds",
                "check_begin_time", "check_end_time", "duration_seconds", "algorithm_type", "algorithm_props", "error_message");
    }
    
    @Override
    public Class<ShowMigrationCheckStatusStatement> getType() {
        return ShowMigrationCheckStatusStatement.class;
    }
}
