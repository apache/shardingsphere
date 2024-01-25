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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.type.rql.RQLExecutor;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.DatabaseAwareRQLExecutor;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.DatabaseRuleAwareRQLExecutor;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.GlobalRuleAwareRQLExecutor;
import org.apache.shardingsphere.distsql.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.MetaDataAwareRQLExecutor;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RQL backend handler.
 */
@RequiredArgsConstructor
public final class RQLBackendHandler implements DistSQLBackendHandler {
    
    private final RQLStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseHeader execute() throws SQLException {
        RQLExecutor executor = TypedSPILoader.getService(RQLExecutor.class, sqlStatement.getClass());
        queryHeaders = createQueryHeader(executor.getColumnNames());
        if (executor instanceof DatabaseAwareRQLExecutor) {
            setUpDatabaseAwareExecutor((DatabaseAwareRQLExecutor) executor);
        }
        if (executor instanceof GlobalRuleAwareRQLExecutor) {
            setUpGlobalRuleAwareExecutor((GlobalRuleAwareRQLExecutor) executor);
        }
        if (executor instanceof MetaDataAwareRQLExecutor) {
            ((MetaDataAwareRQLExecutor<?>) executor).setMetaDataContexts(ProxyContext.getInstance().getContextManager().getMetaDataContexts());
        }
        mergedResult = null == mergedResult ? createMergedResult(executor.getRows(sqlStatement)) : mergedResult;
        return new QueryResponseHeader(queryHeaders);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setUpDatabaseAwareExecutor(final DatabaseAwareRQLExecutor executor) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession));
        ((DatabaseAwareRQLExecutor<?>) executor).setDatabase(database);
        if (executor instanceof DatabaseRuleAwareRQLExecutor) {
            Optional<ShardingSphereRule> rule = database.getRuleMetaData().findSingleRule(((DatabaseRuleAwareRQLExecutor) executor).getRuleClass());
            if (rule.isPresent()) {
                ((DatabaseRuleAwareRQLExecutor) executor).setRule(rule.get());
            } else {
                mergedResult = createMergedResult(Collections.emptyList());
            }
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setUpGlobalRuleAwareExecutor(final GlobalRuleAwareRQLExecutor executor) {
        Optional<ShardingSphereRule> rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(executor.getRuleClass());
        if (rule.isPresent()) {
            executor.setRule(rule.get());
        } else {
            mergedResult = createMergedResult(Collections.emptyList());
        }
    }
    
    private List<QueryHeader> createQueryHeader(final Collection<String> columnNames) {
        return columnNames.stream().map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(Collectors.toList());
    }
    
    private MergedResult createMergedResult(final Collection<LocalDataQueryResultRow> rows) {
        return new LocalDataMergedResult(rows);
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        for (int i = 0; i < queryHeaders.size(); i++) {
            cells.add(new QueryResponseCell(queryHeaders.get(i).getColumnType(), mergedResult.getValue(i + 1, Object.class)));
        }
        return new QueryResponseRow(cells);
    }
}
