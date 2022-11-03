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

package org.apache.shardingsphere.data.pipeline.core.util;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Pipeline distributed barrier.
 */
@Slf4j
public final class PipelineDistributedBarrier {
    
    private static final PipelineDistributedBarrier INSTANCE = new PipelineDistributedBarrier();
    
    private static final LazyInitializer<ClusterPersistRepository> REPOSITORY_LAZY_INITIALIZER = new LazyInitializer<ClusterPersistRepository>() {
        
        @Override
        protected ClusterPersistRepository initialize() {
            return (ClusterPersistRepository) PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository();
        }
    };
    
    private final Map<String, InnerCountDownLatchHolder> countDownLatchMap = new ConcurrentHashMap<>();
    
    @SneakyThrows(ConcurrentException.class)
    private static ClusterPersistRepository getRepository() {
        return REPOSITORY_LAZY_INITIALIZER.get();
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static PipelineDistributedBarrier getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register count down latch.
     *
     * @param barrierPath barrier path
     * @param totalCount total count
     */
    public void register(final String barrierPath, final int totalCount) {
        getRepository().persist(barrierPath, "");
        countDownLatchMap.computeIfAbsent(barrierPath, k -> new InnerCountDownLatchHolder(totalCount, new CountDownLatch(1)));
    }
    
    /**
     * Persist ephemeral children node.
     *
     * @param barrierPath barrier path
     * @param shardingItem sharding item
     */
    public void persistEphemeralChildrenNode(final String barrierPath, final int shardingItem) {
        if (!getRepository().isExisted(barrierPath)) {
            log.info("barrier path {} not exist, ignore", barrierPath);
            return;
        }
        String key = String.join("/", barrierPath, Integer.toString(shardingItem));
        getRepository().delete(key);
        getRepository().persistEphemeral(key, "");
    }
    
    /**
     * Persist ephemeral children node.
     *
     * @param barrierPath barrier path
     */
    public void unregister(final String barrierPath) {
        getRepository().delete(String.join("/", barrierPath));
        InnerCountDownLatchHolder holder = countDownLatchMap.remove(barrierPath);
        if (null != holder) {
            holder.getCountDownLatch().countDown();
        }
    }
    
    /**
     * Await barrier path all children node is ready.
     *
     * @param barrierPath barrier path
     * @param timeout timeout
     * @param timeUnit time unit
     * @return true if the count reached zero and false if the waiting time elapsed before the count reached zero
     */
    public boolean await(final String barrierPath, final long timeout, final TimeUnit timeUnit) {
        InnerCountDownLatchHolder holder = countDownLatchMap.get(barrierPath);
        if (null == holder) {
            return false;
        }
        try {
            boolean result = holder.getCountDownLatch().await(timeout, timeUnit);
            if (!result) {
                log.info("await timeout, barrier path: {}, timeout: {}, time unit: {}", barrierPath, timeout, timeUnit);
            }
            return result;
        } catch (final InterruptedException ignored) {
        }
        return false;
    }
    
    /**
     * Check child node count equal sharding count.
     *
     * @param event event
     */
    public void checkChildrenNodeCount(final DataChangedEvent event) {
        if (Strings.isNullOrEmpty(event.getKey())) {
            return;
        }
        String barrierPath = event.getKey().substring(0, event.getKey().lastIndexOf("/"));
        InnerCountDownLatchHolder holder = countDownLatchMap.get(barrierPath);
        if (null == holder) {
            return;
        }
        List<String> childrenKeys = getRepository().getChildrenKeys(barrierPath);
        log.info("children keys: {}, total count: {}", childrenKeys, holder.getTotalCount());
        if (childrenKeys.size() == holder.getTotalCount()) {
            holder.getCountDownLatch().countDown();
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private static class InnerCountDownLatchHolder {
        
        private final int totalCount;
        
        private final CountDownLatch countDownLatch;
    }
}
