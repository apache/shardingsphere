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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.NewMetaDataPersistService;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.subscriber.StandaloneProcessSubscriber;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.NewMetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscriber;

import java.sql.SQLException;
import java.util.Properties;

/**
 * TODO Rename StandaloneContextManagerBuilder when metadata structure adjustment completed. #25485
 * New Standalone context manager builder.
 */
public final class NewStandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param) throws SQLException {
        PersistRepositoryConfiguration repositoryConfig = param.getModeConfiguration().getRepository();
        StandalonePersistRepository repository = TypedSPILoader.getService(
                StandalonePersistRepository.class, null == repositoryConfig ? null : repositoryConfig.getType(), null == repositoryConfig ? new Properties() : repositoryConfig.getProps());
        NewMetaDataPersistService persistService = new NewMetaDataPersistService(repository);
        InstanceContext instanceContext = buildInstanceContext(param);
        new StandaloneProcessSubscriber(instanceContext.getEventBusContext());
        MetaDataContexts metaDataContexts = NewMetaDataContextsFactory.create(persistService, param, instanceContext);
        ContextManager result = new ContextManager(metaDataContexts, instanceContext);
        registerSubscriber(result);
        setContextManagerAware(result);
        return result;
    }
    
    private InstanceContext buildInstanceContext(final ContextManagerBuilderParameter param) {
        return new InstanceContext(new ComputeNodeInstance(param.getInstanceMetaData()),
                new StandaloneWorkerIdGenerator(), param.getModeConfiguration(), new NewStandaloneModeContextManager(), new GlobalLockContext(null), new EventBusContext());
    }
    
    private void registerSubscriber(final ContextManager contextManager) {
        contextManager.getInstanceContext().getEventBusContext().register(new RuleItemChangedSubscriber(contextManager));
    }
    
    private void setContextManagerAware(final ContextManager contextManager) {
        ((NewStandaloneModeContextManager) contextManager.getInstanceContext().getModeContextManager()).setContextManagerAware(contextManager);
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
