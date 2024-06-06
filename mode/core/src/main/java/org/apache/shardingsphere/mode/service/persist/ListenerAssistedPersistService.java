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

package org.apache.shardingsphere.mode.service.persist;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.path.ListenerAssistedNodePath;
import org.apache.shardingsphere.mode.service.pojo.ListenerAssistedPOJO;
import org.apache.shardingsphere.mode.spi.PersistRepository;

/**
 * Listener assisted persist service.
 */
@RequiredArgsConstructor
public final class ListenerAssistedPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist database name listener assisted.
     *
     * @param listenerAssistedPOJO listener assisted pojo
     */
    public void persistDatabaseNameListenerAssisted(final ListenerAssistedPOJO listenerAssistedPOJO) {
        repository.persist(ListenerAssistedNodePath.getDatabaseNameNodePath(listenerAssistedPOJO.getDatabaseName()), YamlEngine.marshal(listenerAssistedPOJO));
    }
    
    /**
     * Delete database name listener assisted.
     *
     * @param databaseName database name
     */
    public void deleteDatabaseNameListenerAssisted(final String databaseName) {
        repository.delete(ListenerAssistedNodePath.getDatabaseNameNodePath(databaseName));
    }
}
