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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor;

import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommandExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintShardingType;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.result.HintShowStatusResult;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Hint show status command executor.
 *
 * @author liya
 */
public final class HintShowStatusExecutor implements HintCommandExecutor {
    
    private List<QueryHeader> queryHeaders;
    
    private Iterator<HintShowStatusResult> queryResults;
    
    @Override
    public BackendResponse execute() {
        queryResults = queryHintStatus();
        queryHeaders = new ArrayList<>(2);
        queryHeaders.add(new QueryHeader("", "", "master_only", "", 5, Types.CHAR, 0));
        queryHeaders.add(new QueryHeader("", "", "sharding_type", "", 255, Types.CHAR, 0));
        return new QueryResponse(queryHeaders);
    }
    
    private Iterator<HintShowStatusResult> queryHintStatus() {
        boolean masterOnly = HintManager.isMasterRouteOnly();
        boolean databaseOnly = HintManager.isDatabaseShardingOnly();
        HintShardingType shardingType = databaseOnly ? HintShardingType.DATABASES_ONLY : HintShardingType.DATABASES_TABLES;
        HintShowStatusResult hintShowStatusResult = new HintShowStatusResult(masterOnly, shardingType);
        return Collections.singletonList(hintShowStatusResult).iterator();
    }
    
    @Override
    public boolean next() {
        return null != queryResults && queryResults.hasNext();
    }
    
    @Override
    public QueryData getQueryData() {
        HintShowStatusResult hintShowStatusResult = queryResults.next();
        List<Object> row = new ArrayList<>(queryHeaders.size());
        row.add(String.valueOf(hintShowStatusResult.isMasterOnly()).toLowerCase());
        row.add(String.valueOf(hintShowStatusResult.getShardingType()).toLowerCase());
        List<Integer> columnTypes = new ArrayList<>(queryHeaders.size());
        columnTypes.add(queryHeaders.get(0).getColumnType());
        columnTypes.add(queryHeaders.get(1).getColumnType());
        return new QueryData(columnTypes, row);
    }
}
