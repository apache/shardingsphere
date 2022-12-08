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

package org.apache.shardingsphere.data.pipeline.cdc.core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSimplePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryIncrementalTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

/**
 * CDC job.
 */
@RequiredArgsConstructor
@Slf4j
public final class CDCJob extends AbstractSimplePipelineJob {
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) {
    }
    
    @Override
    protected void doClean() {
    }
    
    @Override
    protected PipelineJobItemContext buildPipelineJobItemContext(final ShardingContext shardingContext) {
        return null;
    }
    
    protected PipelineTasksRunner buildPipelineTasksRunner(final PipelineJobItemContext pipelineJobItemContext) {
        InventoryIncrementalJobItemContext jobItemContext = (InventoryIncrementalJobItemContext) pipelineJobItemContext;
        return new InventoryIncrementalTasksRunner(jobItemContext, jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
}
