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

package org.apache.shardingsphere.data.pipeline.core.job.progress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;

import java.util.Map;

/**
 * Data consistency check job item progress.
 */
@RequiredArgsConstructor
@Getter
@ToString
// TODO Refactor structure, List<TableProgress>
public final class ConsistencyCheckJobItemProgress implements PipelineJobItemProgress {
    
    private final String tableNames;
    
    private final String ignoredTableNames;
    
    private final Long checkedRecordsCount;
    
    private final Long recordsCount;
    
    private final Long checkBeginTimeMillis;
    
    private final Long checkEndTimeMillis;
    
    private final Map<String, Object> sourceTableCheckPositions;
    
    private final Map<String, Object> targetTableCheckPositions;
    
    private final String sourceDatabaseType;
    
    @Setter
    private JobStatus status = JobStatus.RUNNING;
    
    public ConsistencyCheckJobItemProgress(final ConsistencyCheckJobItemProgressContext context) {
        tableNames = String.join(",", context.getTableNames());
        ignoredTableNames = String.join(",", context.getIgnoredTableNames());
        checkedRecordsCount = context.getCheckedRecordsCount().get();
        recordsCount = context.getRecordsCount();
        checkBeginTimeMillis = context.getCheckBeginTimeMillis();
        checkEndTimeMillis = context.getCheckEndTimeMillis();
        sourceTableCheckPositions = context.getSourceTableCheckPositions();
        targetTableCheckPositions = context.getTargetTableCheckPositions();
        sourceDatabaseType = context.getSourceDatabaseType();
    }
}
