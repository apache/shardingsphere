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

package org.apache.shardingsphere.data.pipeline.cdc.core.task;

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;

import java.util.Collection;

/**
 * CDC tasks runner.
 */
public final class CDCTasksRunner implements PipelineTasksRunner {
    
    @Getter
    private final TransmissionJobItemContext jobItemContext;
    
    private final Collection<PipelineTask> inventoryTasks;
    
    private final Collection<PipelineTask> incrementalTasks;
    
    public CDCTasksRunner(final TransmissionJobItemContext jobItemContext) {
        this.jobItemContext = jobItemContext;
        inventoryTasks = jobItemContext.getInventoryTasks();
        incrementalTasks = jobItemContext.getIncrementalTasks();
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        for (PipelineTask each : inventoryTasks) {
            each.stop();
        }
        for (PipelineTask each : incrementalTasks) {
            each.stop();
        }
    }
}
