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

package org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.executor;

import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseData;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.HintCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.HintCommandExecutor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract hint query command.
 */
public abstract class AbstractHintQueryExecutor<T extends HintCommand> implements HintCommandExecutor<T> {
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    @Override
    public final ResponseHeader execute(final T hintCommand) {
        queryHeaders = createQueryHeaders();
        mergedResult = createMergedResult();
        return new QueryResponseHeader(queryHeaders);
    }
    
    protected abstract List<QueryHeader> createQueryHeaders();
    
    protected abstract MergedResult createMergedResult();
    
    @Override
    public final boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public final QueryResponseData getQueryResponseData() throws SQLException {
        List<Integer> columnTypes = new ArrayList<>(queryHeaders.size());
        List<Object> row = new ArrayList<>(queryHeaders.size());
        for (int i = 0; i < queryHeaders.size(); i++) {
            columnTypes.add(queryHeaders.get(i).getColumnType());
            row.add(mergedResult.getValue(i + 1, Object.class));
        }
        return new QueryResponseData(columnTypes, row);
    }
}
