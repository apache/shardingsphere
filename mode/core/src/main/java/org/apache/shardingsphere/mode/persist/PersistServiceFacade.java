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

package org.apache.shardingsphere.mode.persist;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.node.QualifiedDataSourceStatePersistService;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacade;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacadeBuilder;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.state.StatePersistService;

/**
 * Persist service facade.
 */
@Getter
public final class PersistServiceFacade implements AutoCloseable {
    
    private final PersistRepository repository;
    
    private final MetaDataPersistFacade metaDataFacade;
    
    private final StatePersistService stateService;
    
    private final QualifiedDataSourceStatePersistService qualifiedDataSourceStateService;
    
    private final ModePersistServiceFacade modeFacade;
    
    public PersistServiceFacade(final PersistRepository repository, final ModeConfiguration modeConfig, final MetaDataContextManager metaDataContextManager) {
        this.repository = repository;
        metaDataFacade = new MetaDataPersistFacade(repository,
                metaDataContextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED));
        stateService = new StatePersistService(repository);
        qualifiedDataSourceStateService = new QualifiedDataSourceStatePersistService(repository);
        modeFacade = TypedSPILoader.getService(ModePersistServiceFacadeBuilder.class, modeConfig.getType()).build(metaDataContextManager, repository);
    }
    
    @Override
    public void close() {
        modeFacade.close();
        repository.close();
    }
}
