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

package org.apache.shardingsphere.scaling.core.execute.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.shardingsphere.scaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Sync executor group.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SyncExecutorGroup {
    
    private final ExecuteCallback executeCallback;
    
    private final Collection<ShardingScalingExecutor> shardingScalingExecutors = new LinkedList<>();
    
    private Channel channel;
    
    /**
     * Add {@code ShardingScalingExecutor}.
     *
     * @param shardingScalingExecutor sync executor
     */
    public void addSyncExecutor(final ShardingScalingExecutor shardingScalingExecutor) {
        shardingScalingExecutors.add(shardingScalingExecutor);
    }
    
    /**
     * Add all {@code ShardingScalingExecutor}.
     *
     * @param syncExecutors collection of sync executors
     */
    public void addAllSyncExecutor(final Collection<? extends ShardingScalingExecutor> syncExecutors) {
        shardingScalingExecutors.addAll(syncExecutors);
    }
    
    /**
     * Invoked when this group is successful.
     */
    public void onSuccess() {
        channel.close();
        executeCallback.onSuccess();
    }
    
    /**
     * Invoked when this group fails or is canceled.
     *
     * @param throwable throwable
     */
    public void onFailure(final Throwable throwable) {
        for (ShardingScalingExecutor each : shardingScalingExecutors) {
            each.stop();
        }
        channel.close();
        executeCallback.onFailure(throwable);
    }
}
