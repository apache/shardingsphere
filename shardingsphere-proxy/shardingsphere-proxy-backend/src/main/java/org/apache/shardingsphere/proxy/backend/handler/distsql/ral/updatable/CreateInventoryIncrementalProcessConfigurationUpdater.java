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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.CreateInventoryIncrementalProcessConfigurationStatement;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.distsql.update.RALUpdater;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.converter.InventoryIncrementalProcessConfigurationSegmentConverter;

/**
 * Create inventory incremental process configuration updater.
 */
public final class CreateInventoryIncrementalProcessConfigurationUpdater implements RALUpdater<CreateInventoryIncrementalProcessConfigurationStatement> {
    
    @Override
    public void executeUpdate(final String databaseName, final CreateInventoryIncrementalProcessConfigurationStatement sqlStatement) {
        PipelineJobPublicAPI jobAPI = PipelineJobPublicAPIFactory.getPipelineJobPublicAPI(sqlStatement.getJobTypeName());
        PipelineProcessConfiguration processConfig = InventoryIncrementalProcessConfigurationSegmentConverter.convert(sqlStatement.getProcessConfigSegment());
        jobAPI.createProcessConfiguration(processConfig);
    }
    
    @Override
    public String getType() {
        return CreateInventoryIncrementalProcessConfigurationStatement.class.getName();
    }
}
