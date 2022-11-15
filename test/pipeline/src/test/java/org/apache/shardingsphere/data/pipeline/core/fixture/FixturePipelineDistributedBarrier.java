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

package org.apache.shardingsphere.data.pipeline.core.fixture;

import org.apache.shardingsphere.data.pipeline.core.api.PipelineDistributedBarrier;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.concurrent.TimeUnit;

public final class FixturePipelineDistributedBarrier implements PipelineDistributedBarrier {
    
    @Override
    public void register(final String barrierPath, final int totalCount) {
    }
    
    @Override
    public void persistEphemeralChildrenNode(final String barrierPath, final int shardingItem) {
    }
    
    @Override
    public void unregister(final String barrierPath) {
    }
    
    @Override
    public boolean await(final String barrierPath, final long timeout, final TimeUnit timeUnit) {
        return false;
    }
    
    @Override
    public void notifyChildrenNodeCountCheck(final DataChangedEvent event) {
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
