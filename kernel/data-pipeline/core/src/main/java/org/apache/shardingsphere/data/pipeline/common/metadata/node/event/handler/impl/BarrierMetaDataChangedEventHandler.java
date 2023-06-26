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

package org.apache.shardingsphere.data.pipeline.common.metadata.node.event.handler.impl;

import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.event.handler.PipelineMetaDataChangedEventHandler;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

import java.util.regex.Pattern;

/**
 * Barrier meta data changed event handler.
 */
public final class BarrierMetaDataChangedEventHandler implements PipelineMetaDataChangedEventHandler {
    
    @Override
    public Pattern getKeyPattern() {
        return PipelineMetaDataNode.BARRIER_PATTERN;
    }
    
    @Override
    public void handle(final String jobId, final DataChangedEvent event) {
        if (event.getType() == Type.ADDED) {
            PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId)).notifyChildrenNodeCountCheck(event.getKey());
        }
    }
}
