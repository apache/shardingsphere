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

package org.apache.shardingsphere.data.pipeline.common.metadata.node;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.event.handler.PipelineMetaDataChangedEventHandler;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Pipeline meta data node watcher.
 */
@Slf4j
public final class PipelineMetaDataNodeWatcher {
    
    private static final Map<PipelineContextKey, PipelineMetaDataNodeWatcher> INSTANCE_MAP = new ConcurrentHashMap<>();
    
    private static final ExecutorService EVENT_LISTENER_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Pipeline-EventListener-%d").build());
    
    private final Map<Pattern, PipelineMetaDataChangedEventHandler> listenerMap = new ConcurrentHashMap<>();
    
    private PipelineMetaDataNodeWatcher(final PipelineContextKey contextKey) {
        listenerMap.putAll(ShardingSphereServiceLoader.getServiceInstances(PipelineMetaDataChangedEventHandler.class)
                .stream().collect(Collectors.toMap(PipelineMetaDataChangedEventHandler::getKeyPattern, each -> each, (key, value) -> value)));
        PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).watch(DataPipelineConstants.DATA_PIPELINE_ROOT, this::dispatchEvent);
    }
    
    private void dispatchEvent(final DataChangedEvent event) {
        CompletableFuture.runAsync(() -> dispatchEvent0(event), EVENT_LISTENER_EXECUTOR).whenComplete((unused, throwable) -> {
            if (null != throwable) {
                log.error("dispatch event failed", throwable);
            }
        });
    }
    
    private void dispatchEvent0(final DataChangedEvent event) {
        for (Entry<Pattern, PipelineMetaDataChangedEventHandler> entry : listenerMap.entrySet()) {
            Matcher matcher = entry.getKey().matcher(event.getKey());
            if (matcher.matches()) {
                String jobId = matcher.group(1);
                entry.getValue().handle(jobId, event);
                return;
            }
        }
    }
    
    /**
     * Get instance.
     *
     * @param contextKey context key
     * @return instance
     */
    public static PipelineMetaDataNodeWatcher getInstance(final PipelineContextKey contextKey) {
        return INSTANCE_MAP.computeIfAbsent(contextKey, PipelineMetaDataNodeWatcher::new);
    }
}
