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

package org.apache.shardingsphere.schedule.core.job.statistics.collect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.statistics.StatisticsRefreshEngine;

/**
 * Statistics collect job.
 */
@RequiredArgsConstructor
@Slf4j
public final class StatisticsCollectJob implements SimpleJob {
    
    private final ContextManager contextManager;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.debug("Running statistics collect job");
        try {
            if (contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()) {
                new StatisticsRefreshEngine(contextManager).refresh();
            }
        } finally {
            log.debug("Finished statistics collect job");
        }
    }
}
