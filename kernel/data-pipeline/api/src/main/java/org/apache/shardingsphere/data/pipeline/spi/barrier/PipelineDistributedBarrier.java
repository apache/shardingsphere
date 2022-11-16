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

package org.apache.shardingsphere.data.pipeline.spi.barrier;

import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

import java.util.concurrent.TimeUnit;

/**
 * Pipeline distributed barrier.
 */
@SingletonSPI
public interface PipelineDistributedBarrier extends RequiredSPI {
    
    /**
     * Register distributed barrier.
     *
     * @param barrierPath barrier path
     * @param totalCount total count
     */
    void register(String barrierPath, int totalCount);
    
    /**
     * Persist ephemeral children node.
     *
     * @param barrierPath barrier path
     * @param shardingItem sharding item
     */
    void persistEphemeralChildrenNode(String barrierPath, int shardingItem);
    
    /**
     * Persist ephemeral children node.
     *
     * @param barrierPath barrier path
     */
    void unregister(String barrierPath);
    
    /**
     * Await barrier path all children node is ready.
     *
     * @param barrierPath barrier path
     * @param timeout timeout
     * @param timeUnit time unit
     * @return true if the count reached zero and false if the waiting time elapsed before the count reached zero
     */
    boolean await(String barrierPath, long timeout, TimeUnit timeUnit);
    
    /**
     * notify children node count check.
     *
     * @param nodePath node path
     */
    void notifyChildrenNodeCountCheck(String nodePath);
}
