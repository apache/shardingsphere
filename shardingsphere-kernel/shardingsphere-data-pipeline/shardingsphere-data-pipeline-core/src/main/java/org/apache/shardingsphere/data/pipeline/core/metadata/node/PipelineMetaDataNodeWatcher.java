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

package org.apache.shardingsphere.data.pipeline.core.metadata.node;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.PipelineMetaDataChangedEventHandler;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.PipelineMetaDataChangedEventHandlerFactory;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Pipeline meta data node watcher.
 */
@Slf4j
public final class PipelineMetaDataNodeWatcher {
    
    private static final PipelineMetaDataNodeWatcher INSTANCE = new PipelineMetaDataNodeWatcher();
    
    private final Map<Pattern, PipelineMetaDataChangedEventHandler> listenerMap = new ConcurrentHashMap<>();
    
    private PipelineMetaDataNodeWatcher() {
        Collection<PipelineMetaDataChangedEventHandler> instances = PipelineMetaDataChangedEventHandlerFactory.findAllInstances();
        for (PipelineMetaDataChangedEventHandler each : instances) {
            listenerMap.put(each.getKeyPattern(), each);
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI().watch(DataPipelineConstants.DATA_PIPELINE_ROOT, this::dispatchEvent);
    }
    
    private void dispatchEvent(final DataChangedEvent event) {
        for (Entry<Pattern, PipelineMetaDataChangedEventHandler> entry : listenerMap.entrySet()) {
            if (entry.getKey().matcher(event.getKey()).matches()) {
                entry.getValue().handle(event);
                return;
            }
        }
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static PipelineMetaDataNodeWatcher getInstance() {
        return INSTANCE;
    }
}
