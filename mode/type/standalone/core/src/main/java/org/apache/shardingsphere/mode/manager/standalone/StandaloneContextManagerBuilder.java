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

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.standalone.subscriber.ProcessStandaloneSubscriber;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryFactory;

import java.sql.SQLException;

/**
 * Standalone context manager builder.
 */
public final class StandaloneContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter param) throws SQLException {
        StandalonePersistRepository repository = StandalonePersistRepositoryFactory.getInstance(param.getModeConfiguration().getRepository());
        MetaDataPersistService persistService = new MetaDataPersistService(repository);
        persistConfigurations(persistService, param);
        InstanceContext instanceContext = buildInstanceContext(param);
        new ProcessStandaloneSubscriber(instanceContext.getEventBusContext());
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(persistService, param, instanceContext);
        return new ContextManager(metaDataContexts, instanceContext);
    }
    
    private void persistConfigurations(final MetaDataPersistService persistService, final ContextManagerBuilderParameter param) {
        if (!param.isEmpty()) {
            persistService.persistConfigurations(param.getDatabaseConfigs(), param.getGlobalRuleConfigs(), param.getProps());
        }
    }
    
    private InstanceContext buildInstanceContext(final ContextManagerBuilderParameter param) {
        return new InstanceContext(new ComputeNodeInstance(param.getInstanceMetaData()),
                new StandaloneWorkerIdGenerator(), param.getModeConfiguration(), new GlobalLockContext(null), new EventBusContext());
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
