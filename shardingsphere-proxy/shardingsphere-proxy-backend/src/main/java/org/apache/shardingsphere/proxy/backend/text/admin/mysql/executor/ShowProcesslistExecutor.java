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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.shardingsphere.governance.context.metadata.GovernanceMetaDataContexts;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.type.RawMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.transparent.TransparentMergedResult;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.model.YamlExecuteProcessContext;

/**
 * Show processlist executor.
 */
public final class ShowProcesslistExecutor implements DatabaseAdminQueryExecutor {
    
    private static final RegistryCenterNode REGISTRY_CENTER_NODE = new RegistryCenterNode();
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final BackendConnection backendConnection) {
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new TransparentMergedResult(getQueryResult(backendConnection));
    }
    
    private QueryResult getQueryResult(final BackendConnection backendConnection) {
        if (!ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName()).isComplete()) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getMetaDataContexts();
        if (!(metaDataContexts instanceof GovernanceMetaDataContexts)) {
            return new RawMemoryQueryResult(queryResultMetaData, Collections.emptyList());
        }
        RegistryRepository registryRepository = ((GovernanceMetaDataContexts) metaDataContexts).getRegistryRepository();
        List<String> executionNodeKeys = registryRepository.getChildrenKeys(REGISTRY_CENTER_NODE.getExecutionNodesPath());
        List<YamlExecuteProcessContext> executionNodeValues = executionNodeKeys.stream()
            .map(executionId -> YamlEngine.unmarshal(registryRepository.get(REGISTRY_CENTER_NODE.getExecutionPath(executionId)), YamlExecuteProcessContext.class)).collect(Collectors.toList());
        Grantee grantee = backendConnection.getGrantee();
        List<MemoryQueryResultDataRow> rows = executionNodeValues.stream().map(processContext -> {
            List<Object> rowValues = new ArrayList<>(8);
            rowValues.add(processContext.getExecutionID());
            rowValues.add(grantee.getUsername());
            rowValues.add(grantee.getHostname());
            rowValues.add(backendConnection.getSchemaName());
            rowValues.add("Execute");
            rowValues.add(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - processContext.getStartTimeMillis()));
            int processDoneCount = processContext.getUnitStatuses().stream().map(processUnit -> ExecuteProcessConstants.EXECUTE_STATUS_DONE == processUnit.getStatus() ? 1 : 0).reduce(0, Integer::sum);
            String statePrefix = "Executing ";
            rowValues.add(statePrefix + processDoneCount + "/" + processContext.getUnitStatuses().size());
            // TODO Show original SQL
            rowValues.add("");
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
