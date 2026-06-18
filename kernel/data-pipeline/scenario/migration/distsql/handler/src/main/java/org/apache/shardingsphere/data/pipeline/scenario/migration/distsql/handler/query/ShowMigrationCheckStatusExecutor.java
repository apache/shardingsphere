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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationCheckStatusStatement;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show migration check status executor.
 */
public final class ShowMigrationCheckStatusExecutor implements DistSQLQueryExecutor<ShowMigrationCheckStatusStatement> {
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI(new ConsistencyCheckJobType());
    
    @Override
    public Collection<String> getColumnNames(final ShowMigrationCheckStatusStatement sqlStatement) {
        return Arrays.asList("tables", "result", "check_failed_tables", "ignored_tables", "active", "inventory_finished_percentage", "inventory_remaining_seconds", "incremental_idle_seconds",
                "check_begin_time", "check_end_time", "duration_seconds", "algorithm_type", "algorithm_props", "error_message");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationCheckStatusStatement sqlStatement, final ContextManager contextManager) {
        ConsistencyCheckJobItemInfo jobItemInfo = jobAPI.getJobItemInfo(sqlStatement.getJobId());
        return Collections.singletonList(convert(jobItemInfo));
    }
    
    private LocalDataQueryResultRow convert(final ConsistencyCheckJobItemInfo info) {
        String incrementalIdleSeconds = null == info.getIncrementalIdleSeconds() ? "" : String.valueOf(info.getIncrementalIdleSeconds());
        return new LocalDataQueryResultRow(info.getTableNames(), info.getCheckSuccess(), info.getCheckFailedTableNames(), info.getIgnoredTableNames(), info.isActive(),
                info.getInventoryFinishedPercentage(), info.getInventoryRemainingSeconds(), incrementalIdleSeconds,
                info.getCheckBeginTime(), info.getCheckEndTime(), info.getDurationSeconds(), info.getAlgorithmType(), info.getAlgorithmProps(), info.getErrorMessage());
    }
    
    @Override
    public Class<ShowMigrationCheckStatusStatement> getType() {
        return ShowMigrationCheckStatusStatement.class;
    }
}
