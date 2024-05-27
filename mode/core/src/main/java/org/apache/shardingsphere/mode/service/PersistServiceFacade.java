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

package org.apache.shardingsphere.mode.service;

import lombok.Getter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.service.persist.ComputeNodePersistService;
import org.apache.shardingsphere.mode.service.persist.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.service.persist.PersistServiceBuilder;
import org.apache.shardingsphere.mode.service.persist.ProcessPersistService;
import org.apache.shardingsphere.mode.service.pojo.ShardingSphereSchemaDataAlteredPOJO;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.state.StatePersistService;

/**
 * Persist service facade.
 */
@Getter
public final class PersistServiceFacade {
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final ComputeNodePersistService computeNodePersistService;
    
    private final StatePersistService statePersistService;
    
    private final MetaDataManagerPersistService metaDataManagerPersistService;
    
    private final ProcessPersistService processPersistService;
    
    public PersistServiceFacade(final PersistRepository repository, final ModeConfiguration modeConfiguration, final ContextManager contextManager) {
        metaDataPersistService = new MetaDataPersistService(repository);
        computeNodePersistService = new ComputeNodePersistService(repository);
        statePersistService = new StatePersistService(repository);
        PersistServiceBuilder persistServiceBuilder = TypedSPILoader.getService(PersistServiceBuilder.class, modeConfiguration.getType());
        metaDataManagerPersistService = persistServiceBuilder.buildMetaDataManagerPersistService(contextManager);
        processPersistService = persistServiceBuilder.buildProcessPersistService(contextManager);
    }
    
    /**
     * Update when sharding sphere schema data altered.
     *
     * @param schemaDataAlteredPOJO sharding sphere schema data
     */
    public void persist(final ShardingSphereSchemaDataAlteredPOJO schemaDataAlteredPOJO) {
        String databaseName = schemaDataAlteredPOJO.getDatabaseName();
        String schemaName = schemaDataAlteredPOJO.getSchemaName();
        metaDataPersistService.getShardingSphereDataPersistService().getTableRowDataPersistService().persist(databaseName, schemaName, schemaDataAlteredPOJO.getTableName(),
                schemaDataAlteredPOJO.getAddedRows());
        metaDataPersistService.getShardingSphereDataPersistService().getTableRowDataPersistService().persist(databaseName, schemaName, schemaDataAlteredPOJO.getTableName(),
                schemaDataAlteredPOJO.getUpdatedRows());
        metaDataPersistService.getShardingSphereDataPersistService().getTableRowDataPersistService().delete(databaseName, schemaName, schemaDataAlteredPOJO.getTableName(),
                schemaDataAlteredPOJO.getDeletedRows());
    }
}
