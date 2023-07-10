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

package org.apache.shardingsphere.metadata.persist.service.version;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;

/**
 * Meta data version persist service.
 */
@RequiredArgsConstructor
public final class MetaDataVersionPersistService implements MetaDataVersionBasedPersistService {
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    private final PersistRepository repository;
    
    // TODO Need to use transaction operation
    @Override
    public void switchActiveVersion(final Collection<MetaDataVersion> metaDataVersions) {
        for (MetaDataVersion each : metaDataVersions) {
            if (each.getNextActiveVersion().equals(each.getCurrentActiveVersion())) {
                continue;
            }
            repository.persist(each.getKey() + "/" + ACTIVE_VERSION, each.getNextActiveVersion());
            repository.delete(String.join("/", each.getKey(), VERSIONS, each.getCurrentActiveVersion()));
        }
    }
    
    @Override
    public String getActiveVersionByFullPath(final String fullPath) {
        return repository.getDirectly(fullPath);
    }
    
    @Override
    public String getVersionPathByActiveVersion(final String path, final String activeVersion) {
        return repository.getDirectly(NewDatabaseMetaDataNode.getVersionNodeByActiveVersionPath(path, activeVersion));
    }
}
