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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.exclusive.StandaloneExclusiveOperatorContext;
import org.apache.shardingsphere.mode.manager.standalone.workerid.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Standalone context manager builder.
 */
public final class StandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param, final EventBusContext eventBusContext) throws SQLException {
        PersistRepositoryConfiguration repositoryConfig = param.getModeConfiguration().getRepository();
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(param.getInstanceMetaData()), param.getModeConfiguration(), eventBusContext);
        computeNodeInstanceContext.init(new StandaloneWorkerIdGenerator());
        StandalonePersistRepository repository = TypedSPILoader.getService(
                StandalonePersistRepository.class, null == repositoryConfig ? null : repositoryConfig.getType(), null == repositoryConfig ? new Properties() : repositoryConfig.getProps());
        ExclusiveOperatorEngine exclusiveOperatorEngine = new ExclusiveOperatorEngine(new StandaloneExclusiveOperatorContext());
        MetaDataContexts metaDataContexts = new MetaDataContextsFactory(new MetaDataPersistFacade(repository), computeNodeInstanceContext).create(param);
        return new ContextManager(metaDataContexts, computeNodeInstanceContext, exclusiveOperatorEngine, repository);
    }
    
    @Override
    public String getType() {
        return "Standalone";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
