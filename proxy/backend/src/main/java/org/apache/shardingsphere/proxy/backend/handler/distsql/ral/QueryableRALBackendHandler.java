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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Queryable RAL backend handler.
 * 
 * @param <E> type of RAL statement
 */
public abstract class QueryableRALBackendHandler<E extends RALStatement> extends RALBackendHandler<E> {
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    @Override
    public final ResponseHeader execute() {
        queryHeaders = createQueryHeader();
        mergedResult = createMergedResult();
        return new QueryResponseHeader(queryHeaders);
    }
    
    private List<QueryHeader> createQueryHeader() {
        return getColumnNames().stream().map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(Collectors.toList());
    }
    
    private MergedResult createMergedResult() {
        return new LocalDataMergedResult(getRows(ProxyContext.getInstance().getContextManager()));
    }
    
    @Override
    public final boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public final QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        for (int i = 0; i < queryHeaders.size(); i++) {
            cells.add(new QueryResponseCell(queryHeaders.get(i).getColumnType(), mergedResult.getValue(i + 1, Object.class)));
        }
        return new QueryResponseRow(cells);
    }
    
    protected abstract Collection<String> getColumnNames();
    
    protected abstract Collection<LocalDataQueryResultRow> getRows(ContextManager contextManager);
}
