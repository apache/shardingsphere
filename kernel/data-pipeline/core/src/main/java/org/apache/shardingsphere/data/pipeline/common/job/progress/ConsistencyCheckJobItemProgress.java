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

package org.apache.shardingsphere.data.pipeline.common.job.progress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;

import java.util.Map;

/**
 * Data consistency check job item progress.
 */
// TODO move package
@Getter
@RequiredArgsConstructor
@ToString
public final class ConsistencyCheckJobItemProgress implements PipelineJobItemProgress {
    
    @Setter
    private JobStatus status = JobStatus.RUNNING;
    
    private final String tableNames;
    
    private final String ignoredTableNames;
    
    private final Long checkedRecordsCount;
    
    private final Long recordsCount;
    
    private final Long checkBeginTimeMillis;
    
    private final Long checkEndTimeMillis;
    
    private final Map<String, Object> tableCheckPositions;
}
