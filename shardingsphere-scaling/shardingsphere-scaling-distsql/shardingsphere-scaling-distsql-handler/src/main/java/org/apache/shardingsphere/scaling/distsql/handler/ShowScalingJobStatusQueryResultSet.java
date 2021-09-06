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

package org.apache.shardingsphere.scaling.distsql.handler;

import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingJobStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Show scaling job status query result set.
 */
public final class ShowScalingJobStatusQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        data = ScalingAPIFactory.getScalingAPI().getProgress(((ShowScalingJobStatusStatement) sqlStatement).getJobId()).entrySet().stream()
                .map(entry -> {
                    Collection<Object> list = new LinkedList<>();
                    list.add(entry.getKey());
                    if (null != entry.getValue()) {
                        list.add(entry.getValue().getDataSource());
                        list.add(entry.getValue().getStatus());
                        list.add(entry.getValue().getInventoryFinishedPercentage());
                        long latestActiveTimeMillis = entry.getValue().getIncrementalLatestActiveTimeMillis();
                        list.add(latestActiveTimeMillis > 0 ? TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - latestActiveTimeMillis) : 0);
                    }
                    return list;
                }).collect(Collectors.toList()).iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("item", "data_source", "status", "inventory_finished_percentage", "incremental_idle_minutes");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowScalingJobStatusStatement.class.getCanonicalName();
    }
}
