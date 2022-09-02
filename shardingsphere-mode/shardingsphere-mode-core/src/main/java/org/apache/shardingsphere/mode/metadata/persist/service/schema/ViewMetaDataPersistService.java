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

package org.apache.shardingsphere.mode.metadata.persist.service.schema;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collections;
import java.util.Map;

/**
 * View meta data persist service.
 */
@RequiredArgsConstructor
public final class ViewMetaDataPersistService implements SchemaMetaDataPersistService<Map<String, ShardingSphereView>> {
    
    private final PersistRepository repository;
    
    @Override
    public void compareAndPersist(final String databaseName, final String schemaName, final Map<String, ShardingSphereView> views) {
        
    }
    
    @Override
    public void persist(final String databaseName, final String schemaName, final Map<String, ShardingSphereView> views) {
        
    }
    
    @Override
    public Map<String, ShardingSphereView> load(final String databaseName, final String schemaName) {
        return Collections.emptyMap();
    }
    
    @Override
    public void delete(final String databaseName, final String schemaName, final String name) {
        
    }
}
