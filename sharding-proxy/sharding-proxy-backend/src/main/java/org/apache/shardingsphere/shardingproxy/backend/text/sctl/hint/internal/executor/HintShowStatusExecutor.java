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

import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommandExecutor;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hint show status command executor.
 *
 * @author liya
 */
public final class HintShowStatusExecutor implements HintCommandExecutor {
    
    private List<QueryHeader> queryHeaders;
    
    private Iterator<Object> queryResults;
    
    @Override
    public BackendResponse execute() {
        // todo 类型 和 长度 需要调整
        List<Object> list = new ArrayList<>(1);
        list.add(1);
        queryResults = list.iterator();
        queryHeaders = new ArrayList<>(4);
        queryHeaders.add(new QueryHeader("", "", "master_only", "", 1, Types.BIT, 0));
        queryHeaders.add(new QueryHeader("", "", "database_only", "", 1, Types.BIT, 0));
        queryHeaders.add(new QueryHeader("", "", "database_sharding_values", "", 255, Types.CHAR, 0));
        queryHeaders.add(new QueryHeader("", "", "table_sharding_values", "", 255, Types.CHAR, 0));
        return new QueryResponse(queryHeaders);
    }
    
    @Override
    public boolean next() {
        return null != queryResults && queryResults.hasNext();
    }
    
    @Override
    public QueryData getQueryData() {
        queryResults.next();
        List<Object> row = new ArrayList<>(queryHeaders.size());
        row.add(1);
        row.add(0);
        row.add("value1,value2");
        row.add("value3,value4");
        List<Integer> columnTypes = new ArrayList<>(queryHeaders.size());
        columnTypes.add(queryHeaders.get(0).getColumnType());
        columnTypes.add(queryHeaders.get(1).getColumnType());
        columnTypes.add(queryHeaders.get(2).getColumnType());
        columnTypes.add(queryHeaders.get(3).getColumnType());
        return new QueryData(columnTypes, row);
    }
}
