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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.List;

/**
 * Meta data version persist service.
 */
@RequiredArgsConstructor
@Slf4j
public final class MetaDataVersionPersistService implements MetaDataVersionBasedPersistService {
    
    private final PersistRepository repository;
    
    @Override
    public void switchActiveVersion(final Collection<MetaDataVersion> metaDataVersions) {
        for (MetaDataVersion each : metaDataVersions) {
            if (each.getNextActiveVersion().equals(each.getCurrentActiveVersion())) {
                continue;
            }
            repository.persist(each.getActiveVersionNodePath(), each.getNextActiveVersion());
            repository.delete(each.getVersionsNodePath());
        }
    }
    
    @Override
    public String getActiveVersionByFullPath(final String fullPath) {
        return repository.query(fullPath);
    }
    
    @Override
    public String getVersionPathByActiveVersion(final String path, final String activeVersion) {
        return repository.query(DatabaseMetaDataNode.getVersionNodeByActiveVersionPath(path, activeVersion));
    }
    
    @Override
    public List<String> getVersions(final String path) {
        List<String> result = repository.getChildrenKeys(path);
        if (result.size() > 1) {
            log.warn("There are multiple versions of ：{}, please check the configuration.", path);
            result.sort((v1, v2) -> Integer.compare(Integer.parseInt(v2), Integer.parseInt(v1)));
        }
        return result;
    }
}
