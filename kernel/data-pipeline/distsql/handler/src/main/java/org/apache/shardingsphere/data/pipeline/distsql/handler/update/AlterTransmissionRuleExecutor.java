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

package org.apache.shardingsphere.data.pipeline.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
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
        PipelineProcessConfiguration processConfig = TransmissionProcessConfigurationSegmentConverter.convert(sqlStatement.getProcessConfigSegment());
        String jobType = TypedSPILoader.getService(PipelineJobType.class, sqlStatement.getJobTypeName()).getType();
        processConfigPersistService.persist(new PipelineContextKey(InstanceType.PROXY), jobType, processConfig);
    }
    
    @Override
    public Class<AlterTransmissionRuleStatement> getType() {
        return AlterTransmissionRuleStatement.class;
    }
}
