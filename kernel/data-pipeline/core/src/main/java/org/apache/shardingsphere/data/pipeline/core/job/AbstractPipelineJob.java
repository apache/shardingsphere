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

package org.apache.shardingsphere.data.pipeline.core.job;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract pipeline job.
 */
@Slf4j
@Getter
public abstract class AbstractPipelineJob implements PipelineJob {
    
    @Setter
    private volatile String jobId;
    
    @Setter
    private volatile boolean stopping;
    
    @Setter
    private volatile JobBootstrap jobBootstrap;
    
    private final Map<Integer, PipelineTasksRunner> tasksRunnerMap = new ConcurrentHashMap<>();
    
    protected void runInBackground(final Runnable runnable) {
        new Thread(runnable).start();
    }
    
    @Override
    public Optional<PipelineTasksRunner> getTasksRunner(final int shardingItem) {
        return Optional.ofNullable(tasksRunnerMap.get(shardingItem));
    }
}
