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

import org.apache.shardingsphere.data.pipeline.api.MigrationJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query result set for check migration.
 */
public final class CheckMigrationQueryResultSet implements DatabaseDistSQLResultSet {
    
    private static final MigrationJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getMigrationJobPublicAPI();
    
    private Iterator<Collection<Object>> data;
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        CheckMigrationStatement checkMigrationStatement = (CheckMigrationStatement) sqlStatement;
        AlgorithmSegment typeStrategy = checkMigrationStatement.getTypeStrategy();
        Map<String, DataConsistencyCheckResult> checkResultMap = null == typeStrategy
                ? JOB_API.dataConsistencyCheck(checkMigrationStatement.getJobId())
                : JOB_API.dataConsistencyCheck(checkMigrationStatement.getJobId(), typeStrategy.getName(), typeStrategy.getProps());
        data = checkResultMap.entrySet().stream()
                .map(each -> {
                    Collection<Object> result = new LinkedList<>();
                    result.add(each.getKey());
                    result.add(each.getValue().getCountCheckResult().getSourceRecordsCount());
                    result.add(each.getValue().getCountCheckResult().getTargetRecordsCount());
                    result.add(each.getValue().getCountCheckResult().isMatched() + "");
                    result.add(each.getValue().getContentCheckResult().isMatched() + "");
                    return result;
                }).collect(Collectors.toList()).iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table_name", "source_records_count", "target_records_count", "records_count_matched", "records_content_matched");
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
        return CheckMigrationStatement.class.getName();
    }
}
