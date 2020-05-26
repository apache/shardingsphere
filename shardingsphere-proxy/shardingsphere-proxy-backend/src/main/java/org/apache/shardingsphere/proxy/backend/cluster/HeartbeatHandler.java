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

package org.apache.shardingsphere.proxy.backend.cluster;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.facade.ClusterFacade;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResult;
import org.apache.shardingsphere.kernal.context.schema.ShardingSphereSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * Heartbeat handler.
 */
@Slf4j
public final class HeartbeatHandler {
    
    private HeartbeatConfiguration configuration;
    
    /**
     * Init heartbeat handler.
     *
     * @param configuration heartbeat configuration
     */
    public void init(final HeartbeatConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "heartbeat configuration can not be null.");
        this.configuration = configuration;
    }
    
    /**
     * Get heartbeat handler instance.
     *
     * @return heartbeat handler instance
     */
    public static HeartbeatHandler getInstance() {
        return HeartbeatHandlerHolder.INSTANCE;
    }
    
    /**
     * Handle heartbeat detect event.
     *
     * @param schemas ShardingSphere schemas
     */
    public void handle(final Map<String, ShardingSphereSchema> schemas) {
        ExecutorService executorService = Executors.newFixedThreadPool(countDataSource(schemas));
        List<FutureTask<Map<String, HeartbeatResult>>> futureTasks = new ArrayList<>();
        schemas.forEach((key, value) -> value.getDataSources().forEach((innerKey, innerValue) -> {
            FutureTask<Map<String, HeartbeatResult>> futureTask = new FutureTask<>(new HeartbeatDetect(key, innerKey, innerValue, configuration));
            futureTasks.add(futureTask);
            executorService.submit(futureTask);
        }));
        reportHeartbeat(futureTasks);
        closeExecutor(executorService);
    }
    
    private Integer countDataSource(final Map<String, ShardingSphereSchema> schemas) {
        return Long.valueOf(schemas.values().stream()
                .collect(Collectors.summarizingInt(entry -> entry.getDataSources().keySet().size())).getSum()).intValue();
    }
    
    private void reportHeartbeat(final List<FutureTask<Map<String, HeartbeatResult>>> futureTasks) {
        Map<String, Collection<HeartbeatResult>> heartbeatResultMap = new HashMap<>();
        futureTasks.stream().forEach(each -> {
            try {
                each.get().entrySet().forEach(entry -> {
                    if (Objects.isNull(heartbeatResultMap.get(entry.getKey()))) {
                        heartbeatResultMap.put(entry.getKey(), new ArrayList<>(Arrays.asList(entry.getValue())));
                    } else {
                        heartbeatResultMap.get(entry.getKey()).add(entry.getValue());
                    }
                });
            } catch (InterruptedException | ExecutionException ex) {
                log.error("Heartbeat report error", ex);
            }
        });
        ClusterFacade.getInstance().reportHeartbeat(new HeartbeatResponse(heartbeatResultMap));
    }
    
    private void closeExecutor(final ExecutorService executorService) {
        if (null != executorService && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    private static final class HeartbeatHandlerHolder {
        
        public static final HeartbeatHandler INSTANCE = new HeartbeatHandler();
    }
}
