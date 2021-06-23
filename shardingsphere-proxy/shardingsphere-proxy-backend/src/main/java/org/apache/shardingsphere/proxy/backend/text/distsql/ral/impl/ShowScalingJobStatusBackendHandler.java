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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingJobStatusStatement;

import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Show scaling job status backend handler.
 */
public final class ShowScalingJobStatusBackendHandler implements TextProtocolBackendHandler {
    
    private final ShowScalingJobStatusStatement sqlStatement;
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    private final List<QueryHeader> queryHeaders;
    
    private Iterator<Map<String, Object>> data;
    
    public ShowScalingJobStatusBackendHandler(final ShowScalingJobStatusStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        queryHeaders = getQueryHeader();
    }
    
    private List<QueryHeader> getQueryHeader() {
        List<QueryHeader> result = Lists.newArrayList();
        result.add(new QueryHeader("", "", "item", "", Types.BIGINT, "BIGINT", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "data_source", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "status", "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "inventory_finished_percentage", "", Types.TINYINT, "TINYINT", 255, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "incremental_delay_milliseconds", "", Types.BIGINT, "BIGINT", 255, 0, false, false, false, false));
        return result;
    }
    
    @Override
    public ResponseHeader execute() {
        loadData();
        return new QueryResponseHeader(queryHeaders);
    }
    
    private void loadData() {
        data = scalingAPI.getProgress(sqlStatement.getJobId()).entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("item", entry.getKey());
                    if (null != entry.getValue()) {
                        map.put("status", entry.getValue().getStatus());
                        map.put("data_source", entry.getValue().getDataSource());
                        map.put("inventory_finished_percentage", entry.getValue().getInventoryFinishedPercentage());
                        map.put("incremental_delay_milliseconds", entry.getValue().getIncrementalDelayMilliseconds());
                    }
                    return map;
                })
                .collect(Collectors.toList())
                .iterator();
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Map<String, Object> next = data.next();
        return queryHeaders.stream()
                .map(each -> next.get(each.getColumnLabel()))
                .collect(Collectors.toList());
    }
}
