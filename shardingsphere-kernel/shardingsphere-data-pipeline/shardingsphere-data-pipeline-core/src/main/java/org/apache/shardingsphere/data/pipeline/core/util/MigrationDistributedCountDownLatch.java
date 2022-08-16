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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public final class MigrationDistributedCountDownLatch {
    
    private static final MigrationDistributedCountDownLatch INSTANCE = new MigrationDistributedCountDownLatch();
    
    private static final Pattern BARRIER_MATCH_PATTERN = Pattern.compile(DataPipelineConstants.DATA_PIPELINE_ROOT + "/(j\\d{2}[0-9a-f]+)/barrier/(enable|distable)/\\d+");
    
    private final ClusterPersistRepository clusterPersistRepository;
    
    private final Map<String, InnerCountDownLatchHolder> countDownLatchMap = new ConcurrentHashMap<>();
    
    private MigrationDistributedCountDownLatch() {
        clusterPersistRepository = (ClusterPersistRepository) PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository();
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static MigrationDistributedCountDownLatch getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register count down latch.
     *
     * @param parentPath parent path
     * @param totalCount total count
     */
    public void register(final String parentPath, final int totalCount) {
        clusterPersistRepository.persist(parentPath, "");
        countDownLatchMap.computeIfAbsent(parentPath, k -> new InnerCountDownLatchHolder(totalCount, new CountDownLatch(1)));
    }
    
    /**
     * Persist ephemeral children node.
     *
     * @param parentPath parent path
     * @param shardingItem sharding item
     */
    public void persistEphemeralChildrenNode(final String parentPath, final int shardingItem) {
        String key = String.join("/", parentPath, Integer.toString(shardingItem));
        clusterPersistRepository.delete(key);
        clusterPersistRepository.persistEphemeral(key, "");
    }
    
    /**
     * Persist ephemeral children node.
     *
     * @param parentPath parent path
     */
    public void removeParentNode(final String parentPath) {
        clusterPersistRepository.delete(String.join("/", parentPath));
        countDownLatchMap.remove(parentPath);
    }
    
    /**
     * Await unitl all children node is ready.
     *
     * @param parentPath parent path
     * @param timeout timeout
     * @param timeUnit time unit
     * @return
     * true if the count reached zero and false if the waiting time elapsed before the count reached zero
     */
    public boolean await(final String parentPath, final long timeout, final TimeUnit timeUnit) {
        InnerCountDownLatchHolder holder = countDownLatchMap.get(parentPath);
        if (holder == null) {
            return false;
        }
        try {
            boolean awaitResult = holder.getCountDownLatch().await(timeout, timeUnit);
            if (!awaitResult) {
                log.info("await timeout, parent path: {}, timeout: {}, time unit: {}", parentPath, timeout, timeUnit);
            }
            return awaitResult;
        } catch (final InterruptedException ignored) {
        }
        return false;
    }
    
    /**
     * Check child node count equal shardingCount.
     *
     * @param event event
     */
    public void checkChildrenNodeCount(final DataChangedEvent event) {
        if (StringUtils.isBlank(event.getKey())) {
            return;
        }
        if (!BARRIER_MATCH_PATTERN.matcher(event.getKey()).matches()) {
            return;
        }
        String parentPath = event.getKey().substring(0, event.getKey().lastIndexOf("/"));
        InnerCountDownLatchHolder holder = countDownLatchMap.get(parentPath);
        if (holder == null) {
            return;
        }
        List<String> childrenKeys = clusterPersistRepository.getChildrenKeys(parentPath);
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
