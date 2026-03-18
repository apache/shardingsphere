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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Pipeline distributed barrier.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineDistributedBarrier {
    
    private static final Map<PipelineContextKey, PipelineDistributedBarrier> INSTANCE_MAP = new ConcurrentHashMap<>();
    
    private final PipelineContextKey contextKey;
    
    private final LazyInitializer<ClusterPersistRepository> repositoryLazyInitializer = new PersistRepositoryLazyInitializer();
    
    private final Map<String, InnerCountDownLatchHolder> countDownLatchHolders = new ConcurrentHashMap<>();
    
    /**
     * Get instance.
     *
     * @param contextKey context key
     * @return instance
     */
    public static PipelineDistributedBarrier getInstance(final PipelineContextKey contextKey) {
        PipelineDistributedBarrier result = INSTANCE_MAP.get(contextKey);
        if (null != result) {
            return result;
        }
        INSTANCE_MAP.computeIfAbsent(contextKey, PipelineDistributedBarrier::new);
        return INSTANCE_MAP.get(contextKey);
    }
    
    @SneakyThrows(ConcurrentException.class)
    private ClusterPersistRepository getRepository() {
        return repositoryLazyInitializer.get();
    }
    
    /**
     * Register distributed barrier.
     *
     * @param barrierPath barrier path
     * @param totalCount total count
     */
    public void register(final String barrierPath, final int totalCount) {
        log.info("Register, barrier path: {}, total count: {}", barrierPath, totalCount);
        getRepository().persist(barrierPath, "");
        countDownLatchHolders.computeIfAbsent(barrierPath, key -> new InnerCountDownLatchHolder(totalCount));
    }
    
    /**
     * Persist ephemeral children node.
     *
     * @param barrierPath barrier path
     * @param shardingItem sharding item
     */
    public void persistEphemeralChildrenNode(final String barrierPath, final int shardingItem) {
        if (!getRepository().isExisted(barrierPath)) {
            log.info("Persist ephemeral children node, barrier path not existed: {}, sharding item: {}", barrierPath, shardingItem);
            return;
        }
        log.info("Persist ephemeral children node, barrier path: {}, sharding item: {}", barrierPath, shardingItem);
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
        log.info("Unregister, barrier path: {}", barrierPath);
        getRepository().delete(barrierPath);
        InnerCountDownLatchHolder holder = countDownLatchHolders.remove(barrierPath);
        if (null != holder) {
            holder.releaseLatch();
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
        InnerCountDownLatchHolder holder = countDownLatchHolders.get(barrierPath);
        if (null == holder) {
            log.info("Await failed, barrier path not registered: {}", barrierPath);
            return false;
        }
        try {
            boolean result = holder.awaitLatchReleasing(timeout, timeUnit);
            if (result) {
                log.info("Await success, barrier path: {}", barrierPath);
            } else {
                log.warn("Await timeout, barrier path: {}, timeout: {}, time unit: {}", barrierPath, timeout, timeUnit);
            }
            return result;
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
    
    /**
     * notify children node count check.
     *
     * @param nodePath node path
     */
    public void notifyChildrenNodeCountCheck(final String nodePath) {
        if (Strings.isNullOrEmpty(nodePath)) {
            log.info("Notify children node count check, node path is null or empty");
            return;
        }
        String barrierPath = nodePath.substring(0, nodePath.lastIndexOf('/'));
        InnerCountDownLatchHolder holder = countDownLatchHolders.get(barrierPath);
        if (null != holder) {
            int childrenSize = getRepository().getChildrenKeys(barrierPath).size();
            log.info("Notify children node count check, barrier path: {}, children size: {}, total count: {}", barrierPath, childrenSize, holder.getTotalCount());
            if (childrenSize == holder.getTotalCount()) {
                holder.releaseLatch();
            }
        }
    }
    
    @RequiredArgsConstructor
    private static final class InnerCountDownLatchHolder {
        
        @Getter
        private final int totalCount;
        
        private final CountDownLatch countDownLatch = new CountDownLatch(1);
        
        public boolean awaitLatchReleasing(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
            return countDownLatch.await(timeout, timeUnit);
        }
        
        public void releaseLatch() {
            countDownLatch.countDown();
        }
    }
    
    private class PersistRepositoryLazyInitializer extends LazyInitializer<ClusterPersistRepository> {
        
        @Override
        protected ClusterPersistRepository initialize() {
            return (ClusterPersistRepository) PipelineContextManager.getContext(contextKey).getPersistServiceFacade().getRepository();
        }
    }
}
