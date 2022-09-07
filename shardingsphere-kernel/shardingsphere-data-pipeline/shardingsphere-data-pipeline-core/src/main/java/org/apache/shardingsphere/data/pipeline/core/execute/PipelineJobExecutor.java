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

package org.apache.shardingsphere.data.pipeline.core.execute;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.spi.listener.PipelineMetaDataListener;
import org.apache.shardingsphere.data.pipeline.core.spi.listener.PipelineMetaDataListenerFactory;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Pipeline job executor.
 */
@Slf4j
public final class PipelineJobExecutor {
    
    private static final Map<Pattern, PipelineMetaDataListener> LISTENER_MAP = new ConcurrentHashMap<>();
    
    /**
     * Register listener.
     */
    public static void registerListener() {
        if (!LISTENER_MAP.isEmpty()) {
            log.info("listener already register");
            return;
        }
        for (JobType each : JobType.values()) {
            Optional<PipelineMetaDataListener> instance = PipelineMetaDataListenerFactory.findInstance(each.getTypeName());
            if (!instance.isPresent()) {
                continue;
            }
            PipelineMetaDataListener pipelineMetaDataListener = instance.get();
            LISTENER_MAP.put(Pattern.compile(pipelineMetaDataListener.getWatchKey()), pipelineMetaDataListener);
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI().watch(DataPipelineConstants.DATA_PIPELINE_ROOT, event -> {
            if (PipelineMetaDataNode.BARRIER_PATTERN.matcher(event.getKey()).matches() && event.getType() == Type.ADDED) {
                PipelineDistributedBarrier.getInstance().checkChildrenNodeCount(event);
            }
            dispatchEvent(event);
        });
    }
    
    private static void dispatchEvent(final DataChangedEvent event) {
        JobConfigurationPOJO jobConfigPOJO;
        log.info("{} job config: {}", event.getType(), event.getKey());
        try {
            jobConfigPOJO = YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class, true);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("analyze job config pojo failed.", ex);
            return;
        }
        for (Entry<Pattern, PipelineMetaDataListener> entry : LISTENER_MAP.entrySet()) {
            if (entry.getKey().matcher(event.getKey()).matches()) {
                entry.getValue().handler(event, jobConfigPOJO);
                return;
            }
        }
    }
}
