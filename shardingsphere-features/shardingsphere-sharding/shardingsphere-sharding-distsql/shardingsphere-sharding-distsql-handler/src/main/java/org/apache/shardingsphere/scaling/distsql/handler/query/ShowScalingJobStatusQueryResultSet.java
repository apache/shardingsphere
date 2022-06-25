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

package org.apache.shardingsphere.scaling.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Query result set for show scaling job status.
 */
public final class ShowScalingJobStatusQueryResultSet implements DistSQLResultSet {
    
    private static final RuleAlteredJobAPI RULE_ALTERED_JOB_API = RuleAlteredJobAPIFactory.getInstance();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        long currentTimeMillis = System.currentTimeMillis();
        data = RULE_ALTERED_JOB_API.getProgress(((ShowScalingStatusStatement) sqlStatement).getJobId()).entrySet().stream()
                .map(entry -> {
                    Collection<Object> result = new LinkedList<>();
                    result.add(entry.getKey());
                    if (null != entry.getValue()) {
                        result.add(entry.getValue().getDataSource());
                        result.add(entry.getValue().getStatus());
                        result.add(entry.getValue().isActive() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
                        result.add(entry.getValue().getInventoryFinishedPercentage());
                        long latestActiveTimeMillis = entry.getValue().getIncrementalLatestActiveTimeMillis();
                        result.add(latestActiveTimeMillis > 0 ? TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - latestActiveTimeMillis) : 0);
                    } else {
                        result.add("");
                        result.add("");
                        result.add("");
                        result.add("");
                        result.add("");
                    }
                    return result;
                }).collect(Collectors.toList()).iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("item", "data_source", "status", "active", "inventory_finished_percentage", "incremental_idle_seconds");
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
        return ShowScalingStatusStatement.class.getName();
    }
}
