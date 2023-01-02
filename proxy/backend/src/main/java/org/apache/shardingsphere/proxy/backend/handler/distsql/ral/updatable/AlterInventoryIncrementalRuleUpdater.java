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

import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.distsql.handler.update.RALUpdater;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterInventoryIncrementalRuleStatement;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.converter.InventoryIncrementalProcessConfigurationSegmentConverter;

/**
 * Alter inventory incremental rule updater.
 */
public final class AlterInventoryIncrementalRuleUpdater implements RALUpdater<AlterInventoryIncrementalRuleStatement> {
    
    @Override
    public void executeUpdate(final String databaseName, final AlterInventoryIncrementalRuleStatement sqlStatement) {
        InventoryIncrementalJobAPI jobAPI = (InventoryIncrementalJobAPI) TypedSPIRegistry.getRegisteredService(PipelineJobAPI.class, sqlStatement.getJobTypeName());
        PipelineProcessConfiguration processConfig = InventoryIncrementalProcessConfigurationSegmentConverter.convert(sqlStatement.getProcessConfigSegment());
        jobAPI.alterProcessConfiguration(processConfig);
    }
    
    @Override
    public String getType() {
        return AlterInventoryIncrementalRuleStatement.class.getName();
    }
}
