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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.config.processor.JobConfigurationChangedProcessor;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.event.handler.PipelineMetaDataChangedEventHandler;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.DataChangedEvent;

import java.util.regex.Pattern;

/**
 * Config meta data changed event handler.
 */
@Slf4j
public final class ConfigMetaDataChangedEventHandler implements PipelineMetaDataChangedEventHandler {
    
    @Override
    public Pattern getKeyPattern() {
        return PipelineMetaDataNode.CONFIG_PATTERN;
    }
    
    @Override
    public void handle(final String jobId, final DataChangedEvent event) {
        JobConfiguration jobConfig;
        try {
            jobConfig = YamlEngine.unmarshal(event.getValue(), JobConfigurationPOJO.class, true).toJobConfiguration();
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("unmarshal job configuration pojo failed.", ex);
            return;
        }
        log.info("{} job configuration: {}, disabled={}", event.getType(), event.getKey(), jobConfig.isDisabled());
        TypedSPILoader.findService(JobConfigurationChangedProcessor.class, PipelineJobIdUtils.parseJobType(jobConfig.getJobName()).getType())
                .ifPresent(optional -> optional.process(event.getType(), jobConfig));
    }
}
