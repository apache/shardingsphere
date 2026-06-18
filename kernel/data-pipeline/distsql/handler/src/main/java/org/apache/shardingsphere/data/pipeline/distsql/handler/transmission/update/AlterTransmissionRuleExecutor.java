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

package org.apache.shardingsphere.data.pipeline.distsql.handler.transmission.update;

import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.distsql.segment.TransmissionRuleSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Alter transmission rule executor.
 */
public final class AlterTransmissionRuleExecutor implements DistSQLUpdateExecutor<AlterTransmissionRuleStatement> {
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    @Override
    public void executeUpdate(final AlterTransmissionRuleStatement sqlStatement, final ContextManager contextManager) {
        PipelineProcessConfiguration processConfig = convertToProcessConfiguration(sqlStatement.getProcessConfigSegment());
        AlgorithmConfiguration streamChannel = processConfig.getStreamChannel();
        if (null != streamChannel && !TypedSPILoader.findService(PipelineChannelCreator.class, streamChannel.getType()).isPresent()) {
            throw new PipelineInvalidParameterException("Unknown stream channel type `" + streamChannel.getType() + "`.");
        }
        String jobType = TypedSPILoader.getService(PipelineJobType.class, sqlStatement.getJobTypeName()).getType();
        processConfigPersistService.persist(new PipelineContextKey(InstanceType.PROXY), jobType, processConfig);
    }
    
    private PipelineProcessConfiguration convertToProcessConfiguration(final TransmissionRuleSegment segment) {
        return new PipelineProcessConfiguration(
                convertToReadConfiguration(segment.getReadSegment()), convertToWriteConfiguration(segment.getWriteSegment()), convertToAlgorithm(segment.getStreamChannel()));
    }
    
    private PipelineReadConfiguration convertToReadConfiguration(final ReadOrWriteSegment readSegment) {
        return null == readSegment
                ? null
                : new PipelineReadConfiguration(readSegment.getWorkerThread(), readSegment.getBatchSize(), readSegment.getShardingSize(), convertToAlgorithm(readSegment.getRateLimiter()));
    }
    
    private PipelineWriteConfiguration convertToWriteConfiguration(final ReadOrWriteSegment writeSegment) {
        return null == writeSegment ? null : new PipelineWriteConfiguration(writeSegment.getWorkerThread(), writeSegment.getBatchSize(), convertToAlgorithm(writeSegment.getRateLimiter()));
    }
    
    private AlgorithmConfiguration convertToAlgorithm(final AlgorithmSegment algorithmSegment) {
        return null == algorithmSegment ? null : new AlgorithmConfiguration(algorithmSegment.getName(), algorithmSegment.getProps());
    }
    
    @Override
    public Class<AlterTransmissionRuleStatement> getType() {
        return AlterTransmissionRuleStatement.class;
    }
}
