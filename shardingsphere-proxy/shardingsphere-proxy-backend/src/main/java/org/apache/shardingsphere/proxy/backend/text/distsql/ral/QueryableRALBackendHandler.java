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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.TextQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Queryable RAL backend handler.
 */
public abstract class QueryableRALBackendHandler<E extends RALStatement, R extends QueryableRALBackendHandler> extends RALBackendHandler<E, R> {
    
    private List<QueryHeader> queryHeaders;
    
    private MultipleLocalDataMergedResult mergedResult;
    
    @Override
    protected final ResponseHeader handle(final ContextManager contextManager, final E sqlStatement) throws SQLException {
        queryHeaders = createQueryHeader(getColumnNames());
        mergedResult = createMergedResult(getRows(contextManager));
        return new QueryResponseHeader(queryHeaders);
    }
    
    @Override
    public final boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public final Collection<Object> getRowData() throws SQLException {
        return createQueryResponseRow(queryHeaders.size(), mergedResult).getData();
    }
    
    protected abstract Collection<String> getColumnNames();
    
    protected abstract Collection<List<Object>> getRows(ContextManager contextManager) throws SQLException;
    
    private MultipleLocalDataMergedResult createMergedResult(final Collection<List<Object>> rows) {
        return new MultipleLocalDataMergedResult(rows);
    }
    
    private List<QueryHeader> createQueryHeader(final Collection<String> columnNames) {
        return columnNames.stream()
                .map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false))
                .collect(Collectors.toList());
    }
    
    private QueryResponseRow createQueryResponseRow(final int size, final MultipleLocalDataMergedResult mergedResult) {
        List<QueryResponseCell> cells = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            cells.add(new TextQueryResponseCell(mergedResult.getValue(i + 1, Object.class)));
        }
        return new QueryResponseRow(cells);
    }
}
