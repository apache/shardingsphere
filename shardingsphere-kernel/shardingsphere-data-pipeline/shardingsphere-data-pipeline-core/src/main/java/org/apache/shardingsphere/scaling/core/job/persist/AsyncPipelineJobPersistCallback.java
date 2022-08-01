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

package org.apache.shardingsphere.scaling.core.job.persist;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.job.persist.PipelineJobPersistCallback;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobPersistService;

/**
 * Async job process persist callback.
 */
@RequiredArgsConstructor
public final class AsyncPipelineJobPersistCallback implements PipelineJobPersistCallback {
    
    private final String jobId;
    
    private final int shardingItem;
    
    @Override
    public String getJobId() {
        return jobId;
    }
    
    @Override
    public int getShardingItem() {
        return shardingItem;
    }
    
    @Override
    public void pushPersistEvent() {
        RuleAlteredJobPersistService.triggerPersist(jobId, shardingItem);
    }
}
