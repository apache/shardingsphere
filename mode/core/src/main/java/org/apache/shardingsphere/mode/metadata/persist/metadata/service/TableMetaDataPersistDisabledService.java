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

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.persist.service.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Table meta data persist service for disabled persist.
 */
@RequiredArgsConstructor
public final class TableMetaDataPersistDisabledService implements TableMetaDataPersistService {
    
    private final PersistRepository repository;
    
    @Override
    public Collection<ShardingSphereTable> load(final String databaseName, final String schemaName) {
        return new LinkedList<>();
    }
    
    @Override
    public ShardingSphereTable load(final String databaseName, final String schemaName, final String tableName) {
        return null;
    }
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Collection<ShardingSphereTable> tables) {
        // TODO Implement persist logic if needed
    }
    
    @Override
    public void drop(final String databaseName, final String schemaName, final String tableName) {
        // TODO Implement drop logic if needed
    }
    
    @Override
    public void drop(final String databaseName, final String schemaName, final Collection<ShardingSphereTable> tables) {
        // TODO Implement drop logic if needed
    }
}
