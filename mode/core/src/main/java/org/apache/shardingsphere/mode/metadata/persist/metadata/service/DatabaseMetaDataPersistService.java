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
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;

/**
 * Database meta data persist service.
 */
@RequiredArgsConstructor
public final class DatabaseMetaDataPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Add database.
     *
     * @param databaseName to be added database name
     */
    public void add(final String databaseName) {
        repository.persist(NodePathGenerator.toPath(new DatabaseMetaDataNodePath(databaseName)), "");
    }
    
    /**
     * Drop database.
     *
     * @param databaseName to be dropped database name
     */
    public void drop(final String databaseName) {
        repository.delete(NodePathGenerator.toPath(new DatabaseMetaDataNodePath(databaseName)));
    }
    
    /**
     * Load database names.
     *
     * @return loaded database names
     */
    public Collection<String> loadAllDatabaseNames() {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new DatabaseMetaDataNodePath(null)));
    }
}
