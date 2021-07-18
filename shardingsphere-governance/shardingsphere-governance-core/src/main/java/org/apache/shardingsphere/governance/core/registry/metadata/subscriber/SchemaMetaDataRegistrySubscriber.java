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

package org.apache.shardingsphere.governance.core.registry.metadata.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.metadata.event.DatabaseCreatedSQLNotificationEvent;
import org.apache.shardingsphere.governance.core.registry.metadata.event.DatabaseDroppedSQLNotificationEvent;
import org.apache.shardingsphere.infra.config.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;

/**
 * Schema meta data registry subscriber.
 */
public final class SchemaMetaDataRegistrySubscriber {
    
    private final SchemaMetaDataPersistService persistService;
    
    public SchemaMetaDataRegistrySubscriber(final RegistryCenterRepository repository) {
        persistService = new SchemaMetaDataPersistService(repository);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Update when database created.
     *
     * @param event database created SQL notification event
     */
    @Subscribe
    public void update(final DatabaseCreatedSQLNotificationEvent event) {
        persistService.persist(event.getDatabaseName(), null);
    }
    
    /**
     * Update when meta data altered.
     *
     * @param event schema altered event
     */
    @Subscribe
    public void update(final SchemaAlteredEvent event) {
        persistService.persist(event.getSchemaName(), event.getSchema());
    }
    
    /**
     * Update when database dropped.
     *
     * @param event database dropped SQL notification event
     */
    @Subscribe
    public void update(final DatabaseDroppedSQLNotificationEvent event) {
        persistService.delete(event.getDatabaseName());
    }
}
