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

import org.apache.shardingsphere.data.pipeline.api.ConsistencyCheckJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
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
 * Show migration check status query result set.
 */
public final class ShowMigrationCheckStatusQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final ConsistencyCheckJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getConsistencyCheckJobPublicAPI();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        ShowMigrationCheckStatusStatement checkMigrationStatement = (ShowMigrationCheckStatusStatement) sqlStatement;
        ConsistencyCheckJobItemInfo info = JOB_API.getJobItemInfo(checkMigrationStatement.getJobId());
        List<Collection<Object>> result = new LinkedList<>();
        String checkResult = null == info.getCheckSuccess() ? "" : info.getCheckSuccess().toString();
        result.add(Arrays.asList(Optional.ofNullable(info.getTableNames()).orElse(""), checkResult, String.valueOf(info.getFinishedPercentage()),
                info.getRemainingSeconds(),
                Optional.ofNullable(info.getCheckBeginTime()).orElse(""),
                Optional.ofNullable(info.getCheckEndTime()).orElse(""),
                info.getDurationSeconds(), Optional.ofNullable(info.getErrorMessage()).orElse("")));
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
