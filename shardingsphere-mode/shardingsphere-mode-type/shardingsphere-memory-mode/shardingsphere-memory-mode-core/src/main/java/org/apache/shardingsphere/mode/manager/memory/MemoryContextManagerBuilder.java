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

package org.apache.shardingsphere.mode.manager.memory;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.memory.lock.MemoryLockContext;
import org.apache.shardingsphere.mode.manager.memory.workerid.generator.MemoryWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Memory context manager builder.
 */
public final class MemoryContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        InstanceContext instanceContext = buildInstanceContext(parameter);
        return new ContextManager(buildMetaDataContexts(parameter, instanceContext), instanceContext);
    }
    
    private InstanceContext buildInstanceContext(final ContextManagerBuilderParameter parameter) {
        ComputeNodeInstance instance = new ComputeNodeInstance(parameter.getInstanceMetaData());
        instance.setLabels(parameter.getLabels());
        return new InstanceContext(instance,
                new MemoryWorkerIdGenerator(), Optional.ofNullable(parameter.getModeConfig()).orElseGet(() -> new ModeConfiguration(getType(), null, false)),
                new MemoryLockContext(), new EventBusContext());
    }
    
    private MetaDataContexts buildMetaDataContexts(final ContextManagerBuilderParameter parameter, final InstanceContext instanceContext) throws SQLException {
        ConfigurationProperties props = new ConfigurationProperties(parameter.getProps());
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(parameter.getDatabaseConfigs(), props, instanceContext);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(parameter.getGlobalRuleConfigs(), databases, instanceContext));
        return new MetaDataContexts(null, new ShardingSphereMetaData(databases, globalMetaData, props), OptimizerContextFactory.create(databases, globalMetaData));
    }
    
    @Override
    public String getType() {
        return "Memory";
    }
}
