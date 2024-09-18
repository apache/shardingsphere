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
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.GlobalLockPersistService;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.spi.PersistRepository;

/**
 * Statistics collect job.
 */
@RequiredArgsConstructor
public final class StatisticsCollectJob implements SimpleJob {
    
    private final ContextManager contextManager;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        PersistRepository repository = contextManager.getPersistServiceFacade().getRepository();
        if (repository instanceof ClusterPersistRepository) {
            new ShardingSphereStatisticsRefreshEngine(contextManager, new GlobalLockContext(new GlobalLockPersistService((ClusterPersistRepository) repository))).refresh();
        }
    }
}
