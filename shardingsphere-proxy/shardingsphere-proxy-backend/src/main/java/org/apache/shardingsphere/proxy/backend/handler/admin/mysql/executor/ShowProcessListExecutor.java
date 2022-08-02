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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.BatchYamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.process.event.ShowProcessListResponseEvent;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Show process list executor.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ShowProcessListExecutor implements DatabaseAdminQueryExecutor {
    
    private Collection<String> batchProcessContexts;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    public ShowProcessListExecutor() {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Receive and handle response event.
     *
     * @param event show process list response event
     */
    @Subscribe
    public void receiveProcessListData(final ShowProcessListResponseEvent event) {
        batchProcessContexts = event.getBatchProcessContexts();
    }
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new TransparentMergedResult(getQueryResult());
    }
    
    private QueryResult getQueryResult() {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().post(new ShowProcessListRequestEvent());
        if (null == batchProcessContexts || batchProcessContexts.isEmpty()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        Collection<YamlExecuteProcessContext> processContexts = new LinkedList<>();
        for (String each : batchProcessContexts) {
            processContexts.addAll(YamlEngine.unmarshal(each, BatchYamlExecuteProcessContext.class).getContexts());
        }
        List<MemoryQueryResultDataRow> rows = processContexts.stream().map(processContext -> {
            List<Object> rowValues = new ArrayList<>(8);
            rowValues.add(processContext.getExecutionID());
            rowValues.add(processContext.getUsername());
            rowValues.add(processContext.getHostname());
            rowValues.add(processContext.getDatabaseName());
            rowValues.add("Execute");
            rowValues.add(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - processContext.getStartTimeMillis()));
            int processDoneCount = processContext.getUnitStatuses().stream().map(each -> ExecuteProcessConstants.EXECUTE_STATUS_DONE == each.getStatus() ? 1 : 0).reduce(0, Integer::sum);
            String statePrefix = "Executing ";
            rowValues.add(statePrefix + processDoneCount + "/" + processContext.getUnitStatuses().size());
            String sql = processContext.getSql();
            if (null != sql && sql.length() > 100) {
                sql = sql.substring(0, 100);
            }
            rowValues.add(null != sql ? sql : "");
            return new MemoryQueryResultDataRow(rowValues);
        }).collect(Collectors.toList());
        return new RawMemoryQueryResult(queryResultMetaData, rows);
    }
    
    private QueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = new ArrayList<>();
        columns.add(new RawQueryResultColumnMetaData("", "Id", "Id", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "User", "User", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Host", "Host", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "db", "db", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Command", "Command", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Time", "Time", Types.VARCHAR, "VARCHAR", 10, 0));
        columns.add(new RawQueryResultColumnMetaData("", "State", "State", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Info", "Info", Types.VARCHAR, "VARCHAR", 120, 0));
        return new RawQueryResultMetaData(columns);
    }
}
