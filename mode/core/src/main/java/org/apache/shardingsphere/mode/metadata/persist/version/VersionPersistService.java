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

package org.apache.shardingsphere.mode.metadata.persist.version;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.node.path.version.MetaDataVersion;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Version persist service.
 */
@RequiredArgsConstructor
@Slf4j
public final class VersionPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist.
     *
     * @param versionNodePath version node path
     * @param content to be persisted content
     * @return version of persisted content
     */
    public int persist(final VersionNodePath versionNodePath, final String content) {
        int nextVersion = getNextVersion(versionNodePath.getVersionsPath());
        repository.persist(versionNodePath.getVersionPath(nextVersion), content);
        switchActiveVersion(versionNodePath, nextVersion);
        return nextVersion;
    }
    
    private void switchActiveVersion(final VersionNodePath versionNodePath, final int currentVersion) {
        repository.persist(versionNodePath.getActiveVersionPath(), String.valueOf(currentVersion));
        if (MetaDataVersion.INIT_VERSION != currentVersion) {
            getVersions(versionNodePath.getVersionsPath()).stream().filter(version -> version < currentVersion)
                    .forEach(version -> repository.delete(versionNodePath.getVersionPath(version)));
        }
    }
    
    /**
     * Get next version.
     *
     * @param path path
     * @return next version
     */
    public int getNextVersion(final String path) {
        List<Integer> versions = getVersions(path);
        return versions.isEmpty() ? MetaDataVersion.INIT_VERSION : versions.get(0) + 1;
    }
    
    private List<Integer> getVersions(final String path) {
        List<Integer> result = repository.getChildrenKeys(path).stream().map(Integer::parseInt).collect(Collectors.toList());
        if (result.size() > 2) {
            log.warn("There are multiple versions of: {}, please check the configuration.", path);
            result.sort(Collections.reverseOrder());
        }
        return result;
    }
    
    /**
     * Load content.
     *
     * @param versionNodePath version node path
     * @return loaded content
     */
    public String loadContent(final VersionNodePath versionNodePath) {
        String version = repository.query(versionNodePath.getActiveVersionPath());
        return null == version ? null : repository.query(versionNodePath.getVersionPath(Integer.parseInt(version)));
    }
}
