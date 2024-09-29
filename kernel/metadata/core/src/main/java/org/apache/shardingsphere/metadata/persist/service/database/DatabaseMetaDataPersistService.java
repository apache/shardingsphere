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

package org.apache.shardingsphere.metadata.persist.service.database;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.schema.SchemaMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.table.TableMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.table.ViewMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;

/**
 * Database meta data registry service.
 */
@Getter
public final class DatabaseMetaDataPersistService {
    
    @Getter(AccessLevel.NONE)
    private final PersistRepository repository;
    
    private final SchemaMetaDataPersistService schemaMetaDataPersistService;
    
    private final TableMetaDataPersistService tableMetaDataPersistService;
    
    private final ViewMetaDataPersistService viewMetaDataPersistService;
    
    public DatabaseMetaDataPersistService(final PersistRepository repository, final MetaDataVersionPersistService metaDataVersionPersistService) {
        this.repository = repository;
        schemaMetaDataPersistService = new SchemaMetaDataPersistService(repository, metaDataVersionPersistService);
        tableMetaDataPersistService = new TableMetaDataPersistService(repository, metaDataVersionPersistService);
        viewMetaDataPersistService = new ViewMetaDataPersistService(repository, metaDataVersionPersistService);
    }
    
    /**
     * Add database.
     *
     * @param databaseName to be added database name
     */
    public void add(final String databaseName) {
        repository.persist(DatabaseMetaDataNode.getDatabaseNamePath(databaseName), "");
    }
    
    /**
     * Drop database.
     *
     * @param databaseName to be dropped database name
     */
    public void drop(final String databaseName) {
        repository.delete(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
    }
    
    /**
     * Load database names.
     *
     * @return loaded database names
     */
    public Collection<String> loadAllDatabaseNames() {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataNode());
    }
}
