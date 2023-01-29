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

package org.apache.shardingsphere.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.api.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationCheckStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Result set for show migration check status.
 */
public final class ShowMigrationCheckStatusResultSet implements DatabaseDistSQLResultSet {
    
    private final ConsistencyCheckJobAPI jobAPI = new ConsistencyCheckJobAPI();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShowMigrationCheckStatusStatement checkMigrationStatement = (ShowMigrationCheckStatusStatement) sqlStatement;
        List<ConsistencyCheckJobItemInfo> infos = jobAPI.getJobItemInfos(checkMigrationStatement.getJobId());
        Collection<Collection<Object>> result = new LinkedList<>();
        for (ConsistencyCheckJobItemInfo each : infos) {
            String checkResult = null == each.getCheckSuccess() ? "" : each.getCheckSuccess().toString();
            Collection<Object> item = Arrays.asList(Optional.ofNullable(each.getTableNames()).orElse(""), checkResult,
                    String.valueOf(each.getFinishedPercentage()), each.getRemainingSeconds(),
                    Optional.ofNullable(each.getCheckBeginTime()).orElse(""), Optional.ofNullable(each.getCheckEndTime()).orElse(""),
                    each.getDurationSeconds(), Optional.ofNullable(each.getErrorMessage()).orElse(""));
            result.add(item);
        }
        data = result.iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("tables", "result", "finished_percentage", "remaining_seconds", "check_begin_time", "check_end_time", "duration_seconds", "error_message");
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
        return ShowMigrationCheckStatusStatement.class.getName();
    }
}
