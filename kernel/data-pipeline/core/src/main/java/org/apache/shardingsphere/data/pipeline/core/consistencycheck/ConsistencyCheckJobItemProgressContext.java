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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Consistency check job item progress context.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class ConsistencyCheckJobItemProgressContext implements PipelineJobProgressListener {
    
    private final String jobId;
    
    private final int shardingItem;
    
    private final Collection<String> tableNames = new CopyOnWriteArraySet<>();
    
    private final Collection<String> ignoredTableNames = new CopyOnWriteArraySet<>();
    
    private volatile long recordsCount;
    
    private final AtomicLong checkedRecordsCount = new AtomicLong(0L);
    
    private final long checkBeginTimeMillis = System.currentTimeMillis();
    
    private volatile Long checkEndTimeMillis;
    
    private final List<TableCheckRangePosition> tableCheckRangePositions = new ArrayList<>();
    
    private final String sourceDatabaseType;
    
    @Override
    public void onProgressUpdated(final PipelineJobUpdateProgress updateProgress) {
        checkedRecordsCount.addAndGet(updateProgress.getProcessedRecordsCount());
        PipelineJobProgressPersistService.notifyPersist(jobId, shardingItem);
    }
}
